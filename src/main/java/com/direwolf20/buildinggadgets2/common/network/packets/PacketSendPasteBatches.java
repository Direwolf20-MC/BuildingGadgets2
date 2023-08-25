package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSendPasteBatches {
    private FriendlyByteBuf data;
    private UUID copyUUID;
    private int position;
    private int numberOfPackets;

    public PacketSendPasteBatches(UUID copyUUID, int numberOfPackets, int position, FriendlyByteBuf data) {
        this.data = data;
        this.copyUUID = copyUUID;
        this.numberOfPackets = numberOfPackets;
        this.position = position;
    }

    public static PacketSendPasteBatches decode(FriendlyByteBuf buf) {
        UUID copyUUID = buf.readUUID(); // Decode UUID
        int numberOfPackets = buf.readInt(); // Decode int
        int position = buf.readInt(); // Decode position
        FriendlyByteBuf data = new FriendlyByteBuf(buf.readBytes(buf.readableBytes())); // Read the remaining bytes
        return new PacketSendPasteBatches(copyUUID, numberOfPackets, position, data);
    }

    public static void encode(PacketSendPasteBatches message, FriendlyByteBuf buf) {
        buf.writeUUID(message.copyUUID); // Write UUID to buffer
        buf.writeInt(message.numberOfPackets); // Write int to buffer
        buf.writeInt(message.position); // Write position to buffer
        buf.writeBytes(message.data);  // Write data to buffer
    }

    public static void handle(PacketSendPasteBatches message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack templateStack = container.getSlot(1).getItem();

            if (templateStack.isEmpty())
                return;

            if (templateStack.is(Items.PAPER)) {
                container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                templateStack = container.getSlot(1).getItem();
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
            boolean complete = bg2Data.addToPasteChunks(message.copyUUID, message.position, message.numberOfPackets, message.data);
            if (!complete)
                return;

            CompoundTag assembledTag = bg2Data.getAssembledTag(message.copyUUID);
            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(assembledTag);
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack, message.copyUUID);

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);
            PacketHandler.sendTo(new PacketSendCopyData(GadgetNBT.getUUID(templateStack), GadgetNBT.getCopyUUID(templateStack), tag), sender);

        });

        context.get().setPacketHandled(true);
    }
}

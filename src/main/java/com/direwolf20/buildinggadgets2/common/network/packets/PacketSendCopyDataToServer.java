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
import java.util.function.Supplier;

public class PacketSendCopyDataToServer {
    private CompoundTag tag;

    public PacketSendCopyDataToServer(CompoundTag tag) {
        this.tag = tag;
    }

    public static PacketSendCopyDataToServer decode(FriendlyByteBuf buf) {
        return new PacketSendCopyDataToServer(buf.readNbt());
    }

    public static void encode(PacketSendCopyDataToServer message, FriendlyByteBuf buf) {
        buf.writeNbt(message.tag);
    }

    public static void handle(PacketSendCopyDataToServer message, Supplier<NetworkEvent.Context> context) {
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

            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(message.tag);
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack);

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);
            PacketHandler.sendTo(new PacketSendCopyData(GadgetNBT.getUUID(templateStack), GadgetNBT.getCopyUUID(templateStack), tag), sender);
        });

        context.get().setPacketHandled(true);
    }
}

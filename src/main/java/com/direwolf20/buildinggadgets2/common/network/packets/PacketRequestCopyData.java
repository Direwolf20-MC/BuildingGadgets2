package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PacketRequestCopyData {
    private UUID gadgetUUID;

    public PacketRequestCopyData(UUID gadgetUUID) {
        this.gadgetUUID = gadgetUUID;
    }

    public static PacketRequestCopyData decode(FriendlyByteBuf buf) {
        return new PacketRequestCopyData(buf.readUUID());
    }

    public static void encode(PacketRequestCopyData message, FriendlyByteBuf buf) {
        buf.writeUUID(message.gadgetUUID);
    }

    public static void handle(PacketRequestCopyData message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }
            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCopyPaste actualGadget)) {
                return;
            }
            if (!GadgetNBT.getUUID(gadget).equals(message.gadgetUUID)) //This should almost never happen but lets confirm?
                return;

            BG2Data bg2Data = BG2Data.get(sender.level().getServer().overworld()); //TODO NPE?
            ListTag list = bg2Data.getCopyPasteListAsNBT(GadgetNBT.getUUID(gadget));
            CompoundTag tag = new CompoundTag();
            tag.put("stateposlist", list);
            PacketSendCopyData packetSendCopyData = new PacketSendCopyData(GadgetNBT.getUUID(gadget), GadgetNBT.getCopyUUID(gadget), tag);
            if (tag.sizeInBytes() > 2000000) {
                sender.displayClientMessage(Component.literal("Size too big for request! It was: " + tag.sizeInBytes()), false);
            } else {
                PacketHandler.sendTo(packetSendCopyData, sender);
                sender.displayClientMessage(Component.literal("Received request to send copy data for " + message.gadgetUUID), false);
            }
        });

        context.get().setPacketHandled(true);
    }
}

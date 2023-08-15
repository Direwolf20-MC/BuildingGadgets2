package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketRequestCopyData {
    private UUID gadgetUUID;
    private UUID copyUUID;

    public PacketRequestCopyData(UUID gadgetUUID, UUID copyUUID) {
        this.gadgetUUID = gadgetUUID;
        this.copyUUID = copyUUID;
    }

    public static PacketRequestCopyData decode(FriendlyByteBuf buf) {
        return new PacketRequestCopyData(buf.readUUID(), buf.readUUID());
    }

    public static void encode(PacketRequestCopyData message, FriendlyByteBuf buf) {
        buf.writeUUID(message.gadgetUUID);
        buf.writeUUID(message.copyUUID);
    }

    public static void handle(PacketRequestCopyData message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }
            //No longer testing these, since we can call it from the Template Manager
            /*ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste)) {
                return;
            }
            if (!GadgetNBT.getUUID(gadget).equals(message.gadgetUUID)) //This should almost never happen but lets confirm?
                return;*/

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(message.gadgetUUID, false);
            //Will bring this back if needed, but the block limit in place should make this obsolete
            /*FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            PacketSendCopyData packet = new PacketSendCopyData(GadgetNBT.getUUID(gadget), GadgetNBT.getCopyUUID(gadget), tag);
            PacketSendCopyData.encode(packet, buffer);
            int packetSize = buffer.writerIndex();
            if (tag.sizeInBytes() > 2000000) {
                sender.displayClientMessage(Component.literal("Size too big for request! It was: " + tag.sizeInBytes()), false);
            } else {
                sender.displayClientMessage(Component.literal("NBT Tag Size is: " + tag.sizeInBytes() + ". Packet size is: " + packetSize), false);
                PacketHandler.sendTo(new PacketSendCopyData(GadgetNBT.getUUID(gadget), GadgetNBT.getCopyUUID(gadget), tag), sender);
            }*/
            PacketHandler.sendTo(new PacketSendCopyData(message.gadgetUUID, message.copyUUID, tag), sender);
        });

        context.get().setPacketHandled(true);
    }
}

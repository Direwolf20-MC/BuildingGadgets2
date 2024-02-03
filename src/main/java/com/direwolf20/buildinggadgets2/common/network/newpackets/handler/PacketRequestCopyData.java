package com.direwolf20.buildinggadgets2.common.network.newpackets.handler;

import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.RequestCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;
import java.util.Optional;

public class PacketRequestCopyData {
    public static final PacketRequestCopyData INSTANCE = new PacketRequestCopyData();

    public static PacketRequestCopyData get() {
        return INSTANCE;
    }

    public void handle(final RequestCopyDataPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            Optional<Player> sender = context.player();
            if (sender.isEmpty()) {
                return;
            }

            //No longer testing these, since we can call it from the Template Manager
            /*ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste)) {
                return;
            }
            if (!GadgetNBT.getUUID(gadget).equals(message.gadgetUUID)) //This should almost never happen but lets confirm?
                return;*/

            if (ServerTickHandler.gadgetWorking(payload.gadgetUUID())) { //Todo Cut and Paste gadget only
                //System.out.println("Gadget still working!");
                return; //If the gadget is mid cut, don't sync data
            }

            var player = (ServerPlayer) sender.get();
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(payload.gadgetUUID(), false);
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

            player.connection.send(new SendCopyDataPayload(
                    payload.gadgetUUID(),
                    payload.copyUUID(),
                    tag
            ));
        });
    }
}

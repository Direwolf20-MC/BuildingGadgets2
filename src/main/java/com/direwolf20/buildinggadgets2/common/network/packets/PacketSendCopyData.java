package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketSendCopyData {
    private UUID gadgetUUID;
    private UUID copyUUID;
    private CompoundTag tag;

    public PacketSendCopyData(UUID gadgetUUID, UUID copyUUID, CompoundTag tag) {
        this.gadgetUUID = gadgetUUID;
        this.copyUUID = copyUUID;
        this.tag = tag;
    }

    public static void encode(PacketSendCopyData msg, FriendlyByteBuf buffer) {
        buffer.writeUUID(msg.gadgetUUID);
        buffer.writeUUID(msg.copyUUID);
        buffer.writeNbt(msg.tag);
    }

    public static PacketSendCopyData decode(FriendlyByteBuf buffer) {
        return new PacketSendCopyData(buffer.readUUID(), buffer.readUUID(), buffer.readNbt());
    }

    public static class Handler {
        public static void handle(PacketSendCopyData msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> clientPacketHandler(msg)));
            ctx.get().setPacketHandled(true);
        }
    }

    public static void clientPacketHandler(PacketSendCopyData msg) {
        UUID gadgetUUID = msg.gadgetUUID;
        UUID copyUUID = msg.copyUUID;
        ArrayList<StatePos> statePosList = BG2Data.statePosListFromNBTMapArray(msg.tag);
        BG2DataClient.updateLookupFromNBT(gadgetUUID, copyUUID, statePosList);
    }
}

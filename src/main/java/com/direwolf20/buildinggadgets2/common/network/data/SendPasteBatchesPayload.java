package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SendPasteBatchesPayload(
        UUID copyUUID,
        int numberOfPackets,
        int position,
        FriendlyByteBuf data
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "send_paste_batches");

    public SendPasteBatchesPayload(final FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readInt(), buffer.readInt(), new FriendlyByteBuf(buffer.readBytes(buffer.readableBytes())));
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(copyUUID());
        buffer.writeInt(numberOfPackets());
        buffer.writeInt(position());
        buffer.writeBytes(data);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

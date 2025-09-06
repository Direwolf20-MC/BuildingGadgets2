package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SendPastePayload(
        UUID copyUUID,
        CompoundTag tag
) implements CustomPacketPayload {
    public static final Type<SendPastePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "send_paste_payload"));

    @Override
    public Type<SendPastePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendPastePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendPastePayload::copyUUID,
            ByteBufCodecs.COMPOUND_TAG, SendPastePayload::tag,
            SendPastePayload::new
    );
}

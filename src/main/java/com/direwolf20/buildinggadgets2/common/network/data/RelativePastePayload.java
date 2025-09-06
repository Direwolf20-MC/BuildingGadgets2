package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RelativePastePayload(
        BlockPos relativePos
) implements CustomPacketPayload {
    public static final Type<RelativePastePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "relative_paste_payload"));

    @Override
    public Type<RelativePastePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, RelativePastePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RelativePastePayload::relativePos,
            RelativePastePayload::new
    );
}

package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RelativePastePayload(
        BlockPos relativePos
) implements CustomPacketPayload {
    public static final Type<RelativePastePayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "relative_paste_payload"));

    @Override
    public Type<RelativePastePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, RelativePastePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, RelativePastePayload::relativePos,
            RelativePastePayload::new
    );
}

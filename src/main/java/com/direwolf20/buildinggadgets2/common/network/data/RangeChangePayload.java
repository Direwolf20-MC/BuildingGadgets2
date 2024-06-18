package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RangeChangePayload(
        int range
) implements CustomPacketPayload {
    public static final Type<RangeChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "range_change_payload"));

    @Override
    public Type<RangeChangePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, RangeChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, RangeChangePayload::range,
            RangeChangePayload::new
    );
}

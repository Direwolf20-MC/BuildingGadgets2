package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RotatePayload(

) implements CustomPacketPayload {
    public static final RotatePayload INSTANCE = new RotatePayload();
    public static final Type<RotatePayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "rotate_payload"));

    @Override
    public Type<RotatePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, RotatePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}

package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RenderChangePayload(
        byte renderType
) implements CustomPacketPayload {
    public static final Type<RenderChangePayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "render_change_payload"));

    @Override
    public Type<RenderChangePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, RenderChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, RenderChangePayload::renderType,
            RenderChangePayload::new
    );
}

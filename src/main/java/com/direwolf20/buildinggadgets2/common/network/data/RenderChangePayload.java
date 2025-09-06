package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RenderChangePayload(
        byte renderType
) implements CustomPacketPayload {
    public static final Type<RenderChangePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "render_change_payload"));

    @Override
    public Type<RenderChangePayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, RenderChangePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, RenderChangePayload::renderType,
            RenderChangePayload::new
    );
}

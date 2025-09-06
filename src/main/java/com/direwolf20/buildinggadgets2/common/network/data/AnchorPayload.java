package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AnchorPayload(

) implements CustomPacketPayload {
    public static final AnchorPayload INSTANCE = new AnchorPayload();
    public static final Type<AnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "anchor_payload"));

    @Override
    public Type<AnchorPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, AnchorPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}


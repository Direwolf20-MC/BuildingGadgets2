package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CutPayload(

) implements CustomPacketPayload {
    public static final CutPayload INSTANCE = new CutPayload();
    public static final Type<CutPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "cut_payload"));

    @Override
    public Type<CutPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, CutPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}

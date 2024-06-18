package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UndoPayload(

) implements CustomPacketPayload {
    public static final UndoPayload INSTANCE = new UndoPayload();
    public static final Type<UndoPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "undo_payload"));

    @Override
    public Type<UndoPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, UndoPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);
}

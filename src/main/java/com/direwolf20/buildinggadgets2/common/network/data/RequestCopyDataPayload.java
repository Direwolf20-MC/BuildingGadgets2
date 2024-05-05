package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RequestCopyDataPayload(
        UUID gadgetUUID,
        UUID copyUUID
) implements CustomPacketPayload {
    public static final Type<RequestCopyDataPayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "request_copy_data"));

    @Override
    public Type<RequestCopyDataPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, RequestCopyDataPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, RequestCopyDataPayload::gadgetUUID,
            UUIDUtil.STREAM_CODEC, RequestCopyDataPayload::copyUUID,
            RequestCopyDataPayload::new
    );
}

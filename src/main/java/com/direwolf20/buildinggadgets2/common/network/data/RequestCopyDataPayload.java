package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
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
    public static final Type<RequestCopyDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "request_copy_data"));

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

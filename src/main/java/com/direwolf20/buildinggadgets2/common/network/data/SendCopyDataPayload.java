package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SendCopyDataPayload(
        UUID gadgetUUID,
        UUID copyUUID,
        CompoundTag tag
) implements CustomPacketPayload {
    public static final Type<SendCopyDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "send_copy_data_payload"));

    @Override
    public Type<SendCopyDataPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendCopyDataPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, SendCopyDataPayload::gadgetUUID,
            UUIDUtil.STREAM_CODEC, SendCopyDataPayload::copyUUID,
            ByteBufCodecs.COMPOUND_TAG, SendCopyDataPayload::tag,
            SendCopyDataPayload::new
    );
}

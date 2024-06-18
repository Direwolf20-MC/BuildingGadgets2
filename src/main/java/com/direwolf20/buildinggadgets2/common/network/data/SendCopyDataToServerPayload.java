package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SendCopyDataToServerPayload(
        CompoundTag compoundTag
) implements CustomPacketPayload {
    public static final Type<SendCopyDataToServerPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "send_copy_data_to_server_payload"));

    @Override
    public Type<SendCopyDataToServerPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, SendCopyDataToServerPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, SendCopyDataToServerPayload::compoundTag,
            SendCopyDataToServerPayload::new
    );
}

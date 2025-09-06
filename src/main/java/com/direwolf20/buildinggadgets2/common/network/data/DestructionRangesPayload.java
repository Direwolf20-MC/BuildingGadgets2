package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DestructionRangesPayload(
        int left, int right,
        int up, int down,
        int depth
) implements CustomPacketPayload {
    public static final Type<DestructionRangesPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "destruction_ranges_payload"));

    @Override
    public Type<DestructionRangesPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, DestructionRangesPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, DestructionRangesPayload::left,
            ByteBufCodecs.INT, DestructionRangesPayload::right,
            ByteBufCodecs.INT, DestructionRangesPayload::up,
            ByteBufCodecs.INT, DestructionRangesPayload::down,
            ByteBufCodecs.INT, DestructionRangesPayload::depth,
            DestructionRangesPayload::new
    );
}
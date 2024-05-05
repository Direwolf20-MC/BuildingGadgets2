package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
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
    public static final Type<DestructionRangesPayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "destruction_ranges_payload"));

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
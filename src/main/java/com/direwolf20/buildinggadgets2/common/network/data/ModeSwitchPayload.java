package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ModeSwitchPayload(
        boolean rotate,
        ResourceLocation modeId
) implements CustomPacketPayload {
    public static final Type<ModeSwitchPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "mode_switch_payload"));

    @Override
    public Type<ModeSwitchPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ModeSwitchPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ModeSwitchPayload::rotate,
            ResourceLocation.STREAM_CODEC, ModeSwitchPayload::modeId,
            ModeSwitchPayload::new
    );
}

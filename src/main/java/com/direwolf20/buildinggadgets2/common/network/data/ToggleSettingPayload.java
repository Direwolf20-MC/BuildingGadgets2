package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleSettingPayload(
        String setting
) implements CustomPacketPayload {
    public static final Type<ToggleSettingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "toggle_setting_payload"));

    @Override
    public Type<ToggleSettingPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, ToggleSettingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ToggleSettingPayload::setting,
            ToggleSettingPayload::new
    );
}

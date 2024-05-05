package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleSettingPayload(
        String setting
) implements CustomPacketPayload {
    public static final Type<ToggleSettingPayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "toggle_setting_payload"));

    @Override
    public Type<ToggleSettingPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, ToggleSettingPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ToggleSettingPayload::setting,
            ToggleSettingPayload::new
    );
}

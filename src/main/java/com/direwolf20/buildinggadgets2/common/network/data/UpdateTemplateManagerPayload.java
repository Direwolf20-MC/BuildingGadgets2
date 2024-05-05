package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateTemplateManagerPayload(
        BlockPos blockPos,
        int mode,
        String templateName
) implements CustomPacketPayload {
    public static final Type<UpdateTemplateManagerPayload> TYPE = new Type<>(new ResourceLocation(BuildingGadgets2.MODID, "update_template_manager_payload"));

    @Override
    public Type<UpdateTemplateManagerPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, UpdateTemplateManagerPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateTemplateManagerPayload::blockPos,
            ByteBufCodecs.INT, UpdateTemplateManagerPayload::mode,
            ByteBufCodecs.STRING_UTF8, UpdateTemplateManagerPayload::templateName,
            UpdateTemplateManagerPayload::new
    );
}

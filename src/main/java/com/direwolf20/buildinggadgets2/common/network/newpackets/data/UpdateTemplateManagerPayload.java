package com.direwolf20.buildinggadgets2.common.network.newpackets.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record UpdateTemplateManagerPayload(
        BlockPos blockPos,
        int mode,
        String templateName
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "update_template_manager");

    public UpdateTemplateManagerPayload(final FriendlyByteBuf buffer) {
        this(buffer.readBlockPos(), buffer.readInt(), buffer.readUtf());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(blockPos());
        buffer.writeInt(mode());
        buffer.writeUtf(templateName());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

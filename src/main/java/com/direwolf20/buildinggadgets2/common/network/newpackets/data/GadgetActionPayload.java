package com.direwolf20.buildinggadgets2.common.network.newpackets.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GadgetActionPayload(
        String actionName,
        CompoundTag metaData
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "gadget_action");

    public GadgetActionPayload(final FriendlyByteBuf buffer) {
        this(
                buffer.readUtf(),
                buffer.readNbt()
        );
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(actionName);
        buffer.writeNbt(metaData);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

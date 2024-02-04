package com.direwolf20.buildinggadgets2.common.network.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction.ActionGadget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GadgetActionPayload(
        ActionGadget actionName,
        CompoundTag metaData
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "gadget_action");

    public GadgetActionPayload(ActionGadget actionName) {
        this(actionName, new CompoundTag());
    }

    public GadgetActionPayload(final FriendlyByteBuf buffer) {
        this(
                ActionGadget.valueOf(buffer.readUtf()),
                buffer.readNbt()
        );
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(actionName.name());
        buffer.writeNbt(metaData);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

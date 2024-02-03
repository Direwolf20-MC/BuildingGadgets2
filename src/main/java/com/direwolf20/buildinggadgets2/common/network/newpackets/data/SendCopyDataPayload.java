package com.direwolf20.buildinggadgets2.common.network.newpackets.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record SendCopyDataPayload(
        UUID gadgetUUID,
        UUID copyUUID,
        CompoundTag tag
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "send_copy_data");
    public SendCopyDataPayload(final FriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readUUID(), buffer.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUUID(gadgetUUID());
        buffer.writeUUID(copyUUID());
        buffer.writeNbt(tag());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}

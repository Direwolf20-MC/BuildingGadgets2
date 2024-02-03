package com.direwolf20.buildinggadgets2.common.network.newpackets.data;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RequestCopyDataPayload(
    UUID gadgetUUID,
    UUID copyUUID
) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(BuildingGadgets2.MODID, "request_copy_data");

    public RequestCopyDataPayload(final FriendlyByteBuf pBuffer) {
        this(
            pBuffer.readUUID(),
            pBuffer.readUUID()
        );
    }

    @Override
    public void write(FriendlyByteBuf pBuffer) {
        pBuffer.writeUUID(gadgetUUID);
        pBuffer.writeUUID(copyUUID);
    }

    @Override
    public @NotNull ResourceLocation id() {
        return ID;
    }
}

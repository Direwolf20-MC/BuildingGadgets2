package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRenderChange {
    byte renderType;

    public PacketRenderChange(byte renderType) {
        this.renderType = renderType;
    }

    public static PacketRenderChange decode(FriendlyByteBuf buf) {
        return new PacketRenderChange(buf.readByte());
    }

    public static void encode(PacketRenderChange message, FriendlyByteBuf buf) {
        buf.writeByte(message.renderType);
    }

    public static void handle(PacketRenderChange message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty()) {
                return;
            }

            GadgetNBT.setRenderType(gadget, message.renderType);
            sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.render_set", Component.translatable(GadgetNBT.getRenderType(gadget).getLang())), true);
        });

        context.get().setPacketHandled(true);
    }
}

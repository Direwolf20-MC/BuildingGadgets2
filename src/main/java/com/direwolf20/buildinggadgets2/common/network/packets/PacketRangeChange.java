package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRangeChange {
    int range;

    public PacketRangeChange(int range) {
        this.range = range;
    }

    public static PacketRangeChange decode(FriendlyByteBuf buf) {
        return new PacketRangeChange(buf.readInt());
    }

    public static void encode(PacketRangeChange message, FriendlyByteBuf buf) {
        buf.writeInt(message.range);
    }

    public static void handle(PacketRangeChange message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
                return;
            }

            GadgetNBT.setToolRange(gadget, message.range);
            sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.range_set", message.range), true);
        });

        context.get().setPacketHandled(true);
    }
}

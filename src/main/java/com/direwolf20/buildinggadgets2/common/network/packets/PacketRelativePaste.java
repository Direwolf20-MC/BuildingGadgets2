package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketRelativePaste {
    int x;
    int y;
    int z;

    public PacketRelativePaste(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PacketRelativePaste decode(FriendlyByteBuf buf) {
        return new PacketRelativePaste(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void encode(PacketRelativePaste message, FriendlyByteBuf buf) {
        buf.writeInt(message.x);
        buf.writeInt(message.y);
        buf.writeInt(message.z);
    }

    public static void handle(PacketRelativePaste message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
                return;
            }
            BlockPos relativePos = new BlockPos(message.x, message.y, message.z);
            GadgetNBT.setRelativePaste(gadget, relativePos);
            sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.relativepaste", relativePos.toShortString()), true);
        });

        context.get().setPacketHandled(true);
    }
}

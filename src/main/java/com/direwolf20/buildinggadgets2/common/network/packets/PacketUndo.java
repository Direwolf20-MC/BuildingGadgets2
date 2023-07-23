package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketUndo {

    public PacketUndo() {
    }

    public static PacketUndo decode(FriendlyByteBuf buf) {
        return new PacketUndo();
    }

    public static void encode(PacketUndo message, FriendlyByteBuf buf) {
    }

    public static void handle(PacketUndo message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget) || gadget.getItem() instanceof GadgetCutPaste) {
                return;
            }

            actualGadget.undo(sender.level(), gadget);
        });

        context.get().setPacketHandled(true);
    }
}

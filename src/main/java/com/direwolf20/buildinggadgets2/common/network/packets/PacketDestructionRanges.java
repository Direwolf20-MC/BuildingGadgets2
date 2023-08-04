package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketDestructionRanges {
    int left, right, up, down, depth;

    public PacketDestructionRanges(int left, int right, int up, int down, int depth) {
        this.left = left;
        this.right = right;
        this.up = up;
        this.down = down;
        this.depth = depth;
    }

    public static PacketDestructionRanges decode(FriendlyByteBuf buf) {
        return new PacketDestructionRanges(buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void encode(PacketDestructionRanges message, FriendlyByteBuf buf) {
        buf.writeInt(message.left);
        buf.writeInt(message.right);
        buf.writeInt(message.up);
        buf.writeInt(message.down);
        buf.writeInt(message.depth);
    }

    public static void handle(PacketDestructionRanges message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetDestruction actualGadget)) {
                return;
            }
            GadgetNBT.setToolValue(gadget, message.left, "left");
            GadgetNBT.setToolValue(gadget, message.right, "right");
            GadgetNBT.setToolValue(gadget, message.up, "up");
            GadgetNBT.setToolValue(gadget, message.down, "down");
            GadgetNBT.setToolValue(gadget, message.depth, "depth");
            //sender.displayClientMessage(Component.literal("Destruction Gadget Ranges Updated"), true);
        });

        context.get().setPacketHandled(true);
    }
}

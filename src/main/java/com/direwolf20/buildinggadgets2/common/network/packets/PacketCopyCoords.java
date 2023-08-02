package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * TODO: remove me, this is a tmp
 */
public class PacketCopyCoords {
    BlockPos startPos;
    BlockPos endPos;

    public PacketCopyCoords(BlockPos startPos, BlockPos endPos) {
        this.startPos = startPos;
        this.endPos = endPos;
    }

    public static PacketCopyCoords decode(FriendlyByteBuf buf) {
        return new PacketCopyCoords(buf.readBlockPos(), buf.readBlockPos());
    }

    public static void encode(PacketCopyCoords message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.startPos);
        buf.writeBlockPos(message.endPos);
    }

    public static void handle(PacketCopyCoords message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
                return;
            }

            GadgetNBT.setCopyStartPos(gadget, message.startPos);
            GadgetNBT.setCopyEndPos(gadget, message.endPos);
            BlockHitResult lookingAt = VectorHelper.getLookingAt(sender, gadget);
            ItemActionContext itemContext = new ItemActionContext(lookingAt.getBlockPos(), lookingAt, sender, sender.level(), InteractionHand.MAIN_HAND, gadget);
            if (gadget.getItem() instanceof GadgetCopyPaste gadgetCopyPaste)
                gadgetCopyPaste.buildAndStore(itemContext, gadget);
            sender.displayClientMessage(Component.literal("Copy set to " + message.startPos + ":" + message.endPos), true);
        });

        context.get().setPacketHandled(true);
    }
}

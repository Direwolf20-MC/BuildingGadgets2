package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class PacketAnchor {
    public PacketAnchor() {
    }

    public static PacketAnchor decode(FriendlyByteBuf buf) {
        return new PacketAnchor();
    }

    public static void encode(PacketAnchor message, FriendlyByteBuf buf) {

    }

    public static void handle(PacketAnchor message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            ItemStack gadget = BaseGadget.getGadget(sender);
            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
                return;
            }
            BlockHitResult lookingAt = VectorHelper.getLookingAt(sender, gadget);
            BlockPos lookingAtPos = lookingAt.getBlockPos();
            BlockState lookingAtState = sender.level().getBlockState(lookingAtPos);
            if (!GadgetNBT.getAnchorPos(gadget).equals(GadgetNBT.nullPos)) {
                GadgetNBT.clearAnchorPos(gadget);
                sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorcleared"), true);
            } else {
                if (lookingAtState.isAir()) return;
                GadgetNBT.setAnchorPos(gadget, lookingAtPos);
                GadgetNBT.setAnchorSide(gadget, lookingAt.getDirection());
                if (gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) {
                    BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
                    if (renderBlockState.isAir()) return;
                    BaseMode mode = GadgetNBT.getMode(gadget);
                    ArrayList<StatePos> buildList = mode.collect(lookingAt.getDirection(), sender, lookingAtPos, renderBlockState);
                    ArrayList<BlockPos> blockPosList = new ArrayList<>();
                    buildList.forEach(e -> blockPosList.add(e.pos));
                    GadgetNBT.setAnchorList(gadget, blockPosList);
                }
                sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorset").append(lookingAtPos.toShortString()), true);
            }
        });

        context.get().setPacketHandled(true);
    }
}

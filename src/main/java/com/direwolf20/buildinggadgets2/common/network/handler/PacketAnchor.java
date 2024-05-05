package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets2.common.network.data.AnchorPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;

public class PacketAnchor {
    public static final PacketAnchor INSTANCE = new PacketAnchor();

    public static PacketAnchor get() {
        return INSTANCE;
    }

    public void handle(final AnchorPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            // If the anchor is already set, clear it
            if (!GadgetNBT.getAnchorPos(gadgetStack).equals(GadgetNBT.nullPos)) {
                GadgetNBT.clearAnchorPos(gadgetStack);
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorcleared"), true);
                return;
            }

            BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadgetStack);
            BlockPos lookingAtPos = lookingAt.getBlockPos();
            BlockState lookingAtState = player.level().getBlockState(lookingAtPos);
            if (lookingAtState.isAir()) return;
            GadgetNBT.setAnchorPos(gadgetStack, lookingAtPos);
            GadgetNBT.setAnchorSide(gadgetStack, lookingAt.getDirection());
            if (gadgetStack.getItem() instanceof GadgetBuilding || gadgetStack.getItem() instanceof GadgetExchanger) {
                BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadgetStack);
                if (renderBlockState.isAir()) return;
                BaseMode mode = GadgetNBT.getMode(gadgetStack);
                ArrayList<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAtPos, renderBlockState);
                ArrayList<BlockPos> blockPosList = new ArrayList<>();
                buildList.forEach(e -> blockPosList.add(e.pos));
                GadgetNBT.setAnchorList(gadgetStack, blockPosList);
            }
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorset").append(lookingAtPos.toShortString()), true);
        });
    }
}

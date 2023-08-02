package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.client.screen.DestructionGUI;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;

public class GadgetDestruction extends BaseGadget {
    public GadgetDestruction() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.DESTRUCTIONGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.DESTRUCTIONGADGET_COST.get();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gadget = player.getItemInHand(hand);

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        ItemActionContext context = new ItemActionContext(lookingAt.getBlockPos(), lookingAt, player, level, hand, gadget);

        if (player.isShiftKeyDown()) {
            return this.onShiftAction(context);
        }

        if (level.isClientSide()) //No client
            return InteractionResultHolder.success(gadget);

        return this.onAction(context);
    }

    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        BlockPos anchor = GadgetNBT.getAnchorPos(gadget);
        Direction anchorSide = GadgetNBT.getAnchorSide(gadget);

        if (context.level().getBlockState(VectorHelper.getLookingAt(context.player(), gadget).getBlockPos()) == Blocks.AIR.defaultBlockState() && anchor == null)
            return InteractionResultHolder.pass(gadget);

        BlockPos startBlock = getHitPos(context);
        Direction facing = (anchorSide == null) ? context.hitResult().getDirection() : anchorSide;

        ArrayList<StatePos> destroyList = GadgetUtils.getDestructionArea(context.level(), startBlock, facing, context.player(), gadget);
        ArrayList<BlockPos> destroyPosList = new ArrayList<>();
        destroyList.forEach(e -> destroyPosList.add(e.pos));
        ArrayList<StatePos> actuallyDestroyedList = BuildingUtils.remove(context.level(), context.player(), destroyPosList, false, true, gadget);
        if (!actuallyDestroyedList.isEmpty()) {
            GadgetUtils.addToUndoList(context.level(), gadget, actuallyDestroyedList); //If we placed anything at all, add to the undoList
            GadgetNBT.clearAnchorPos(gadget);
        }
        return InteractionResultHolder.success(gadget);
    }

    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        if (context.level().isClientSide)
            Minecraft.getInstance().setScreen(new DestructionGUI(context.stack(), false));

        return super.onShiftAction(context);
    }

    /**
     * Undo is handled differently for destroyer - since we voided the blocks we removed we can just return them for free
     */
    @Override
    public void undo(Level level, Player player, ItemStack gadget) {
        BG2Data bg2Data = BG2Data.get(level.getServer().overworld()); //TODO NPE?
        ArrayList<StatePos> undoList = bg2Data.getUndoList(GadgetNBT.popUndoList(gadget));
        if (undoList.isEmpty()) return;

        byte drawSize = 40;
        for (StatePos pos : undoList) {
            if (pos.state.isAir()) continue; //Since we store air now

            BlockState oldState = level.getBlockState(pos.pos);
            if (!oldState.canBeReplaced() && !(oldState.getBlock() instanceof RenderBlock))
                continue; //Don't overwrite any blocks that have been placed since destroying - only air or replacables like grass/water.

            if ((oldState.getBlock() instanceof RenderBlock)) {
                BlockEntity blockEntity = level.getBlockEntity(pos.pos);
                if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                    drawSize = renderBlockBE.drawSize;
                    renderBlockBE.setRenderData(Blocks.AIR.defaultBlockState(), pos.state);
                    renderBlockBE.drawSize = drawSize;
                }
            } else {
                boolean placed = level.setBlockAndUpdate(pos.pos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(pos.pos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                be.setRenderData(oldState, pos.state);
            }
        }
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.DESTRUCTION;
    }
}

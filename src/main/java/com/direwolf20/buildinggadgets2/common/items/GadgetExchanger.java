package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class GadgetExchanger extends BaseGadget {
    public GadgetExchanger() {
        super();
    }

    /**
     * TODO: Placeholder Class for now.
     */
    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        BlockState setState = GadgetNBT.getGadgetBlockState(gadget);
        if (setState.isAir()) return InteractionResultHolder.pass(gadget);

        var mode = GadgetNBT.getMode(gadget);
        ArrayList<StatePos> buildList = mode.collect(context.hitResult().getDirection(), context.player(), getHitPos(context), setState);

        // This should go through some translation based process
        // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
        ArrayList<StatePos> actuallyBuiltList = BuildingUtils.exchange(context.level(), context.player(), buildList, getHitPos(context));
        if (!actuallyBuiltList.isEmpty()) {
            GadgetNBT.clearAnchorPos(gadget);
            GadgetUtils.addToUndoList(context.level(), gadget, actuallyBuiltList); //If we placed anything at all, add to the undoList
        }
        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        BlockState blockState = context.level().getBlockState(context.pos());
        if (!GadgetUtils.isValidBlockState(blockState, context.level(), context.pos()))
            return super.onShiftAction(context);
        if (GadgetUtils.setBlockState(context.stack(), blockState))
            return InteractionResultHolder.success(context.stack());

        return super.onShiftAction(context);
    }

    /**
     * Undo is handled differently for exchanger - we want to look at what the blocks that were replaced were previously, and grab them from the players inventory to undo it.
     */
    @Override
    public void undo(Level level, Player player, ItemStack gadget) {
        BG2Data bg2Data = BG2Data.get(level.getServer().overworld()); //TODO NPE?
        ArrayList<StatePos> undoList = bg2Data.getUndoList(GadgetNBT.popUndoList(gadget));
        if (undoList.isEmpty()) return;

        Inventory playerInventory = player.getInventory();
        int lastSlot = -1;

        for (StatePos pos : undoList) {
            if (pos.state.isAir()) continue; //Since we store air now
            if (!player.isCreative()) {
                lastSlot = BuildingUtils.findItemStack(playerInventory, GadgetUtils.getItemForBlock(pos.state));
                if (lastSlot == -1) continue;
            }
            BlockState oldState = level.getBlockState(pos.pos);
            boolean placed = level.setBlockAndUpdate(pos.pos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(pos.pos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            if (!player.isCreative()) {
                playerInventory.getItem(lastSlot).shrink(1);
                ItemStack returnedItem = GadgetUtils.getItemForBlock(oldState);
                BuildingUtils.giveItemToPlayer(player, returnedItem);
            }
            be.setRenderData(oldState, pos.state);
        }
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.EXCHANGING;
    }
}

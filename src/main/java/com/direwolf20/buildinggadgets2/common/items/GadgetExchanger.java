package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class GadgetExchanger extends BaseGadget {
    public GadgetExchanger() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.EXCHANGINGGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.EXCHANGINGGADGET_COST.get();
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
        ArrayList<StatePos> actuallyBuiltList = BuildingUtils.exchange(context.level(), context.player(), buildList, getHitPos(context), gadget);
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
        if (!GadgetUtils.isValidBlockState(blockState, context.level(), context.pos())) {
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.invalidblock"), true);
            return super.onShiftAction(context);
        }
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
        byte drawSize = 40;

        Inventory playerInventory = player.getInventory();

        for (StatePos pos : undoList) {
            if (pos.state.isAir()) continue; //Since we store air now
            if (!pos.state.canSurvive(level, pos.pos)) continue;
            boolean foundStacks = false;
            List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, pos.pos, pos.state);
            if (!player.isCreative()) {
                foundStacks = BuildingUtils.removeStacksFromInventory(playerInventory, neededItems, true);
                if (!foundStacks) continue;
            }
            boolean placed = false;
            BlockState oldState = level.getBlockState(pos.pos);
            BlockState oldRenderState = level.getBlockState(pos.pos);
            if (oldState.getBlock() instanceof RenderBlock) {
                BlockEntity blockEntity = level.getBlockEntity(pos.pos);
                if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                    oldState = renderBlockBE.targetBlock;
                    oldRenderState = renderBlockBE.renderBlock;
                    drawSize = renderBlockBE.drawSize;
                    placed = true;
                }
            } else {
                placed = level.setBlockAndUpdate(pos.pos, Registration.RenderBlock.get().defaultBlockState());
            }
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(pos.pos);
            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            if (!player.isCreative()) {
                BuildingUtils.removeStacksFromInventory(playerInventory, neededItems, false);
                List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, pos.pos, oldState, gadget);
                for (ItemStack returnedItem : returnedItems)
                    BuildingUtils.giveItemToPlayer(player, returnedItem);
            }
            if (oldRenderState.equals(pos.state))
                be.setRenderData(Blocks.AIR.defaultBlockState(), pos.state);
            else
                be.setRenderData(oldState, pos.state);
            be.drawSize = drawSize;
        }
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.EXCHANGING;
    }

    /**
     * For Silk Touch
     */
    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 3;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return EnchantmentHelper.getEnchantments(book).containsKey(Enchantments.SILK_TOUCH) || super.isBookEnchantable(stack, book);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment == Enchantments.SILK_TOUCH || super.canApplyAtEnchantingTable(stack, enchantment);
    }
}

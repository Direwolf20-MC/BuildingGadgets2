package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuildingUtils {
    public static int lastSLot = -1;

    public static int findItemStack(Inventory playerInventory, ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.is(Items.AIR)) return -1;
        //Todo Bag support
        if (lastSLot != -1) {
            ItemStack slotStack = playerInventory.getItem(lastSLot);
            if (ItemStack.isSameItem(slotStack, itemStack)) //Todo validate proper comparison
                return lastSLot;
        }
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            if (ItemStack.isSameItem(slotStack, itemStack)) //Todo validate proper comparison
                return i;
        }
        return -1;
    }

    public static void giveItemToPlayer(Player player, ItemStack returnedItem) {
        if (!player.addItem(returnedItem)) {
            BlockPos dropPos = player.getOnPos();
            ItemEntity itementity = new ItemEntity(player.level(), dropPos.getX(), dropPos.getY(), dropPos.getZ(), returnedItem);
            itementity.setPickUpDelay(40);
            player.level().addFreshEntity(itementity);
        }
    }

    public static ArrayList<StatePos> build(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        Inventory playerInventory = player.getInventory();
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            if (!player.isCreative()) {
                lastSLot = findItemStack(playerInventory, GadgetUtils.getItemForBlock(pos.state));
                if (lastSLot == -1) continue;
            }
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (level.getBlockState(blockPos).canBeReplaced()) {
                boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                if (!player.isCreative())
                    playerInventory.getItem(lastSLot).shrink(1);
                actuallyBuiltList.add(new StatePos(pos.state, blockPos));
                be.setRenderData(Blocks.AIR.defaultBlockState(), pos.state);
            }
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> exchange(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        Inventory playerInventory = player.getInventory();
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            if (!player.isCreative()) {
                lastSLot = findItemStack(playerInventory, GadgetUtils.getItemForBlock(pos.state));
                if (lastSLot == -1) continue;
            }
            BlockPos blockPos = pos.pos.offset(lookingAt);
            BlockState oldState = level.getBlockState(blockPos);
            boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            if (!player.isCreative()) {
                playerInventory.getItem(lastSLot).shrink(1);
                ItemStack returnedItem = GadgetUtils.getItemForBlock(oldState);
                giveItemToPlayer(player, returnedItem);
            }
            actuallyBuiltList.add(new StatePos(pos.state, blockPos));
            be.setRenderData(oldState, pos.state);
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> buildWithTileData(Level level, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ArrayList<TagPos> teData) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        if (teData == null) return actuallyBuiltList;
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (level.getBlockState(blockPos).canBeReplaced()) {
                Optional<TagPos> foundTagPos = teData.stream()
                        .filter(tagPos -> tagPos.pos.equals(pos.pos))
                        .findFirst();
                boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                actuallyBuiltList.add(new StatePos(pos.state, blockPos));
                be.setRenderData(Blocks.AIR.defaultBlockState(), pos.state);
                if (foundTagPos.isPresent()) {
                    TagPos result = foundTagPos.get();
                    be.setBlockEntityData(result.tag);
                }
            }
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> remove(Level level, Player player, List<BlockPos> blockPosList, boolean giveItem) {
        ArrayList<StatePos> affectedBlocks = new ArrayList<>();
        byte drawSize = 40;
        for (BlockPos pos : blockPosList) {
            BlockState oldState = level.getBlockState(pos);
            if (oldState.isAir()) continue;
            if (oldState.getBlock() instanceof RenderBlock) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                    oldState = renderBlockBE.renderBlock;
                    drawSize = renderBlockBE.drawSize;
                }
            }
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 48);
            affectedBlocks.add(new StatePos(oldState, pos));
            if (giveItem) {
                ItemStack returnedItem = GadgetUtils.getItemForBlock(oldState);
                giveItemToPlayer(player, returnedItem);
            }
        }

        for (StatePos affectedBlock : affectedBlocks) {
            boolean placed = level.setBlock(affectedBlock.pos, Registration.RenderBlock.get().defaultBlockState(), 3);
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(affectedBlock.pos);
            if (placed && be != null) {
                be.setRenderData(affectedBlock.state, Blocks.AIR.defaultBlockState());
                be.drawSize = drawSize;
            }
        }
        return affectedBlocks;
    }
}

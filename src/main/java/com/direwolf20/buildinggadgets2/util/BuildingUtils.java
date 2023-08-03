package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BuildingUtils {

    public static boolean removeStacksFromInventory(Inventory playerInventory, List<ItemStack> itemStacks, boolean simulate) {
        if (itemStacks.isEmpty() || itemStacks.contains(Items.AIR.getDefaultInstance())) return false;
        //Todo Bag support
        ArrayList<ItemStack> testArray = new ArrayList<>(itemStacks);
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            Optional<ItemStack> matchStack = itemStacks.stream().filter(e -> ItemStack.isSameItem(e, slotStack) && slotStack.getCount() >= e.getCount()).findFirst();
            if (matchStack.isPresent()) { //Todo validate proper comparison, support multiple stacks of same item
                ItemStack matchingStack = matchStack.get();
                if (!simulate)
                    slotStack.shrink(matchingStack.getCount());
                testArray.remove(matchingStack);
                if (testArray.isEmpty()) return true;
            }
        }
        return false;
    }

    public static int countItemStacks(Inventory playerInventory, ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.is(Items.AIR)) return 0;
        int counter = 0;
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            if (ItemStack.isSameItem(slotStack, itemStack)) //Todo validate proper comparison
                counter += slotStack.getCount();
        }
        return counter;
    }

    public static void giveItemToPlayer(Player player, ItemStack returnedItem) {
        if (!player.addItem(returnedItem)) {
            BlockPos dropPos = player.getOnPos();
            ItemEntity itementity = new ItemEntity(player.level(), dropPos.getX(), dropPos.getY(), dropPos.getZ(), returnedItem);
            itementity.setPickUpDelay(40);
            player.level().addFreshEntity(itementity);
        }
    }

    public static int getEnergyStored(ItemStack gadget) {
        if (gadget.getItem() instanceof BaseGadget baseGadget) {
            IEnergyStorage energy = gadget.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
            return energy.getEnergyStored();
        }
        return 0;
    }

    public static int getEnergyCost(ItemStack gadget) {
        if (gadget.getItem() instanceof BaseGadget baseGadget) {
            return baseGadget.getEnergyCost();
        }
        return -1;
    }

    public static boolean hasEnoughEnergy(ItemStack gadget) {
        int energyStored = getEnergyStored(gadget);
        int energyCost = getEnergyCost(gadget);
        return energyCost <= energyStored;
    }

    public static boolean hasEnoughEnergy(ItemStack gadget, int cost) {
        int energyStored = getEnergyStored(gadget);
        return cost <= energyStored;
    }

    public static void useEnergy(ItemStack gadget) {
        if (gadget.getItem() instanceof BaseGadget baseGadget) {
            IEnergyStorage energy = gadget.getCapability(ForgeCapabilities.ENERGY, null).orElse(null);
            int cost = baseGadget.getEnergyCost();
            energy.extractEnergy(cost, false);
        }
    }

    public static ArrayList<StatePos> build(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ItemStack gadget) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        Inventory playerInventory = player.getInventory();
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (!pos.state.canSurvive(level, blockPos)) continue;
            boolean foundStacks = false;
            List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, pos.pos, pos.state);
            if (!player.isCreative()) {
                if (!hasEnoughEnergy(gadget)) break; //Break out if we're out of power
                foundStacks = removeStacksFromInventory(playerInventory, neededItems, true);
                if (!foundStacks) continue;
            }

            if (level.getBlockState(blockPos).canBeReplaced()) {
                boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                if (!player.isCreative()) {
                    useEnergy(gadget);
                    removeStacksFromInventory(playerInventory, neededItems, false);
                }
                actuallyBuiltList.add(new StatePos(pos.state, blockPos));
                be.setRenderData(Blocks.AIR.defaultBlockState(), pos.state);
            }
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> exchange(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ItemStack gadget) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        Inventory playerInventory = player.getInventory();
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos.offset(lookingAt);
            //if (pos.state.isAir()) continue; //Since we store air now
            if (!pos.state.canSurvive(level, blockPos)) continue;
            boolean foundStacks = false;
            List<ItemStack> neededItems = new ArrayList<>();
            if (!player.isCreative()) {
                if (!hasEnoughEnergy(gadget)) break; //Break out if we're out of power
                if (!pos.state.isAir()) {
                    neededItems.addAll(GadgetUtils.getDropsForBlockState((ServerLevel) level, pos.pos, pos.state));
                    foundStacks = removeStacksFromInventory(playerInventory, neededItems, true);
                    if (!foundStacks) continue;
                }
            }
            BlockState oldState = level.getBlockState(blockPos);
            boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            if (!player.isCreative()) {
                useEnergy(gadget);
                if (!pos.state.isAir()) {
                    removeStacksFromInventory(playerInventory, neededItems, false);
                    List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, oldState, gadget);
                    for (ItemStack returnedItem : returnedItems)
                        giveItemToPlayer(player, returnedItem);
                }
            }
            actuallyBuiltList.add(new StatePos(oldState, blockPos)); //For undo purposes we track what the OLD state was here, so we can put it back with Undo
            be.setRenderData(oldState, pos.state);
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> buildWithTileData(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ArrayList<TagPos> teData, ItemStack gadget) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        if (teData == null) return actuallyBuiltList;

        boolean replace = GadgetNBT.getPasteReplace(gadget);
        if (!replace)
            actuallyBuiltList = BuildingUtils.build(level, player, blockPosList, lookingAt, gadget);
        else
            actuallyBuiltList = BuildingUtils.exchange(level, player, blockPosList, lookingAt, gadget);

        for (TagPos tagPos : teData) {
            BlockPos blockPos = tagPos.pos.offset(lookingAt);
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);
            if (be == null) {
                continue; //Shouldn't Happen!
            }
            be.setBlockEntityData(tagPos.tag);
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> remove(Level level, Player player, List<BlockPos> blockPosList, boolean giveItem, boolean dropContents, ItemStack gadget) {
        ArrayList<StatePos> affectedBlocks = new ArrayList<>();
        byte drawSize = 40;
        for (BlockPos pos : blockPosList) {
            if (!player.isCreative()) {
                if (!hasEnoughEnergy(gadget)) break; //Break out if we're out of power
            }
            BlockState oldState = level.getBlockState(pos);
            if (oldState.isAir() || !GadgetUtils.isValidBlockState(oldState, level, pos)) continue;
            if (oldState.getBlock() instanceof RenderBlock) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                    oldState = renderBlockBE.renderBlock;
                    drawSize = renderBlockBE.drawSize;
                }
            }
            if (!dropContents)
                level.removeBlockEntity(pos); //Calling this prevents chests from dropping their contents, so only do it if we don't care about the drops (Like cut)
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 48);
            affectedBlocks.add(new StatePos(oldState, pos));
            if (!player.isCreative())
                useEnergy(gadget);
            if (giveItem) {
                List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, pos, oldState, gadget);
                for (ItemStack returnedItem : returnedItems)
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

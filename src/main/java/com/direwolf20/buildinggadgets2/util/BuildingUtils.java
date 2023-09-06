package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.integration.CuriosIntegration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.*;

public class BuildingUtils {

    public static void checkHandlerForItems(IItemHandler handler, List<ItemStack> testArray, boolean simulate) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack itemInSlot = handler.getStackInSlot(j);
            LazyOptional<IItemHandler> itemStackCapability = itemInSlot.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (itemStackCapability.isPresent()) {
                IItemHandler slotHandler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                checkHandlerForItems(slotHandler, testArray, simulate);
                if (testArray.isEmpty()) break;
            } else {
                Optional<ItemStack> matchStack = testArray.stream().filter(e -> ItemStack.isSameItem(e, itemInSlot) && itemInSlot.getCount() >= e.getCount()).findFirst();
                if (matchStack.isPresent()) { //Todo: Support multiple stacks of same item
                    ItemStack matchingStack = matchStack.get();
                    handler.extractItem(j, matchingStack.getCount(), simulate);
                    testArray.remove(matchingStack);
                }
            }
            if (testArray.isEmpty()) break;
        }
    }

    public static void checkInventoryForItems(Inventory inventory, List<ItemStack> testArray, boolean simulate) {
        for (int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack itemInSlot = inventory.getItem(j);
            LazyOptional<IItemHandler> itemStackCapability = itemInSlot.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (itemStackCapability.isPresent()) {
                IItemHandler slotHandler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                checkHandlerForItems(slotHandler, testArray, simulate);
                if (testArray.isEmpty()) break;
            } else {
                Optional<ItemStack> matchStack = testArray.stream().filter(e -> ItemStack.isSameItem(e, itemInSlot) && itemInSlot.getCount() >= e.getCount()).findFirst();
                if (matchStack.isPresent()) { //Todo: Support multiple stacks of same item
                    ItemStack matchingStack = matchStack.get();
                    if (!simulate)
                        itemInSlot.shrink(matchingStack.getCount());
                    testArray.remove(matchingStack);
                }
            }
            if (testArray.isEmpty()) break;
        }
    }

    public static IItemHandler getHandlerFromBound(Player player, DimBlockPos boundInventory, Direction direction) {
        Level level = boundInventory.getLevel(player.getServer());
        if (level == null) return null;
        BlockEntity blockEntity = level.getBlockEntity(boundInventory.blockPos);
        if (blockEntity == null) return null;
        LazyOptional<IItemHandler> handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction);
        if (handler.isPresent()) {
            return handler.resolve().get();
        }
        return null;
    }

    public static boolean removeStacksFromInventory(Player player, List<ItemStack> itemStacks, boolean simulate, DimBlockPos boundInventory, Direction direction) {
        if (itemStacks.isEmpty() || itemStacks.contains(Items.AIR.getDefaultInstance())) return false;
        ArrayList<ItemStack> testArray = new ArrayList<>(itemStacks);
        //Check Bound Inventory First
        if (boundInventory != null) {
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                checkHandlerForItems(boundHandler, testArray, simulate);
            }
        }

        if (testArray.isEmpty()) return true;
        //Check curious slots second:
        if (CuriosIntegration.isLoaded()) {
            LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
            if (curiosOpt.isPresent()) {
                curiosOpt.resolve().get().getCurios().forEach((id, stackHandler) -> {
                    for (int j = 0; j < stackHandler.getSlots(); j++) {
                        ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(j);
                        LazyOptional<IItemHandler> itemStackCapability = itemInSlot.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                        if (itemStackCapability.isPresent()) {
                            IItemHandler slotHandler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                            checkHandlerForItems(slotHandler, testArray, simulate);
                            if (testArray.isEmpty()) break;
                        }
                    }
                });
            }
        }
        if (testArray.isEmpty()) return true;

        Inventory playerInventory = player.getInventory();
        checkInventoryForItems(playerInventory, testArray, simulate);
        if (testArray.isEmpty()) return true;
        return false;
    }

    public static int countItemStacks(Player player, ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.is(Items.AIR)) return 0;
        Inventory playerInventory = player.getInventory();
        final int[] counter = {0};

        //Check curious slots first:
        if (CuriosIntegration.isLoaded()) {
            LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
            if (curiosOpt.isPresent()) {
                curiosOpt.resolve().get().getCurios().forEach((id, stackHandler) -> {
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                        LazyOptional<IItemHandler> itemStackCapability = itemInSlot.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                        if (itemStackCapability.isPresent()) {
                            IItemHandler slotHandler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                            for (int j = 0; j < slotHandler.getSlots(); j++) {
                                ItemStack itemInBagSlot = slotHandler.getStackInSlot(j);
                                if (ItemStack.isSameItem(itemInBagSlot, itemStack))
                                    counter[0] += itemInBagSlot.getCount();
                            }
                        }
                    }
                });
            }
        }

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            LazyOptional<IItemHandler> itemStackCapability = slotStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (itemStackCapability.isPresent()) {
                IItemHandler handler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                for (int j = 0; j < handler.getSlots(); j++) {
                    ItemStack itemInSlot = handler.getStackInSlot(j);
                    if (ItemStack.isSameItem(itemInSlot, itemStack))
                        counter[0] += itemInSlot.getCount();
                }
            } else {
                if (ItemStack.isSameItem(slotStack, itemStack))
                    counter[0] += slotStack.getCount();
            }
        }
        return counter[0];
    }

    public static void giveItemToPlayer(Player player, ItemStack returnedItem, DimBlockPos boundInventory, Direction direction) {
        //Check Bound Inventory First
        ItemStack tempReturnedItem = returnedItem.copy();
        if (boundInventory != null) {
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                tempReturnedItem = ItemHandlerHelper.insertItemStacked(boundHandler, returnedItem, false);
            }
        }
        if (tempReturnedItem.isEmpty()) return;
        ItemStack realReturnedItem = tempReturnedItem.copy();

        //Look for matching itemstacks inside curios inventories second - if found, insert there!
        if (CuriosIntegration.isLoaded()) {
            LazyOptional<ICuriosItemHandler> curiosOpt = CuriosApi.getCuriosHelper().getCuriosHandler(player);
            if (curiosOpt.isPresent()) {
                curiosOpt.resolve().get().getCurios().forEach((id, stackHandler) -> {
                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                        LazyOptional<IItemHandler> itemStackCapability = itemInSlot.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
                        if (itemStackCapability.isPresent()) {
                            IItemHandler slotHandler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                            for (int j = 0; j < slotHandler.getSlots(); j++) {
                                ItemStack itemInBagSlot = slotHandler.getStackInSlot(j);
                                if (ItemStack.isSameItem(itemInBagSlot, realReturnedItem))
                                    slotHandler.insertItem(j, realReturnedItem.split(slotHandler.getSlotLimit(i) - itemInBagSlot.getCount()), false);
                                if (realReturnedItem.isEmpty()) return;
                            }
                        }
                    }
                });
            }
            if (realReturnedItem.isEmpty()) return;
        }
        //Now look for bags inside the players inventory
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            LazyOptional<IItemHandler> itemStackCapability = slotStack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
            if (itemStackCapability.isPresent()) {
                IItemHandler handler = itemStackCapability.orElseThrow(IllegalStateException::new); // This should never throw
                for (int j = 0; j < handler.getSlots(); j++) {
                    ItemStack itemInSlot = handler.getStackInSlot(j);
                    if (ItemStack.isSameItem(itemInSlot, realReturnedItem))
                        handler.insertItem(j, realReturnedItem.split(handler.getSlotLimit(i) - itemInSlot.getCount()), false);
                    if (realReturnedItem.isEmpty()) break;
                }
            }
        }
        if (realReturnedItem.isEmpty()) return;

        //Finally just give it to the player already!
        if (!player.addItem(realReturnedItem)) {
            BlockPos dropPos = player.getOnPos();
            ItemEntity itementity = new ItemEntity(player.level(), dropPos.getX(), dropPos.getY(), dropPos.getZ(), realReturnedItem);
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

    public static UUID build(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ItemStack gadget, boolean needItems) {
        UUID buildUUID = UUID.randomUUID();
        FakeRenderingWorld fakeRenderingWorld = new FakeRenderingWorld(level, blockPosList, lookingAt);
        DimBlockPos boundPos = GadgetNBT.getBoundPos(gadget);
        int dir = boundPos == null ? -1 : GadgetNBT.getToolValue(gadget, "binddirection");
        Direction direction = dir == -1 ? null : Direction.values()[dir];
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            BlockPos blockPos = pos.pos;
            if (!level.mayInteract(player, blockPos)) continue; //Chunk Protection like spawn and FTB Utils
            if (gadget.getItem() instanceof GadgetBuilding && needItems && !pos.state.canSurvive(level, blockPos.offset(lookingAt)))
                continue; //Don't do this validation for copy/paste
            List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos.offset(lookingAt), pos.state, player);
            if (!player.isCreative() && needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                if (!removeStacksFromInventory(player, neededItems, true, boundPos, direction))
                    continue; //Continue to the next position
            }
            if (!player.isCreative() && !hasEnoughEnergy(gadget)) {
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.outofpower"), true);
                break; //Break out if we're out of power
            }
            if (!player.isCreative()) {
                useEnergy(gadget);
            }
            ServerTickHandler.addToMap(buildUUID, new StatePos(fakeRenderingWorld.getBlockStateWithoutReal(pos.pos), pos.pos), level, GadgetNBT.getRenderTypeByte(gadget), player, needItems, false, gadget, ServerBuildList.BuildType.BUILD, true, lookingAt);
        }
        return buildUUID;
    }

    public static UUID exchange(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ItemStack gadget, boolean needItems, boolean returnItems) {
        UUID buildUUID = UUID.randomUUID();
        FakeRenderingWorld fakeRenderingWorld = new FakeRenderingWorld(level, blockPosList, lookingAt);
        DimBlockPos boundPos = GadgetNBT.getBoundPos(gadget);
        int dir = boundPos == null ? -1 : GadgetNBT.getToolValue(gadget, "binddirection");
        Direction direction = dir == -1 ? null : Direction.values()[dir];
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos;
            if (!level.mayInteract(player, blockPos)) continue; //Chunk Protection like spawn and FTB Utils
            if (!GadgetUtils.isValidBlockState(level.getBlockState(blockPos), level, blockPos)) continue;
            if (gadget.getItem() instanceof GadgetBuilding && needItems && !pos.state.canSurvive(level, blockPos.offset(lookingAt)))
                continue;  //Don't do this validation for copy/paste
            List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos.offset(lookingAt), pos.state, player);
            if (!player.isCreative() && needItems) {  //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                if (!removeStacksFromInventory(player, neededItems, true, boundPos, direction))
                    continue; //Continue to the next position
            }
            if (!player.isCreative() && !hasEnoughEnergy(gadget)) {
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.outofpower"), true);
                break; //Break out if we're out of power
            }
            if (!player.isCreative()) {
                useEnergy(gadget);
            }
            ServerTickHandler.addToMap(buildUUID, new StatePos(fakeRenderingWorld.getBlockStateWithoutReal(pos.pos), pos.pos), level, GadgetNBT.getRenderTypeByte(gadget), player, needItems, returnItems, gadget, ServerBuildList.BuildType.EXCHANGE, true, lookingAt);
        }
        return buildUUID;
    }

    public static ArrayList<StatePos> buildWithTileData(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ArrayList<TagPos> teData, ItemStack gadget) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        if (teData == null) return actuallyBuiltList;
        UUID buildUUID;
        boolean replace = GadgetNBT.getPasteReplace(gadget);
        if (!replace)
            buildUUID = BuildingUtils.build(level, player, blockPosList, lookingAt, gadget, false);
        else
            buildUUID = BuildingUtils.exchange(level, player, blockPosList, lookingAt, gadget, false, false);

        ServerTickHandler.addTEData(buildUUID, teData);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        if (!bg2Data.containsUndoList(GadgetNBT.getUUID(gadget))) //Only if theres not already an undo list for this gadget, otherwise it'll clear it (Duh dire)
            GadgetUtils.addToUndoList(level, gadget, new ArrayList<>(), GadgetNBT.getUUID(gadget)); //For cut gadget, undo list will be a tracker of whats been built so far! Only 1 per gadget, so use gadgetUUID
        return actuallyBuiltList;
    }

    public static UUID removeTickHandler(Level level, Player player, List<BlockPos> blockPosList, boolean giveItem, boolean dropContents, ItemStack gadget) {
        UUID buildUUID = UUID.randomUUID();
        for (BlockPos pos : blockPosList) {
            if (!level.mayInteract(player, pos)) continue; //Chunk Protection like spawn and FTB Utils
            if (!player.isCreative() && !hasEnoughEnergy(gadget)) {
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.outofpower"), true);
                break; //Break out if we're out of power
            }
            BlockState oldState = level.getBlockState(pos);
            if (oldState.isAir() || !GadgetUtils.isValidBlockState(oldState, level, pos)) continue;
            if (!player.isCreative())
                useEnergy(gadget);
            ServerTickHandler.addToMap(buildUUID, new StatePos(Blocks.AIR.defaultBlockState(), pos), level, GadgetNBT.getRenderTypeByte(gadget), player, false, giveItem, gadget, ServerBuildList.BuildType.DESTROY, dropContents, BlockPos.ZERO);
        }
        return buildUUID;
    }
}

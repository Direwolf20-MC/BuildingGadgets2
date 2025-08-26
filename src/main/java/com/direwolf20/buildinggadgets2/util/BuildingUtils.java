package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.api.integrations.IntegrationRegistry;
import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.integration.AE2Integration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.*;

import static com.direwolf20.buildinggadgets2.integration.AE2Methods.*;

public class BuildingUtils {

    public static Level getLevel(MinecraftServer server, GlobalPos globalPos) {
        if (server == null)
            return null;//level = Minecraft.getInstance().level;
        else
            return server.getLevel(globalPos.dimension());
    }

    public static IItemHandler getHandlerFromBound(Player player, GlobalPos boundInventory, Direction direction) {
        Level level = getLevel(player.getServer(), boundInventory);
        if (level == null) return null;

        BlockEntity blockEntity = level.getBlockEntity(boundInventory.pos());
        if (blockEntity == null) return null;

        return level.getCapability(Capabilities.ItemHandler.BLOCK, boundInventory.pos(), direction);
    }

    public static ItemStack checkFluidHandlerForFluids(IFluidHandlerItem handler, FluidStack fluidStack, boolean simulate) {
        FluidStack drainedStack = handler.drain(fluidStack, IFluidHandler.FluidAction.SIMULATE);
        if (drainedStack.getAmount() == fluidStack.getAmount()) {
            if (!simulate)
                handler.drain(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            fluidStack.shrink(drainedStack.getAmount());
            return handler.getContainer();
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack insertFluidIntoHandler(IFluidHandlerItem handler, FluidStack fluidStack, boolean simulate) {
        int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.SIMULATE);
        if (filled == fluidStack.getAmount()) {
            if (!simulate)
                handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            fluidStack.shrink(filled);
            return handler.getContainer();
        }
        return ItemStack.EMPTY;
    }

    // TODO: Dire learn about avoiding the non-DRY hell. (Dry = Don't Repeat Yourself)
    public static ItemStack checkItemForFluids(ItemStack itemStack, FluidStack fluidStack, boolean simulate) {
        var itemStackCapability = itemStack.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (itemStackCapability != null) {
            checkItemHandlerForFluids(itemStackCapability, fluidStack, simulate);
            if (fluidStack.isEmpty())
                return ItemStack.EMPTY; //The Item Handler removed this for us, so no need to remove it again!
        }

        var fluidStackCapability = itemStack.getCapability(Capabilities.FluidHandler.ITEM, null);
        if (fluidStackCapability != null) {
            ItemStack returnedStack = checkFluidHandlerForFluids(fluidStackCapability, fluidStack, simulate);
            if (fluidStack.isEmpty())
                return returnedStack;
        }

        return ItemStack.EMPTY;
    }

    public static ItemStack insertFluidIntoItem(ItemStack itemStack, FluidStack fluidStack, boolean simulate) {
        var itemStackCapability = itemStack.getCapability(Capabilities.ItemHandler.ITEM, null);
        if (itemStackCapability != null) {
            insertFluidIntoItemHandler(itemStackCapability, fluidStack, simulate);
            if (fluidStack.isEmpty())
                return ItemStack.EMPTY; //The Item Handler removed this for us, so no need to remove it again!
        }

        var fluidStackCapability = itemStack.getCapability(Capabilities.FluidHandler.ITEM, null);
        if (fluidStackCapability != null) {
            ItemStack returnedStack = insertFluidIntoHandler(fluidStackCapability, fluidStack, simulate);
            if (fluidStack.isEmpty())
                return returnedStack;
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack checkItemHandlerForFluids(IItemHandler handler, FluidStack fluidStack, boolean simulate) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack itemInSlot = handler.getStackInSlot(j);
            ItemStack returnedStack = checkItemForFluids(itemInSlot.copy(), fluidStack, simulate);
            if (fluidStack.isEmpty()) {
                if (!simulate && !returnedStack.isEmpty()) {
                    if (itemInSlot.getCount() == 1) {
                        handler.extractItem(j, 1, false);
                        handler.insertItem(j, returnedStack, false);
                    } else {
                        handler.extractItem(j, 1, false);
                        ItemHandlerHelper.insertItemStacked(handler, returnedStack, false);
                    }
                }
                return returnedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static ItemStack insertFluidIntoItemHandler(IItemHandler handler, FluidStack fluidStack, boolean simulate) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack itemInSlot = handler.getStackInSlot(j);
            ItemStack returnedStack = insertFluidIntoItem(itemInSlot.copy().split(1), fluidStack, simulate);
            if (fluidStack.isEmpty()) {
                if (!simulate && !returnedStack.isEmpty()) {
                    if (itemInSlot.getCount() == 1) {
                        handler.extractItem(j, 1, false);
                        handler.insertItem(j, returnedStack, false);
                    } else {
                        handler.extractItem(j, 1, false);
                        ItemHandlerHelper.insertItemStacked(handler, returnedStack, false);
                    }
                }
                return returnedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static void checkInventoryForFluids(Inventory inventory, FluidStack fluidStack, boolean simulate) {
        for (int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack itemInSlot = inventory.getItem(j);
            ItemStack returnedStack = checkItemForFluids(itemInSlot, fluidStack, simulate);
            if (fluidStack.isEmpty()) { //Got all the fluids we need
                if (!simulate && !returnedStack.isEmpty()) {
                    inventory.setItem(j, returnedStack);
                }
                break;
            }
        }
    }

    public static boolean removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate, GlobalPos boundInventory, Direction direction) {
        if (fluidStack.isEmpty()) return false;
        //Check Bound Inventory First
        if (boundInventory != null) {
            if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
                checkAE2ForFluids(boundInventory, player, fluidStack, simulate);
                if (fluidStack.isEmpty()) return true;
            }
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                checkItemHandlerForFluids(boundHandler, fluidStack, simulate);
            }
        }

        if (fluidStack.isEmpty()) return true;

        IntegrationRegistry.removeFluidStacksFromInventory(player, fluidStack, simulate);
        if (fluidStack.isEmpty()) return true;

        Inventory playerInventory = player.getInventory();
        checkInventoryForFluids(playerInventory, fluidStack, simulate);
        if (fluidStack.isEmpty()) return true;
        return false;
    }

    // TODO: Dire, DRY plz
    public static void checkHandlerForItems(IItemHandler handler, List<ItemStack> testArray, boolean simulate) {
        for (int j = 0; j < handler.getSlots(); j++) {
            ItemStack itemInSlot = handler.getStackInSlot(j);
            var itemStackCapability = itemInSlot.getCapability(Capabilities.ItemHandler.ITEM, null);

            if (itemStackCapability != null) {
                checkHandlerForItems(itemStackCapability, testArray, simulate);
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
            var itemStackCapability = itemInSlot.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (itemStackCapability != null) {
                checkHandlerForItems(itemStackCapability, testArray, simulate);
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

    public static boolean removeStacksFromInventory(Player player, List<ItemStack> itemStacks, boolean simulate, GlobalPos boundInventory, Direction direction) {
        if (itemStacks.isEmpty() || itemStacks.contains(Items.AIR.getDefaultInstance())) return false;
        ArrayList<ItemStack> testArray = new ArrayList<>(itemStacks);
        //Check Bound Inventory First
        if (boundInventory != null) {
            if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
                checkAE2ForItems(boundInventory, player, testArray, simulate);
                if (testArray.isEmpty()) return true;
            }
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                checkHandlerForItems(boundHandler, testArray, simulate);
            }
        }

        if (testArray.isEmpty()) return true;

        IntegrationRegistry.removeStacksFromInventory(player, testArray, simulate);
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

        IntegrationRegistry.countItemStacks(player, itemStack, counter);

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            var itemStackCapability = slotStack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (itemStackCapability != null) {
                for (int j = 0; j < itemStackCapability.getSlots(); j++) {
                    ItemStack itemInSlot = itemStackCapability.getStackInSlot(j);
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

    public static void giveFluidToPlayer(Player player, FluidStack returnedFluid, GlobalPos boundInventory, Direction direction) {
        //Check Bound Inventory First
        if (boundInventory != null) {
            if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
                insertFluidIntoAE2(player, boundInventory, returnedFluid);
                if (returnedFluid.isEmpty()) return;
            }
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                insertFluidIntoItemHandler(boundHandler, returnedFluid, false);
            }
        }
        if (returnedFluid.isEmpty()) return;

        IntegrationRegistry.giveFluidToPlayer(player, returnedFluid);
        if (returnedFluid.isEmpty()) return;

        //Now look inside the players inventory
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) { //If this fails the fluid just gets voided!
            ItemStack slotStack = playerInventory.getItem(i);
            ItemStack returnedStack = insertFluidIntoItem(slotStack.copy().split(1), returnedFluid, false);
            if (!returnedStack.isEmpty()) {
                if (slotStack.getCount() == 1) {
                    playerInventory.setItem(i, returnedStack);
                } else {
                    slotStack.shrink(1);
                    if (!player.addItem(returnedStack)) {
                        BlockPos dropPos = player.getOnPos();
                        ItemEntity itementity = new ItemEntity(player.level(), dropPos.getX(), dropPos.getY(), dropPos.getZ(), returnedStack);
                        itementity.setPickUpDelay(40);
                        player.level().addFreshEntity(itementity);
                    }
                }
                return;
            }
        }
    }

    public static void giveItemToPlayer(Player player, ItemStack returnedItem, GlobalPos boundInventory, Direction direction) {
        //Check Bound Inventory First
        ItemStack tempReturnedItem = returnedItem.copy();
        if (boundInventory != null) {
            if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
                insertIntoAE2(player, boundInventory, tempReturnedItem);
                if (tempReturnedItem.isEmpty()) return;
            }
            IItemHandler boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                tempReturnedItem = ItemHandlerHelper.insertItemStacked(boundHandler, returnedItem, false);
            }
        }
        if (tempReturnedItem.isEmpty()) return;
        ItemStack realReturnedItem = tempReturnedItem.copy();

        IntegrationRegistry.giveItemToPlayer(player, realReturnedItem);
        if (realReturnedItem.isEmpty()) return;

        //Now look for bags inside the players inventory
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            var itemStackCapability = slotStack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (itemStackCapability != null) {
                for (int j = 0; j < itemStackCapability.getSlots(); j++) {
                    ItemStack itemInSlot = itemStackCapability.getStackInSlot(j);
                    if (ItemStack.isSameItem(itemInSlot, realReturnedItem))
                        itemStackCapability.insertItem(j, realReturnedItem.split(itemStackCapability.getSlotLimit(j) - itemInSlot.getCount()), false);
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
            IEnergyStorage energy = gadget.getCapability(Capabilities.EnergyStorage.ITEM);
            return energy != null ? energy.getEnergyStored() : 0;
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
            IEnergyStorage energy = gadget.getCapability(Capabilities.EnergyStorage.ITEM);
            if (energy == null) return; //This should never happen, but just in case :
            int cost = baseGadget.getEnergyCost();
            energy.extractEnergy(cost, false);
        }
    }

    public static UUID build(Level level, Player player, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ItemStack gadget, boolean needItems) {
        UUID buildUUID = UUID.randomUUID();
        FakeRenderingWorld fakeRenderingWorld = new FakeRenderingWorld(level, blockPosList, lookingAt);
        GlobalPos boundPos = GadgetNBT.getBoundPos(gadget);
        int dir = boundPos == null ? -1 : GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.BIND_DIRECTION.getName());
        Direction direction = dir == -1 ? null : Direction.values()[dir];
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            BlockPos blockPos = pos.pos;
            if (!level.mayInteract(player, blockPos.offset(lookingAt)))
                continue; //Chunk Protection like spawn
            if (EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, blockPos.offset(lookingAt).below()), Direction.UP))
                continue; //FTB Chunk Protection, etc
            if (!level.getBlockState(blockPos.offset(lookingAt)).canBeReplaced())
                continue; //Skip this block if it can't be placed (Avoids using energy)
            if (gadget.getItem() instanceof GadgetBuilding && needItems && !pos.state.canSurvive(level, blockPos.offset(lookingAt)))
                continue; //Don't do this validation for copy/paste
            if (pos.state.getFluidState().isEmpty()) { //Check for items
                List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos.offset(lookingAt), pos.state, player);
                if (!player.isCreative() && needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    if (!removeStacksFromInventory(player, neededItems, true, boundPos, direction))
                        continue; //Continue to the next position
                }
            } else { //Check For Fluids
                FluidState fluidState = pos.state.getFluidState();
                if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                    Fluid fluid = fluidState.getType();
                    FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                    if (!player.isCreative() && needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                        if (!removeFluidStacksFromInventory(player, fluidStack, true, boundPos, direction))
                            continue; //Continue to the next position
                    }
                }
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
        GlobalPos boundPos = GadgetNBT.getBoundPos(gadget);
        int dir = boundPos == null ? -1 : GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.BIND_DIRECTION.getName());
        Direction direction = dir == -1 ? null : Direction.values()[dir];
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos;
            if (!level.mayInteract(player, blockPos.offset(lookingAt)))
                continue; //Chunk Protection like spawn and FTB Utils
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, blockPos.offset(lookingAt), level.getBlockState(blockPos.offset(lookingAt)), player);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) continue;
            if (EventHooks.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, blockPos.offset(lookingAt).below()), Direction.UP))
                continue; //FTB Chunk Protection, etc
            if (level.getBlockState(blockPos.offset(lookingAt)).equals(pos.state))
                continue; //No need to replace blocks if they already match!
            if (!GadgetUtils.isValidBlockState(level.getBlockState(blockPos.offset(lookingAt)), level, blockPos))
                continue;
            if (gadget.getItem() instanceof GadgetBuilding && needItems && !pos.state.canSurvive(level, blockPos.offset(lookingAt)))
                continue;  //Don't do this validation for copy/paste
            if (pos.state.getFluidState().isEmpty()) { //Check for items
                List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos.offset(lookingAt), pos.state, player);
                if (!player.isCreative() && needItems && !pos.state.isAir()) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    if (!removeStacksFromInventory(player, neededItems, true, boundPos, direction))
                        continue; //Continue to the next position
                }
            } else { //Check For Fluids
                FluidState fluidState = pos.state.getFluidState();
                if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                    Fluid fluid = fluidState.getType();
                    FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                    if (!player.isCreative() && needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                        if (!removeFluidStacksFromInventory(player, fluidStack, true, boundPos, direction))
                            continue; //Continue to the next position
                    }
                }
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

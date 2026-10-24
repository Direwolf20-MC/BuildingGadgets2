package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
// TODO(port, caps-rework): re-enable AE2 imports when Transfer API port is done
// import com.direwolf20.buildinggadgets2.integration.AE2Integration;
import com.direwolf20.buildinggadgets2.integration.CuriosIntegration;
import com.direwolf20.buildinggadgets2.integration.CuriosMethods;
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
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.*;

// TODO(port, caps-rework): re-enable AE2Methods static import when Transfer API port is done
// import static com.direwolf20.buildinggadgets2.integration.AE2Methods.*;

public class BuildingUtils {

    public static Level getLevel(MinecraftServer server, GlobalPos globalPos) {
        if (server == null)
            return null;//level = Minecraft.getInstance().level;
        else
            return server.getLevel(globalPos.dimension());
    }

    public static ResourceHandler<ItemResource> getHandlerFromBound(Player player, GlobalPos boundInventory, Direction direction) {
        Level level = getLevel(player.level().getServer(), boundInventory);
        if (level == null) return null;

        BlockEntity blockEntity = level.getBlockEntity(boundInventory.pos());
        if (blockEntity == null) return null;

        return level.getCapability(Capabilities.Item.BLOCK, boundInventory.pos(), direction);
    }

    /**
     * Drain {@code fluidStack.getAmount()} units of {@code fluidStack}'s fluid from the given handler, if the full amount is available.
     * Shrinks {@code fluidStack} in-place on success. Returns whether the drain happened (or would happen, when simulating).
     */
    public static boolean drainFluidFromHandler(ResourceHandler<FluidResource> handler, FluidStack fluidStack, boolean simulate) {
        if (fluidStack.isEmpty()) return false;
        FluidResource resource = FluidResource.of(fluidStack);
        int needed = fluidStack.getAmount();
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, needed, tx);
            if (extracted == needed) {
                if (!simulate) tx.commit();
                fluidStack.shrink(extracted);
                return true;
            }
        }
        return false;
    }

    /**
     * Insert {@code fluidStack.getAmount()} units of {@code fluidStack}'s fluid into the given handler, if the full amount fits.
     * Shrinks {@code fluidStack} in-place on success. Returns whether the insert happened (or would happen, when simulating).
     */
    public static boolean insertFluidIntoHandler(ResourceHandler<FluidResource> handler, FluidStack fluidStack, boolean simulate) {
        if (fluidStack.isEmpty()) return false;
        FluidResource resource = FluidResource.of(fluidStack);
        int amount = fluidStack.getAmount();
        try (Transaction tx = Transaction.openRoot()) {
            int inserted = ResourceHandlerUtil.insertStacking(handler, resource, amount, tx);
            if (inserted == amount) {
                if (!simulate) tx.commit();
                fluidStack.shrink(inserted);
                return true;
            }
        }
        return false;
    }

    // TODO: Dire learn about avoiding the non-DRY hell. (Dry = Don't Repeat Yourself)
    public static void checkItemForFluids(ItemAccess itemAccess, FluidStack fluidStack, boolean simulate) {
        if (fluidStack.isEmpty()) return;
        ResourceHandler<ItemResource> itemHandlerCap = itemAccess.getCapability(Capabilities.Item.ITEM);
        if (itemHandlerCap != null) {
            checkItemHandlerForFluids(itemHandlerCap, fluidStack, simulate);
            if (fluidStack.isEmpty()) return; // bag-in-item handled it
        }

        ResourceHandler<FluidResource> fluidHandlerCap = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (fluidHandlerCap != null) {
            drainFluidFromHandler(fluidHandlerCap, fluidStack, simulate);
        }
    }

    public static void insertFluidIntoItem(ItemAccess itemAccess, FluidStack fluidStack, boolean simulate) {
        if (fluidStack.isEmpty()) return;
        ResourceHandler<ItemResource> itemHandlerCap = itemAccess.getCapability(Capabilities.Item.ITEM);
        if (itemHandlerCap != null) {
            insertFluidIntoItemHandler(itemHandlerCap, fluidStack, simulate);
            if (fluidStack.isEmpty()) return; // bag-in-item handled it
        }

        ResourceHandler<FluidResource> fluidHandlerCap = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (fluidHandlerCap != null) {
            insertFluidIntoHandler(fluidHandlerCap, fluidStack, simulate);
        }
    }

    public static void checkItemHandlerForFluids(ResourceHandler<ItemResource> handler, FluidStack fluidStack, boolean simulate) {
        int size = handler.size();
        for (int j = 0; j < size; j++) {
            if (handler.getResource(j).isEmpty()) continue;
            ItemAccess slotAccess = ItemAccess.forHandlerIndex(handler, j);
            checkItemForFluids(slotAccess, fluidStack, simulate);
            if (fluidStack.isEmpty()) return;
        }
    }

    public static void insertFluidIntoItemHandler(ResourceHandler<ItemResource> handler, FluidStack fluidStack, boolean simulate) {
        int size = handler.size();
        for (int j = 0; j < size; j++) {
            if (handler.getResource(j).isEmpty()) continue;
            ItemAccess slotAccess = ItemAccess.forHandlerIndex(handler, j);
            insertFluidIntoItem(slotAccess, fluidStack, simulate);
            if (fluidStack.isEmpty()) return;
        }
    }

    public static void checkInventoryForFluids(Player player, Inventory inventory, FluidStack fluidStack, boolean simulate) {
        // Capability lookup pattern: slotStack.getCapability(cap, ItemAccess.forStack(slotStack)).
        // This preserves both:
        //   1. ItemStack identity — ItemStack.getCapability(cap, ctx) passes `self()` (the live
        //      slotStack) to the provider factory, so mods that cache their handler by
        //      ItemStack identity (e.g. Sophisticated Backpacks' StorageWrapperRepository)
        //      hit the same cache entry the open menu is bound to.
        //   2. A non-null ItemAccess context — required by providers that use NeoForge's
        //      ItemAccessItemHandler / ItemAccessEnergyHandler and dereference the context.
        // Going through ItemAccess.forStack(slotStack).getCapability(cap) instead loses #1
        // because the default ItemAccess.getCapability passes `getResource().toStack()` — a
        // fresh ItemStack instance — to the provider, producing a cache miss in mods like SB.
        for (int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack itemInSlot = inventory.getItem(j);
            if (itemInSlot.isEmpty()) continue;
            ResourceHandler<ItemResource> nestedHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
            if (nestedHandler != null) {
                checkItemHandlerForFluids(nestedHandler, fluidStack, simulate);
                if (fluidStack.isEmpty()) return;
            }
            ResourceHandler<FluidResource> fluidHandlerCap = itemInSlot.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemInSlot));
            if (fluidHandlerCap != null) {
                drainFluidFromHandler(fluidHandlerCap, fluidStack, simulate);
                if (fluidStack.isEmpty()) return;
            }
        }
    }

    public static boolean removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate, GlobalPos boundInventory, Direction direction) {
        if (fluidStack.isEmpty()) return false;
        //Check Bound Inventory First
        if (boundInventory != null) {
            // TODO(port, caps-rework): load-bearing — AE2 bound inventory fluid extraction silently skipped
            // if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
            //     checkAE2ForFluids(boundInventory, player, fluidStack, simulate);
            //     if (fluidStack.isEmpty()) return true;
            // }
            ResourceHandler<ItemResource> boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                checkItemHandlerForFluids(boundHandler, fluidStack, simulate);
            }
        }

        if (fluidStack.isEmpty()) return true;
        //Check curious slots second:
        if (CuriosIntegration.isLoaded()) {
            CuriosMethods.removeFluidStacksFromInventory(player, fluidStack, simulate);
        }
        if (fluidStack.isEmpty()) return true;

        checkInventoryForFluids(player, player.getInventory(), fluidStack, simulate);
        return fluidStack.isEmpty();
    }

    // TODO: Dire, DRY plz
    public static void checkHandlerForItems(ResourceHandler<ItemResource> handler, List<ItemStack> testArray, boolean simulate) {
        int size = handler.size();
        for (int j = 0; j < size; j++) {
            ItemResource slotResource = handler.getResource(j);
            if (slotResource.isEmpty()) continue;
            ItemStack itemInSlot = slotResource.toStack(handler.getAmountAsInt(j));

            // Check for a nested item handler first (bag-in-inventory)
            ResourceHandler<ItemResource> nestedHandler = ItemAccess.forHandlerIndex(handler, j).getCapability(Capabilities.Item.ITEM);
            if (nestedHandler != null && nestedHandler != handler) {
                checkHandlerForItems(nestedHandler, testArray, simulate);
                if (testArray.isEmpty()) return;
                continue;
            }

            Optional<ItemStack> matchStack = testArray.stream().filter(e -> ItemStack.isSameItem(e, itemInSlot) && itemInSlot.getCount() >= e.getCount()).findFirst();
            if (matchStack.isPresent()) { //Todo: Support multiple stacks of same item
                ItemStack matchingStack = matchStack.get();
                try (Transaction tx = Transaction.openRoot()) {
                    int extracted = handler.extract(j, slotResource, matchingStack.getCount(), tx);
                    if (extracted == matchingStack.getCount()) {
                        if (!simulate) tx.commit();
                        testArray.remove(matchingStack);
                    }
                }
            }
            if (testArray.isEmpty()) return;
        }
    }

    public static void checkInventoryForItems(Player player, Inventory inventory, List<ItemStack> testArray, boolean simulate) {
        // See the null-ItemAccess rationale on checkInventoryForFluids above.
        for (int j = 0; j < inventory.getContainerSize(); j++) {
            ItemStack itemInSlot = inventory.getItem(j);
            if (itemInSlot.isEmpty()) continue;
            ResourceHandler<ItemResource> nestedHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
            if (nestedHandler != null) {
                checkHandlerForItems(nestedHandler, testArray, simulate);
                if (testArray.isEmpty()) return;
            } else {
                Optional<ItemStack> matchStack = testArray.stream().filter(e -> ItemStack.isSameItem(e, itemInSlot) && itemInSlot.getCount() >= e.getCount()).findFirst();
                if (matchStack.isPresent()) { //Todo: Support multiple stacks of same item
                    ItemStack matchingStack = matchStack.get();
                    if (!simulate)
                        itemInSlot.shrink(matchingStack.getCount());
                    testArray.remove(matchingStack);
                }
            }
            if (testArray.isEmpty()) return;
        }
    }

    public static boolean removeStacksFromInventory(Player player, List<ItemStack> itemStacks, boolean simulate, GlobalPos boundInventory, Direction direction) {
        if (itemStacks.isEmpty() || itemStacks.contains(Items.AIR.getDefaultInstance())) return false;
        ArrayList<ItemStack> testArray = new ArrayList<>(itemStacks);
        //Check Bound Inventory First
        if (boundInventory != null) {
            // TODO(port, caps-rework): load-bearing — AE2 bound inventory item extraction silently skipped
            // if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
            //     checkAE2ForItems(boundInventory, player, testArray, simulate);
            //     if (testArray.isEmpty()) return true;
            // }
            ResourceHandler<ItemResource> boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                checkHandlerForItems(boundHandler, testArray, simulate);
            }
        }

        if (testArray.isEmpty()) return true;
        //Check curious slots second:
        if (CuriosIntegration.isLoaded()) {
            CuriosMethods.removeStacksFromInventory(player, testArray, simulate);
        }
        if (testArray.isEmpty()) return true;

        checkInventoryForItems(player, player.getInventory(), testArray, simulate);
        return testArray.isEmpty();
    }

    public static int countItemStacks(Player player, ItemStack itemStack) {
        if (itemStack.isEmpty() || itemStack.is(Items.AIR)) return 0;
        Inventory playerInventory = player.getInventory();
        // Mutable holder so the Curios lambda can accumulate.
        int[] counter = new int[]{0};

        //Check curious slots first:
        if (CuriosIntegration.isLoaded()) {
            CuriosMethods.countItemStacks(player, itemStack, counter);
        }

        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            if (slotStack.isEmpty()) continue;
            ResourceHandler<ItemResource> nestedHandler = slotStack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(slotStack));
            if (nestedHandler != null) {
                int size = nestedHandler.size();
                for (int j = 0; j < size; j++) {
                    ItemResource nestedResource = nestedHandler.getResource(j);
                    if (nestedResource.isEmpty()) continue;
                    if (ItemStack.isSameItem(nestedResource.toStack(), itemStack))
                        counter[0] += nestedHandler.getAmountAsInt(j);
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
            // TODO(port, caps-rework): load-bearing — AE2 bound inventory fluid insertion silently skipped
            // if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
            //     insertFluidIntoAE2(player, boundInventory, returnedFluid);
            //     if (returnedFluid.isEmpty()) return;
            // }
            ResourceHandler<ItemResource> boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                insertFluidIntoItemHandler(boundHandler, returnedFluid, false);
            }
        }
        if (returnedFluid.isEmpty()) return;

        //Look for matching itemstacks inside curios inventories second - if found, insert there!
        if (CuriosIntegration.isLoaded()) {
            CuriosMethods.giveFluidToPlayer(player, returnedFluid);
        }
        if (returnedFluid.isEmpty()) return;
        //Now look inside the players inventory
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) { //If this fails the fluid just gets voided!
            ItemStack slotStack = playerInventory.getItem(i);
            if (slotStack.isEmpty()) continue;
            ResourceHandler<ItemResource> nestedHandler = slotStack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(slotStack));
            if (nestedHandler != null) {
                insertFluidIntoItemHandler(nestedHandler, returnedFluid, false);
                if (returnedFluid.isEmpty()) return;
            }
            ResourceHandler<FluidResource> fluidHandlerCap = slotStack.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(slotStack));
            if (fluidHandlerCap != null) {
                insertFluidIntoHandler(fluidHandlerCap, returnedFluid, false);
                if (returnedFluid.isEmpty()) return;
            }
        }
    }

    public static void giveItemToPlayer(Player player, ItemStack returnedItem, GlobalPos boundInventory, Direction direction) {
        //Check Bound Inventory First
        ItemStack tempReturnedItem = returnedItem.copy();
        if (boundInventory != null) {
            // TODO(port, caps-rework): load-bearing — AE2 bound inventory item insertion silently skipped
            // if (AE2Integration.isLoaded()) { //Check if we are bound to an AE Device
            //     insertIntoAE2(player, boundInventory, tempReturnedItem);
            //     if (tempReturnedItem.isEmpty()) return;
            // }
            ResourceHandler<ItemResource> boundHandler = getHandlerFromBound(player, boundInventory, direction);
            if (boundHandler != null) {
                int inserted = ResourceHandlerUtil.insertStacking(boundHandler, ItemResource.of(tempReturnedItem), tempReturnedItem.getCount(), null);
                tempReturnedItem.shrink(inserted);
            }
        }
        if (tempReturnedItem.isEmpty()) return;
        ItemStack realReturnedItem = tempReturnedItem.copy();

        //Look for matching itemstacks inside curios inventories second - if found, insert there!
        if (CuriosIntegration.isLoaded()) {
            CuriosMethods.giveItemToPlayer(player, realReturnedItem);
            if (realReturnedItem.isEmpty()) return;
        }
        //Now look for bags inside the players inventory
        Inventory playerInventory = player.getInventory();
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack slotStack = playerInventory.getItem(i);
            if (slotStack.isEmpty()) continue;
            ResourceHandler<ItemResource> nestedHandler = slotStack.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(slotStack));
            if (nestedHandler != null) {
                int inserted = ResourceHandlerUtil.insertStacking(nestedHandler, ItemResource.of(realReturnedItem), realReturnedItem.getCount(), null);
                realReturnedItem.shrink(inserted);
                if (realReturnedItem.isEmpty()) return;
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
            EnergyHandler energy = gadget.getCapability(Capabilities.Energy.ITEM, null);
            return energy != null ? energy.getAmountAsInt() : 0;
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
            EnergyHandler energy = gadget.getCapability(Capabilities.Energy.ITEM, null);
            if (energy == null) return; //This should never happen, but just in case :
            int cost = baseGadget.getEnergyCost();
            try (Transaction tx = Transaction.openRoot()) {
                energy.extract(cost, tx);
                tx.commit();
            }
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
                player.sendOverlayMessage(Component.translatable("buildinggadgets2.messages.outofpower"));
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
            BreakBlockEvent event = new BreakBlockEvent(level, blockPos.offset(lookingAt), level.getBlockState(blockPos.offset(lookingAt)), player);
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
                player.sendOverlayMessage(Component.translatable("buildinggadgets2.messages.outofpower"));
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
                player.sendOverlayMessage(Component.translatable("buildinggadgets2.messages.outofpower"));
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

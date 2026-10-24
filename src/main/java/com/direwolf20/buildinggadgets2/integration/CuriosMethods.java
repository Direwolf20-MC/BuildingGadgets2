package com.direwolf20.buildinggadgets2.integration;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;

import static com.direwolf20.buildinggadgets2.util.BuildingUtils.*;

// Curios 15.0 keeps the legacy IItemHandlerModifiable on getStacks(), so we read the
// per-slot ItemStack the old way and look up its capability via
//   itemInSlot.getCapability(cap, ItemAccess.forStack(itemInSlot))
// This preserves ItemStack identity for providers that cache their handler by stack
// (e.g. Sophisticated Backpacks' StorageWrapperRepository — otherwise the menu goes
// stale), AND supplies a non-null ItemAccess for providers that use NeoForge's
// ItemAccessItemHandler / ItemAccessEnergyHandler. Going through
// ItemAccess.forStack(stack).getCapability(cap) instead loses identity because the
// default ItemAccess.getCapability passes getResource().toStack() — a fresh copy — to
// the provider factory.
//
// We still round-trip through stackHandler.getStacks().setStackInSlot(j, itemInSlot)
// after a successful mutation: in-place component writes don't trigger Curios'
// previous-vs-current diff (in CuriosCommonEvents.tick); the round-trip pokes the
// IDynamicStackHandler so its onContentsChanged fires and the next tick's diff sends
// SPacketSyncStack to the client.
public class CuriosMethods {

    public static void removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate) {
        var curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int j = 0; j < stackHandler.getSlots(); j++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(j);
                if (itemInSlot.isEmpty()) continue;
                int before = fluidStack.getAmount();
                ResourceHandler<ItemResource> nestedItemHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
                if (nestedItemHandler != null) {
                    checkItemHandlerForFluids(nestedItemHandler, fluidStack, simulate);
                }
                if (!fluidStack.isEmpty()) {
                    ResourceHandler<FluidResource> fluidCap = itemInSlot.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemInSlot));
                    if (fluidCap != null) drainFluidFromHandler(fluidCap, fluidStack, simulate);
                }
                if (!simulate && fluidStack.getAmount() != before)
                    stackHandler.getStacks().setStackInSlot(j, itemInSlot);
                if (fluidStack.isEmpty()) return;
            }
        }));
    }

    public static void removeStacksFromInventory(Player player, ArrayList<ItemStack> testArray, boolean simulate) {
        var curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int j = 0; j < stackHandler.getSlots(); j++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(j);
                if (itemInSlot.isEmpty()) continue;
                ResourceHandler<ItemResource> nestedHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
                if (nestedHandler != null) {
                    int before = testArray.size();
                    checkHandlerForItems(nestedHandler, testArray, simulate);
                    if (!simulate && testArray.size() != before)
                        stackHandler.getStacks().setStackInSlot(j, itemInSlot);
                    if (testArray.isEmpty()) return;
                }
            }
        }));
    }

    public static void countItemStacks(Player player, ItemStack itemStack, int[] counter) {
        var curiosOpt = CuriosApi.getCuriosInventory(player);
        curiosOpt.ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                if (itemInSlot.isEmpty()) continue;
                ResourceHandler<ItemResource> nestedHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
                if (nestedHandler == null) continue;
                int size = nestedHandler.size();
                for (int j = 0; j < size; j++) {
                    ItemResource nestedResource = nestedHandler.getResource(j);
                    if (nestedResource.isEmpty()) continue;
                    if (ItemStack.isSameItem(nestedResource.toStack(), itemStack))
                        counter[0] += nestedHandler.getAmountAsInt(j);
                }
            }
        }));
    }

    public static void giveFluidToPlayer(Player player, FluidStack returnedFluid) {
        var curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                if (itemInSlot.isEmpty()) continue;
                int before = returnedFluid.getAmount();
                ResourceHandler<ItemResource> nestedItemHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
                if (nestedItemHandler != null) {
                    insertFluidIntoItemHandler(nestedItemHandler, returnedFluid, false);
                }
                if (!returnedFluid.isEmpty()) {
                    ResourceHandler<FluidResource> fluidCap = itemInSlot.getCapability(Capabilities.Fluid.ITEM, ItemAccess.forStack(itemInSlot));
                    if (fluidCap != null) insertFluidIntoHandler(fluidCap, returnedFluid, false);
                }
                if (returnedFluid.getAmount() != before)
                    stackHandler.getStacks().setStackInSlot(i, itemInSlot);
                if (returnedFluid.isEmpty()) return;
            }
        }));
    }

    public static void giveItemToPlayer(Player player, ItemStack realReturnedItem) {
        var curiosOpt = CuriosApi.getCuriosInventory(player);
        curiosOpt.ifPresent(handler -> handler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                if (itemInSlot.isEmpty()) continue;
                ResourceHandler<ItemResource> nestedHandler = itemInSlot.getCapability(Capabilities.Item.ITEM, ItemAccess.forStack(itemInSlot));
                if (nestedHandler == null) continue;
                int inserted = ResourceHandlerUtil.insertStacking(nestedHandler, ItemResource.of(realReturnedItem), realReturnedItem.getCount(), null);
                if (inserted > 0) {
                    realReturnedItem.shrink(inserted);
                    stackHandler.getStacks().setStackInSlot(i, itemInSlot);
                }
                if (realReturnedItem.isEmpty()) return;
            }
        }));
    }
}

package com.direwolf20.buildinggadgets2.integration;

import com.direwolf20.buildinggadgets2.api.integrations.IIntegration;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.direwolf20.buildinggadgets2.util.BuildingUtils.*;

public class CuriosIntegration implements IIntegration {
    public void removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate) {
        var curios = CuriosApi.getCuriosInventory(player);

        curios.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((id, stackHandler) -> {
            for (int j = 0; j < stackHandler.getSlots(); j++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(j);
                checkItemForFluids(itemInSlot, fluidStack, simulate);
            }
        }));
    }

    public void removeStacksFromInventory(Player player, ArrayList<ItemStack> testArray, boolean simulate) {
        var curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((id, stackHandler) -> {
            for (int j = 0; j < stackHandler.getSlots(); j++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(j);
                var itemStackCapability = itemInSlot.getCapability(Capabilities.ItemHandler.ITEM, null);
                if (itemStackCapability != null) {
                    checkHandlerForItems(itemStackCapability, testArray, simulate);
                    if (testArray.isEmpty()) break;
                }
            }
        }));
    }

    public int countItemStacks(Player player, ItemStack itemStack) {
        var curiosOpt = CuriosApi.getCuriosInventory(player);
        AtomicInteger count = new AtomicInteger();
        curiosOpt.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                var itemStackCapability = itemInSlot.getCapability(Capabilities.ItemHandler.ITEM, null);
                if (itemStackCapability != null) {
                    for (int j = 0; j < itemStackCapability.getSlots(); j++) {
                        ItemStack itemInBagSlot = itemStackCapability.getStackInSlot(j);
                        if (ItemStack.isSameItem(itemInBagSlot, itemStack))
                            count.addAndGet(itemInBagSlot.getCount());
                    }
                }
            }
        }));
        return count.get();
    }

    public void giveFluidToPlayer(Player player, FluidStack returnedFluid) {
        var curios = CuriosApi.getCuriosInventory(player);
        curios.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                insertFluidIntoItem(itemInSlot, returnedFluid, false);
                if (returnedFluid.isEmpty()) return;
            }
        }));
    }

    public void giveItemToPlayer(Player player, ItemStack realReturnedItem) {
        var curiosOpt = CuriosApi.getCuriosInventory(player);
        curiosOpt.ifPresent(iCuriosItemHandler -> iCuriosItemHandler.getCurios().forEach((id, stackHandler) -> {
            for (int i = 0; i < stackHandler.getSlots(); i++) {
                ItemStack itemInSlot = stackHandler.getStacks().getStackInSlot(i);
                var itemStackCapability = itemInSlot.getCapability(Capabilities.ItemHandler.ITEM, null);
                if (itemStackCapability != null) {
                    for (int j = 0; j < itemStackCapability.getSlots(); j++) {
                        ItemStack itemInBagSlot = itemStackCapability.getStackInSlot(j);
                        if (ItemStack.isSameItem(itemInBagSlot, realReturnedItem))
                            itemStackCapability.insertItem(j, realReturnedItem.split(itemStackCapability.getSlotLimit(j) - itemInBagSlot.getCount()), false);
                        if (realReturnedItem.isEmpty()) return;
                    }
                }
            }
        }));

    }
}

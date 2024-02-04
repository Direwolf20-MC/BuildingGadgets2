package com.direwolf20.buildinggadgets2.integration;

import appeng.api.config.Actionable;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.storage.MEStorage;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.DimBlockPos;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Iterator;
import java.util.List;

public class AE2Methods {

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public static void registerItems() {
        GridLinkables.register(Registration.Building_Gadget.get(), LINKABLE_HANDLER);
        GridLinkables.register(Registration.Exchanging_Gadget.get(), LINKABLE_HANDLER);
        GridLinkables.register(Registration.CopyPaste_Gadget.get(), LINKABLE_HANDLER);
    }

    public static class LinkableHandler implements IGridLinkableHandler {
        @Override
        public boolean canLink(ItemStack stack) {
            return stack.getItem() instanceof BaseGadget;
        }

        @Override
        public void link(ItemStack itemStack, GlobalPos pos) {
            GadgetNBT.setBoundPos(itemStack, new DimBlockPos(pos.dimension(), pos.pos()));
        }

        @Override
        public void unlink(ItemStack itemStack) {
            GadgetNBT.clearBoundPos(itemStack);
        }
    }

    public static void checkAE2ForItems(DimBlockPos boundInventory, Player player, List<ItemStack> testArray, boolean simulate) {
        Level level = boundInventory.getLevel(player.getServer());
        if (level == null) return;
        BlockEntity blockEntity = level.getBlockEntity(boundInventory.blockPos);
        if (blockEntity == null) return;
        if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
            IGrid grid = accessPoint.getGrid();
            if (grid == null) return;
            MEStorage networkInv = grid.getStorageService().getInventory();
            Iterator<ItemStack> iterator = testArray.iterator();
            while (iterator.hasNext()) {
                ItemStack itemStack = iterator.next();
                AEItemKey itemKey = AEItemKey.of(itemStack);
                long amountExtracted = networkInv.extract(itemKey, itemStack.getCount(), Actionable.SIMULATE, IActionSource.ofPlayer(player));
                if (amountExtracted == itemStack.getCount()) { //I don't wanna do partial removes - because if you need 2 slabs and only have 1, i don't wanna place the half
                    if (!simulate)
                        networkInv.extract(itemKey, itemStack.getCount(), Actionable.MODULATE, IActionSource.ofPlayer(player));
                    iterator.remove();
                }
            }
        }
    }

    public static void checkAE2ForFluids(DimBlockPos boundInventory, Player player, FluidStack fluidStack, boolean simulate) {
        Level level = boundInventory.getLevel(player.getServer());
        if (level == null) return;
        BlockEntity blockEntity = level.getBlockEntity(boundInventory.blockPos);
        if (blockEntity == null) return;
        if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
            IGrid grid = accessPoint.getGrid();
            if (grid == null) return;
            MEStorage networkInv = grid.getStorageService().getInventory();
            AEFluidKey fluidKey = AEFluidKey.of(fluidStack);
            long amountExtracted = networkInv.extract(fluidKey, fluidStack.getAmount(), Actionable.SIMULATE, IActionSource.ofPlayer(player));
            if (amountExtracted == fluidStack.getAmount()) {
                if (!simulate)
                    networkInv.extract(fluidKey, fluidStack.getAmount(), Actionable.MODULATE, IActionSource.ofPlayer(player));
                fluidStack.shrink(fluidStack.getAmount());
            }

        }
    }

    public static void insertIntoAE2(Player player, DimBlockPos boundInventory, ItemStack tempReturnedItem) {
        Level level = boundInventory.getLevel(player.getServer());
        if (level == null) return;
        BlockEntity blockEntity = level.getBlockEntity(boundInventory.blockPos);
        if (blockEntity == null) return;
        if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
            IGrid grid = accessPoint.getGrid();
            if (grid == null) return;
            MEStorage networkInv = grid.getStorageService().getInventory();
            AEItemKey itemKey = AEItemKey.of(tempReturnedItem);
            long amountInserted = networkInv.insert(itemKey, tempReturnedItem.getCount(), Actionable.MODULATE, IActionSource.ofPlayer(player));
            tempReturnedItem.shrink((int) amountInserted);
        }
    }

    public static void insertFluidIntoAE2(Player player, DimBlockPos boundInventory, FluidStack returnedFluid) {
        Level level = boundInventory.getLevel(player.getServer());
        if (level == null) return;
        BlockEntity blockEntity = level.getBlockEntity(boundInventory.blockPos);
        if (blockEntity == null) return;
        if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
            IGrid grid = accessPoint.getGrid();
            if (grid == null) return;
            MEStorage networkInv = grid.getStorageService().getInventory();
            AEFluidKey fluidKey = AEFluidKey.of(returnedFluid);
            long amountInserted = networkInv.insert(fluidKey, returnedFluid.getAmount(), Actionable.SIMULATE, IActionSource.ofPlayer(player));
            if (amountInserted == returnedFluid.getAmount()) { //Only insert it all!
                networkInv.insert(fluidKey, returnedFluid.getAmount(), Actionable.MODULATE, IActionSource.ofPlayer(player));
                returnedFluid.shrink(returnedFluid.getAmount()); //Ensure it clears completely
            }
        }
    }
}

package com.direwolf20.buildinggadgets2.common.containers;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;


import javax.annotation.Nonnull;
import java.util.Objects;

public class TemplateManagerContainer extends BaseContainer {
    public static final String TEXTURE_LOC_SLOT_TOOL = BuildingGadgets2.MODID + ":textures/gui/slot_copy_paste_gadget.png";
    public static final String TEXTURE_LOC_SLOT_TEMPLATE = BuildingGadgets2.MODID + ":gui/slot_template";
    public static final int SLOTS = 2;
    private TemplateManagerBE be;

    public TemplateManagerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(Registration.TemplateManager_Container.get(), windowId);
        BlockPos pos = extraData.readBlockPos();

        this.be = (TemplateManagerBE) playerInventory.player.level().getBlockEntity(pos);
        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    public TemplateManagerContainer(int windowId, Inventory playerInventory, TemplateManagerBE tileEntity) {
        super(Registration.TemplateManager_Container.get(), windowId);
        this.be = Objects.requireNonNull(tileEntity);

        addOwnSlots();
        addPlayerSlots(playerInventory, -12, 70);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(ContainerLevelAccess.create(be.getLevel(), be.getBlockPos()), playerIn, Registration.TemplateManager.get());
    }

    private void addOwnSlots() {
        var cap = this.be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), be.getBlockState(), be, null);
        if (cap != null) {
            int x = 132;
            addSlot(new SlotTemplateManager(cap, 0, x, 18, TEXTURE_LOC_SLOT_TOOL));
            addSlot(new SlotTemplateManager(cap, 1, x, 63, TEXTURE_LOC_SLOT_TEMPLATE));
        }
    }

    @Override
    @Nonnull
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack currentStack = slot.getItem();
            itemstack = currentStack.copy();

            if (index < SLOTS) {
                if (!this.moveItemStackTo(currentStack, SLOTS, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(currentStack, 0, SLOTS, false)) {
                return ItemStack.EMPTY;
            }

            if (currentStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public TemplateManagerBE getTe() {
        return be;
    }

    public static class SlotTemplateManager extends SlotItemHandler {
        private String backgroundLoc;

        public SlotTemplateManager(IItemHandler itemHandler, int index, int xPosition, int yPosition, String backgroundLoc) {
            super(itemHandler, index, xPosition, yPosition);
            this.backgroundLoc = backgroundLoc;
            //this.setBackground(InventoryMenu.BLOCK_ATLAS, new ResourceLocation(this.backgroundLoc));
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}

package com.direwolf20.buildinggadgets2.common.containers.customhandler;

import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TemplateManagerHandler extends ItemStackHandler {
    TemplateManagerBE blockEntity;

    public TemplateManagerHandler(int size) {
        super(size);
    }

    public TemplateManagerHandler(int size, TemplateManagerBE blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        if (slot == 0)
            return (stack.getItem() instanceof GadgetCopyPaste || stack.getItem() instanceof GadgetCutPaste);
        if (slot == 1)
            return stack.is(Items.PAPER) || stack.is(Registration.Template.get()) || stack.is(Registration.Redprint.get());
        return false;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (blockEntity != null)
            blockEntity.setChanged();
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        return super.insertItem(slot, stack, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }
}

package com.direwolf20.buildinggadgets2.common.containers.customhandler;

import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class TemplateManagerHandler extends ItemStacksResourceHandler {
    public static final String STACKS_KEY = "stacks";
    public static final Codec<NonNullList<ItemStack>> STACKS_CODEC =
            ItemStack.OPTIONAL_CODEC.listOf().xmap(
                    list -> NonNullList.of(ItemStack.EMPTY, list.toArray(ItemStack[]::new)),
                    list -> list);

    private final TemplateManagerBE blockEntity;

    public TemplateManagerHandler(int size) {
        super(size);
        this.blockEntity = null;
    }

    public TemplateManagerHandler(int size, TemplateManagerBE blockEntity) {
        super(size);
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        if (resource.isEmpty()) return false;
        Item item = resource.getItem();
        if (index == 0)
            return item instanceof GadgetCopyPaste || item instanceof GadgetCutPaste;
        if (index == 1)
            return item == Items.PAPER || item == Registration.Template.get() || item == Registration.Redprint.get();
        return false;
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousContents) {
        if (blockEntity != null)
            blockEntity.setChanged();
    }

    public void writeToOutput(ValueOutput output) {
        output.store(STACKS_KEY, STACKS_CODEC, copyToList());
    }

    public void readFromInput(ValueInput input) {
        input.read(STACKS_KEY, STACKS_CODEC).ifPresent(this::setStacks);
    }
}

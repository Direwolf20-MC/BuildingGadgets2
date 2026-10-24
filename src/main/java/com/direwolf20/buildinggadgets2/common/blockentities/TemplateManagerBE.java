package com.direwolf20.buildinggadgets2.common.blockentities;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.containers.customhandler.TemplateManagerHandler;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;

import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer.SLOTS;

public class TemplateManagerBE extends BlockEntity implements MenuProvider {
    public final TemplateManagerHandler itemHandler = new TemplateManagerHandler(SLOTS, this);

    public TemplateManagerBE(BlockPos pos, BlockState state) {
        super(com.direwolf20.buildinggadgets2.setup.Registration.TemplateManager_BE.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null) return;
        for (int i = 0; i < itemHandler.size(); i++) {
            ItemResource slotResource = itemHandler.getResource(i);
            if (slotResource.isEmpty()) continue;
            ItemStack slotStack = slotResource.toStack(itemHandler.getAmountAsInt(i));
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), slotStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("Inventory").ifPresent(itemHandler::readFromInput);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.writeToOutput(output.child("Inventory"));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        this.loadAdditional(input);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveCustomOnly(provider);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput input) {
        this.loadAdditional(input);
    }

    public void markDirtyClient() {
        this.setChanged();
        if (this.getLevel() != null) {
            BlockState state = this.getLevel().getBlockState(this.getBlockPos());
            this.getLevel().sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("buildinggadgets2.screen.templatemanager");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
        return new TemplateManagerContainer(i, playerInventory, this);
    }
}

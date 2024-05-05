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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer.SLOTS;

public class TemplateManagerBE extends BlockEntity implements MenuProvider {
    //public static final TagKey<Item> TEMPLATE_CONVERTIBLES = TagKey.create(Registries.ITEM, ItemReference.TAG_TEMPLATE_CONVERTIBLE);
    //private final IItemHandler EMPTY = new ItemStackHandler(0);
    public final TemplateManagerHandler itemHandler = new TemplateManagerHandler(SLOTS, this);
    //public LazyOptional<TemplateManagerHandler> handlerLazyOptional;

    public TemplateManagerBE(BlockPos pos, BlockState state) {
        super(Registration.TemplateManager_BE.get(), pos, state);
        //handlerLazyOptional = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        if (tag.contains("Inventory"))
            itemHandler.deserializeNBT(provider, tag.getCompound("Inventory"));
        super.loadAdditional(tag, provider);
    }

    @Nonnull
    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("Inventory", itemHandler.serializeNBT(provider));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.loadAdditional(tag, lookupProvider);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
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

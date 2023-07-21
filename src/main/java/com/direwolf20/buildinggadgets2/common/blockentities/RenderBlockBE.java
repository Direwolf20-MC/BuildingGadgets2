package com.direwolf20.buildinggadgets2.common.blockentities;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class RenderBlockBE extends BlockEntity {
    public byte drawSize;
    public BlockState renderBlock;
    public CompoundTag blockEntityData;

    public RenderBlockBE(BlockPos pos, BlockState state) {
        super(Registration.RenderBlock_BE.get(), pos, state);
    }

    public void tickClient() {
        increaseDrawSize();
        if (drawSize >= 40)
            drawSize = 40;
    }

    public void tickServer() {
        increaseDrawSize();
        //markDirtyClient();
        if (drawSize >= 40) {
            level.setBlockAndUpdate(this.getBlockPos(), renderBlock);
            if (blockEntityData != null) {
                BlockEntity newBE = level.getBlockEntity(this.getBlockPos());
                try {
                    newBE.load(blockEntityData);
                } catch (Exception e) {
                    System.out.println("Failed to restore tile data for block at: " + this.getBlockPos() + " with NBT: " + blockEntityData + ". Consider adding it to the blacklist");
                }
            }
        }
    }

    public void increaseDrawSize() {
        //if (drawSize == 20) return;
        drawSize++;
    }

    public void setRenderBlock(BlockState state) {
        renderBlock = state;
        drawSize = 0;
        markDirtyClient();
    }

    public void setBlockEntityData(CompoundTag tag) {
        blockEntityData = tag;
        markDirtyClient();
    }

    /** Misc Methods for TE's */
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.renderBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("renderBlock"));
        this.drawSize = tag.getByte("drawSize");
        if (tag.contains("blockEntityData"))
            this.blockEntityData = tag.getCompound("blockEntityData");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.renderBlock != null) {
            tag.put("renderBlock", NbtUtils.writeBlockState(this.renderBlock));
        }
        tag.putByte("drawSize", this.drawSize);
        if (blockEntityData != null)
            tag.put("blockEntityData", this.blockEntityData);
    }

    @Nonnull
    @Override
    public AABB getRenderBoundingBox() {
        return new AABB(getBlockPos().above(10).north(10).east(10), getBlockPos().below(10).south(10).west(10));
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // Vanilla uses the type parameter to indicate which type of tile entity (command block, skull, or beacon?) is receiving the packet, but it seems like Forge has overridden this behavior
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() == null) return;
        this.load(pkt.getTag());
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
}

package com.direwolf20.buildinggadgets2.common.blockentities;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;

public class RenderBlockBE extends BlockEntity {
    public byte drawSize;
    public BlockState renderBlock;
    public BlockState sourceBlock;
    public BlockState targetBlock;
    public CompoundTag blockEntityData;
    public boolean shrinking;
    public boolean exchanging;

    public RenderBlockBE(BlockPos pos, BlockState state) {
        super(Registration.RenderBlock_BE.get(), pos, state);
    }

    public void tickClient() {
        increaseDrawSize();
    }

    public void tickServer() {
        increaseDrawSize();
        if (exchanging) {
            if (shrinking) {
                if (drawSize <= 0) {
                    shrinking = false;
                    renderBlock = targetBlock;
                    markDirtyClient();
                }
            } else {
                if (drawSize >= 40) {
                    setRealBlock(targetBlock);
                }
            }
        } else {
            if (shrinking) {
                if (drawSize <= 0) {
                    setRealBlock(Blocks.AIR.defaultBlockState());
                }
            } else {
                if (drawSize >= 40) {
                    setRealBlock(targetBlock);
                }
            }
        }
    }

    public void setRealBlock(BlockState realBlock) {
        level.setBlockAndUpdate(this.getBlockPos(), realBlock);
        if (blockEntityData != null) {
            BlockEntity newBE = level.getBlockEntity(this.getBlockPos());
            try {
                newBE.load(blockEntityData);
            } catch (Exception e) {
                System.out.println("Failed to restore tile data for block at: " + this.getBlockPos() + " with NBT: " + blockEntityData + ". Consider adding it to the blacklist");
            }
        }
    }

    public void increaseDrawSize() {
        if (shrinking) {
            drawSize--;
            if (drawSize <= 0)
                drawSize = 0;
        } else {
            drawSize++;
            if (drawSize >= 40)
                drawSize = 40;
        }
    }

    public byte nextDrawSize() {
        if (shrinking)
            return (byte) (drawSize - 1);
        return (byte) (drawSize + 1);
    }

    public void setRenderData(BlockState sourceBlock, BlockState targetBlock) {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
        if (sourceBlock.equals(Blocks.AIR.defaultBlockState())) { //If Source is air, we must be BUILDING!
            exchanging = false;
            shrinking = false;
            this.renderBlock = targetBlock;
            drawSize = 0;
        } else if (targetBlock.equals(Blocks.AIR.defaultBlockState())) { //If Target is air, we must be DESTROYING!
            exchanging = false;
            shrinking = true;
            this.renderBlock = sourceBlock;
            drawSize = 40;
        } else { //We must be EXCHANGING!
            exchanging = true;
            shrinking = true;
            this.renderBlock = sourceBlock;
            drawSize = 40;
        }

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
        this.sourceBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("sourceBlock"));
        this.targetBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("targetBlock"));
        this.shrinking = tag.getBoolean("shrinking");
        this.exchanging = tag.getBoolean("exchanging");
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
        if (this.sourceBlock != null) {
            tag.put("sourceBlock", NbtUtils.writeBlockState(this.sourceBlock));
        }
        if (this.targetBlock != null) {
            tag.put("targetBlock", NbtUtils.writeBlockState(this.targetBlock));
        }
        tag.putBoolean("shrinking", shrinking);
        tag.putBoolean("exchanging", exchanging);
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

package com.direwolf20.buildinggadgets2.common.blockentities;

import com.direwolf20.buildinggadgets2.client.particles.fluidparticle.FluidFlowParticleData;
import com.direwolf20.buildinggadgets2.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.List;
import java.util.Random;

import static com.direwolf20.buildinggadgets2.util.GadgetUtils.getSimpleItemForBlock;

public class RenderBlockBE extends BlockEntity {
    public byte drawSize;
    public BlockState renderBlock;
    public BlockState sourceBlock;
    public BlockState targetBlock;
    public CompoundTag blockEntityData;
    public boolean shrinking;
    public boolean exchanging;
    public byte renderType;

    public RenderBlockBE(BlockPos pos, BlockState state) {
        super(Registration.RENDER_BLOCK_BLOCK_ENTITY.get(), pos, state);
    }

    public void tickClient() {
        increaseDrawSize();
        if (renderType == 5)
            drawParticles();
    }

    public void drawParticles() {
        if (renderBlock == null) return;
        Random random = new Random();
        float nowScale = (float) (drawSize) / (float) getMaxSize();
        if (renderBlock.getFluidState().isEmpty()) {
            if (shrinking) {
                if (nowScale != 0 && nowScale < 0.5f) return;
                ItemStack itemStack = getSimpleItemForBlock(renderBlock);
                BlockPos startPos = getBlockPos();

                if (nowScale != 0) {
                    float yscale = Mth.lerp(nowScale, 0.75f, 1f);
                    ItemFlowParticleData data = new ItemFlowParticleData(itemStack, false, shrinking);
                    for (int i = 0; i < 2; i++) {
                        double randomX = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + yscale, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                } else {
                    ItemFlowParticleData data = new ItemFlowParticleData(itemStack, true, shrinking);
                    for (int i = 0; i < 100; i++) {
                        double randomX = random.nextFloat();
                        double randomY = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + randomY, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                }
            } else {
                ItemStack itemStack = getSimpleItemForBlock(renderBlock);
                BlockPos startPos = getBlockPos();
                if (nowScale < 0.05) {
                    ItemFlowParticleData data = new ItemFlowParticleData(itemStack, true, !shrinking);
                    for (int i = 0; i < 100; i++) {
                        double randomX = random.nextFloat();
                        double randomY = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + randomY, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                }

                if (nowScale > 0.5f) return;

                ItemFlowParticleData data = new ItemFlowParticleData(itemStack, false, shrinking);
                for (int i = 0; i < 2; i++) {
                    level.addParticle(data, startPos.getX() + 0.5f, startPos.getY() + +0.5f, startPos.getZ() + +0.5f, 0, 0, 0);
                }
            }
        } else {
            if (shrinking) {
                if (nowScale != 0 && nowScale < 0.5f) return;
                BlockPos startPos = getBlockPos();

                if (nowScale != 0) {
                    float yscale = Mth.lerp(nowScale, 0.75f, 1f);
                    FluidFlowParticleData data = new FluidFlowParticleData(new FluidStack(renderBlock.getFluidState().getType(), 1000), false, shrinking);
                    for (int i = 0; i < 2; i++) {
                        double randomX = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + yscale, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                } else {
                    FluidFlowParticleData data = new FluidFlowParticleData(new FluidStack(renderBlock.getFluidState().getType(), 1000), true, shrinking);
                    for (int i = 0; i < 100; i++) {
                        double randomX = random.nextFloat();
                        double randomY = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + randomY, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                }
            } else {
                BlockPos startPos = getBlockPos();
                if (nowScale < 0.05) {
                    FluidFlowParticleData data = new FluidFlowParticleData(new FluidStack(renderBlock.getFluidState().getType(), 1000), true, !shrinking);
                    for (int i = 0; i < 100; i++) {
                        double randomX = random.nextFloat();
                        double randomY = random.nextFloat();
                        double randomZ = random.nextFloat();
                        level.addParticle(data, startPos.getX() + randomX, startPos.getY() + randomY, startPos.getZ() + randomZ, 0, 0, 0);
                    }
                }
                if (nowScale > 0.5f) return;

                FluidFlowParticleData data = new FluidFlowParticleData(new FluidStack(renderBlock.getFluidState().getType(), 1000), false, shrinking);
                for (int i = 0; i < 2; i++) {
                    level.addParticle(data, startPos.getX() + 0.5f, startPos.getY() + +0.5f, startPos.getZ() + +0.5f, 0, 0, 0);
                }
            }
        }
    }

    public void tickServer() {
        increaseDrawSize();
        if (shrinking && drawSize <= 0) {
            if (exchanging) {
                shrinking = false;
                renderBlock = targetBlock;
                markDirtyClient();
            } else {
                setRealBlock(Blocks.AIR.defaultBlockState());
            }
        } else if (drawSize >= getMaxSize()) {
            setRealBlock(targetBlock);
        }
    }

    public byte getMaxSize() {
        if (renderType == 5)
            return 40;
        return 20;
    }

    public void setRealBlock(BlockState realBlock) {
        if (realBlock == null) { //This should never happen in theory, defensive coding
            level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
            return;
        }
        if (!realBlock.canSurvive(level, getBlockPos())) {
            List<ItemStack> drops = GadgetUtils.getDropsForBlockState((ServerLevel) level, getBlockPos(), realBlock, null);
            for (ItemStack returnedItem : drops) {
                ItemEntity itementity = new ItemEntity(level, getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ(), returnedItem);
                itementity.setPickUpDelay(40);
                level.addFreshEntity(itementity);
                level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
                return;
            }
        }
        BlockState adjustedState = Block.updateFromNeighbourShapes(realBlock, level, getBlockPos()); //Ensure double chests are placed as single chests if only 1 chest available in copy/paste, for example, or fixes fences
        level.setBlockAndUpdate(this.getBlockPos(), adjustedState);
        if (blockEntityData != null) {
            BlockEntity newBE = level.getBlockEntity(this.getBlockPos());
            try {
                newBE.loadCustomOnly(blockEntityData, level.registryAccess());
            } catch (Exception e) {
                System.out.println("Failed to restore tile data for block at: " + this.getBlockPos() + " with NBT: " + blockEntityData + ". Consider adding it to the blacklist");
            }
        }
    }

    public void increaseDrawSize() {
        //if (drawSize == 10) return;
        if (shrinking) {
            drawSize--;
            if (drawSize <= 0)
                drawSize = 0;
        } else {
            drawSize++;
            if (drawSize >= getMaxSize())
                drawSize = getMaxSize();
        }
    }

    public byte nextDrawSize() {
        if (shrinking)
            return (byte) (drawSize - 1);
        return (byte) (drawSize + 1);
    }

    public void setRenderData(BlockState sourceBlock, BlockState targetBlock, byte renderType) {
        this.sourceBlock = sourceBlock;
        this.targetBlock = targetBlock;
        this.renderType = renderType;
        if (sourceBlock.equals(Blocks.AIR.defaultBlockState())) { //If Source is air, we must be BUILDING!
            exchanging = false;
            shrinking = false;
            this.renderBlock = targetBlock;
            drawSize = 0;
        } else if (targetBlock.equals(Blocks.AIR.defaultBlockState())) { //If Target is air, we must be DESTROYING!
            exchanging = false;
            shrinking = true;
            this.renderBlock = sourceBlock;
            drawSize = getMaxSize();
        } else { //We must be EXCHANGING!
            exchanging = true;
            shrinking = true;
            this.renderBlock = sourceBlock;
            drawSize = getMaxSize();
        }

        markDirtyClient();
    }

    public void setBlockEntityData(CompoundTag tag) {
        blockEntityData = tag;
        markDirtyClient();
    }

    /** Misc Methods for TE's */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.renderBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("renderBlock"));
        this.sourceBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("sourceBlock"));
        this.targetBlock = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("targetBlock"));
        this.shrinking = tag.getBoolean("shrinking");
        this.exchanging = tag.getBoolean("exchanging");
        this.drawSize = tag.getByte("drawSize");
        this.renderType = tag.getByte("renderType");
        if (tag.contains("blockEntityData"))
            this.blockEntityData = tag.getCompound("blockEntityData");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
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
        tag.putByte("renderType", this.renderType);
        if (blockEntityData != null)
            tag.put("blockEntityData", this.blockEntityData);
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
}

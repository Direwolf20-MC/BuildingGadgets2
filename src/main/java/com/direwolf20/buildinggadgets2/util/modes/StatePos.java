package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;

public class StatePos {
    public BlockState state;
    public BlockPos pos;
    public boolean isModelRender;

    public StatePos(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
        this.isModelRender = isModelRender(state);
    }

    public StatePos(CompoundTag compoundTag) {
        if (!compoundTag.contains("blockstate") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compoundTag.getCompound("blockstate"));
        this.pos = NbtUtils.readBlockPos(compoundTag.getCompound("blockpos"));
        this.isModelRender = isModelRender(state);
    }

    public StatePos(CompoundTag compoundTag, ArrayList<BlockState> blockStates) {
        if (!compoundTag.contains("blockstateshort") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = blockStates.get(compoundTag.getShort("blockstateshort"));
        this.pos = NbtUtils.readBlockPos(compoundTag.getCompound("blockpos"));
    }

    public boolean isModelRender(BlockState state) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel ibakedmodel = dispatcher.getBlockModel(state);
        for (Direction direction : Direction.values()) {
            if (!ibakedmodel.getQuads(state, direction, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                return true;
            }
            if (!ibakedmodel.getQuads(state, null, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public CompoundTag getTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("blockstate", NbtUtils.writeBlockState(state));
        compoundTag.put("blockpos", NbtUtils.writeBlockPos(pos));
        return compoundTag;
    }

    public CompoundTag getTag(ArrayList<BlockState> blockStates) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putShort("blockstateshort", (short) blockStates.indexOf(state));
        compoundTag.put("blockpos", NbtUtils.writeBlockPos(pos));
        return compoundTag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePos) {
            return ((StatePos) obj).state.equals(this.state) && ((StatePos) obj).pos.equals(this.pos);
        }
        return false;
    }
}

package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class StatePos {
    public BlockState state;
    public BlockPos pos;

    public StatePos(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public StatePos(CompoundTag compoundTag) {
        if (!compoundTag.contains("blockstate") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compoundTag.getCompound("blockstate"));
        this.pos = NbtUtils.readBlockPos(compoundTag.getCompound("blockpos"));
    }

    public StatePos(CompoundTag compoundTag, ArrayList<BlockState> blockStates) {
        if (!compoundTag.contains("blockstateshort") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = blockStates.get(compoundTag.getShort("blockstateshort"));
        this.pos = BlockPos.of(compoundTag.getLong("blockpos"));
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
        compoundTag.putLong("blockpos", pos.asLong());
        return compoundTag;
    }

    public static ArrayList<BlockState> getBlockStateMap(ArrayList<StatePos> list) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (StatePos statePos : list) {
            if (!blockStateMap.contains(statePos.state))
                blockStateMap.add(statePos.state);
        }
        return blockStateMap;
    }

    public static ListTag getBlockStateNBT(ArrayList<BlockState> blockStateMap) {
        ListTag listTag = new ListTag();
        for (BlockState blockState : blockStateMap) {
            listTag.add(NbtUtils.writeBlockState(blockState));
        }
        return listTag;
    }

    public static ArrayList<BlockState> getBlockStateMapFromNBT(ListTag listTag) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), listTag.getCompound(i));
            blockStateMap.add(blockState);
        }
        return blockStateMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePos) {
            return ((StatePos) obj).state.equals(this.state) && ((StatePos) obj).pos.equals(this.pos);
        }
        return false;
    }
}

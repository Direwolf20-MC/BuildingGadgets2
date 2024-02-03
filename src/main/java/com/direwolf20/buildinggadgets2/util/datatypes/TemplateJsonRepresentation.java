package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.datagen.BG2BlockTags;
import com.direwolf20.buildinggadgets2.util.VecHelpers;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;

public class TemplateJsonRepresentation {
    /**
     * Supports the old format of building gadgets one
     */
    public static final int B1_BYTE_MASK = 0xFF;
    public static final int B2_BYTE_MASK = 0xFF_FF;
    public static final int B3_BYTE_MASK = 0xFF_FF_FF;

    public final JsonObject header;
    public final String body;

    private TemplateJsonRepresentation(JsonObject header, String body) {
        this.header = header;
        this.body = body;
    }

    public static ArrayList<StatePos> deserialize(CompoundTag nbt, BlockPos startPos, BlockPos endPos) {
        ArrayList<StatePos> statePosList = new ArrayList<>();
        ListTag posList = nbt.getList("pos", Tag.TAG_LONG);
        ListTag stateList = nbt.getList("data", Tag.TAG_COMPOUND);
        HashMap<BlockPos, BlockState> tempMap = new HashMap<>();

        for (Tag inbt : posList) {
            LongTag longNBT = (LongTag) inbt;
            BlockPos pos = posFromLong(longNBT.getAsLong());
            int stateID = readStateId(longNBT.getAsLong());
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), stateList.getCompound(stateID).getCompound("state"));
            tempMap.put(pos, blockState);
        }

        AABB area = VecHelpers.aabbFromBlockPos(startPos, endPos);
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            BlockState blockState = tempMap.getOrDefault(pos, Blocks.AIR.defaultBlockState());
            if (blockState.isAir()) {
                statePosList.add(new StatePos(blockState, pos));
                return;
            }

            if (blockState.is(BG2BlockTags.BG2DENY)) {
                statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                return;
            }
            if (blockState.getBlock().defaultDestroyTime() < 0) {
                statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                return;
            }
            if (!blockState.getFluidState().isEmpty() && !blockState.getFluidState().isSource()) {
                statePosList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos));
                return;
            }

            statePosList.add(new StatePos(blockState, pos));
        });

        return statePosList;
    }

    public static BlockPos posFromLong(long serialized) {
        int x = (int) ((serialized >> 24) & B2_BYTE_MASK);
        int y = (int) ((serialized >> 16) & B1_BYTE_MASK);
        int z = (int) (serialized & B2_BYTE_MASK);
        return new BlockPos(x, y, z);
    }

    public static int readStateId(long serialized) {
        return (int) ((serialized >> 40) & B3_BYTE_MASK);
    }
}

package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class BuildingUtils {
    public static void build(Level level, ArrayList<StatePos> blockPosList, BlockState blockState, BlockPos lookingAt) {
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (level.getBlockState(blockPos).isAir())
                level.setBlock(blockPos, blockState, 3);
        }
    }
}

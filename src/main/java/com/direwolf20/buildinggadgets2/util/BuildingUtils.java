package com.direwolf20.buildinggadgets2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BuildingUtils {
    public static void build(Level level, List<BlockPos> blockPosList, BlockState blockState) {
        for (BlockPos pos : blockPosList) {
            if (level.getBlockState(pos).isAir())
                level.setBlock(pos, blockState, 3);
        }
    }
}

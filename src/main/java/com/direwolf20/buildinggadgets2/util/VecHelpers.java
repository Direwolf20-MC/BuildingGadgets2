package com.direwolf20.buildinggadgets2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class VecHelpers {
    public static Vec3 blockPosToVec3(BlockPos pos) {
        return new Vec3(pos.getX(), pos.getY(), pos.getZ());
    }

    public static AABB aabbFromBlockPos(BlockPos start, BlockPos end) {
        return new AABB(blockPosToVec3(start), blockPosToVec3(end));
    }
}

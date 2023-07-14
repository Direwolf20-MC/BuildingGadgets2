package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class BuildingUtils {
    public static ArrayList<StatePos> build(Level level, ArrayList<StatePos> blockPosList, BlockPos lookingAt) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (level.getBlockState(blockPos).canBeReplaced()) {
                boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                actuallyBuiltList.add(new StatePos(pos.state, blockPos));
                be.setRenderBlock(pos.state);
            }
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> exchange(Level level, ArrayList<StatePos> blockPosList, BlockPos lookingAt) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        for (StatePos pos : blockPosList) {
            BlockPos blockPos = pos.pos.offset(lookingAt);
            //if (level.getBlockState(blockPos).isAir()) {
            boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            actuallyBuiltList.add(new StatePos(pos.state, blockPos));
            be.setRenderBlock(pos.state);
            //}
        }
        return actuallyBuiltList;
    }
}

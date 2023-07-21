package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Optional;

public class BuildingUtils {
    public static ArrayList<StatePos> build(Level level, ArrayList<StatePos> blockPosList, BlockPos lookingAt) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
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
            boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                continue;
            }
            actuallyBuiltList.add(new StatePos(pos.state, blockPos));
            be.setRenderBlock(pos.state);
        }
        return actuallyBuiltList;
    }

    public static ArrayList<StatePos> buildWithTileData(Level level, ArrayList<StatePos> blockPosList, BlockPos lookingAt, ArrayList<TagPos> teData) {
        ArrayList<StatePos> actuallyBuiltList = new ArrayList<>();
        if (teData == null) return actuallyBuiltList;
        for (StatePos pos : blockPosList) {
            if (pos.state.isAir()) continue; //Since we store air now
            BlockPos blockPos = pos.pos.offset(lookingAt);
            if (level.getBlockState(blockPos).canBeReplaced()) {
                Optional<TagPos> foundTagPos = teData.stream()
                        .filter(tagPos -> tagPos.pos.equals(pos.pos))
                        .findFirst();
                boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
                RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

                if (!placed || be == null) {
                    // this can happen when another mod rejects the set block state (fixes #120)
                    continue;
                }
                actuallyBuiltList.add(new StatePos(pos.state, blockPos));
                be.setRenderBlock(pos.state);
                if (foundTagPos.isPresent()) {
                    TagPos result = foundTagPos.get();
                    be.setBlockEntityData(result.tag);
                }
            }
        }
        return actuallyBuiltList;
    }
}

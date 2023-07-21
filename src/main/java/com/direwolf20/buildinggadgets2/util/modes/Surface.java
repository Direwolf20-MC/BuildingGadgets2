package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class Surface extends BaseMode {
    public Surface(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "surface");
    }

    @Override
    public ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        int bound = range / 2;
        Level level = player.level();

        ArrayList<StatePos> coordinates = new ArrayList<>();
        BlockState lookingAtState = level.getBlockState(start);
        BlockPos startAt = isExchanging ? start : start.above();
        AABB box = GadgetUtils.getSquareArea(startAt, hitSide, bound);

        boolean connected = GadgetNBT.getSetting(gadget, GadgetNBT.NBTValues.CONNECTED_AREA.value);
        if (connected) {
            Set<BlockPos> visitedBlocks = new HashSet<>(); //Blocks we've checked
            Queue<BlockPos> blocksToVisit = new LinkedList<>(); //Blocks we need to check
            blocksToVisit.offer(startAt); //Add the starting block to 'need to check'

            while (!blocksToVisit.isEmpty()) {
                BlockPos currentPos = blocksToVisit.poll(); //Get the current block to check
                BlockState currentBlock = level.getBlockState(currentPos);

                if (!visitedBlocks.contains(currentPos) && !currentBlock.isAir() && GadgetUtils.direContains(box, currentPos)) { //If we haven't added it already and its not air and its inside our bounding box add to the list to use
                    visitedBlocks.add(currentPos);

                    for (Direction direction : Direction.stream().filter(e -> !e.getAxis().equals(hitSide.getAxis())).toList()) { //Grab all the blocks around this one based on hitSide and add to the list to check out
                        BlockPos nextPos = currentPos.relative(direction);
                        if (GadgetUtils.direContains(box, nextPos)) { //Only if its inside our AABB box.
                            blocksToVisit.offer(nextPos);
                        }
                    }
                }
            }
            for (BlockPos pos : visitedBlocks) { //Of all the blocks we checked above, filter now based on validity
                if (isPosValid(level, pos) && isPosValidCustom(level, pos, lookingAtState, gadget))
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            }
        } else {
            BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
                if (isPosValid(level, pos) && isPosValidCustom(level, pos, lookingAtState, gadget))
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            });
        }

        return coordinates;
    }

    public boolean isPosValidCustom(Level level, BlockPos pos, BlockState compareState, ItemStack gadget) {
        boolean fuzzy = GadgetNBT.getSetting(gadget, GadgetNBT.NBTValues.FUZZY.value);
        if (isExchanging) {
            if (fuzzy) {
                if (level.getBlockState(pos).isAir()) return false;
            } else {
                if (!level.getBlockState(pos).equals(compareState)) return false;
            }
        } else {
            if (fuzzy) {
                if (level.getBlockState(pos.below()).isAir()) return false;
            } else {
                if (!level.getBlockState(pos.below()).equals(compareState)) return false;
            }
        }
        return true;
    }
}

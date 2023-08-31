package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
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
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        int bound = range / 2;
        Level level = player.level();

        ArrayList<StatePos> coordinates = new ArrayList<>();
        BlockState lookingAtState = level.getBlockState(start);
        BlockPos startAt = isExchanging ? start : start.relative(hitSide);
        AABB box = GadgetUtils.getSquareArea(startAt, hitSide, bound);

        boolean connected = GadgetNBT.getSetting(gadget, GadgetNBT.NBTValues.CONNECTED_AREA.value);
        if (connected) {
            Set<BlockPos> visitedBlocks = new HashSet<>(); //Blocks we've checked
            Queue<BlockPos> blocksToVisit = new LinkedList<>(); //Blocks we need to check
            blocksToVisit.offer(startAt); //Add the starting block to 'need to check'

            while (!blocksToVisit.isEmpty()) {
                BlockPos currentPos = blocksToVisit.poll(); //Get the current block to check
                BlockState currentBlock = level.getBlockState(currentPos);

                if (!visitedBlocks.contains(currentPos) && isPosValid(level, player, currentPos, state) && isPosValidCustom(level, currentPos, lookingAtState, gadget, hitSide) && GadgetUtils.direContains(box, currentPos)) { //If we haven't added it already and its not air and its inside our bounding box add to the list to use
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
                if (isPosValid(level, player, pos, state) && isPosValidCustom(level, pos, lookingAtState, gadget, hitSide))
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            }
        } else {
            BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
                if (isPosValid(level, player, pos, state) && isPosValidCustom(level, pos, lookingAtState, gadget, hitSide))
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            });
        }

        return coordinates;
    }

    public boolean isPosValidCustom(Level level, BlockPos pos, BlockState compareState, ItemStack gadget, Direction hitSide) {
        if (isExchanging) return true; //Handled by isExchangingValid
        boolean fuzzy = GadgetNBT.getSetting(gadget, GadgetNBT.NBTValues.FUZZY.value);
        BlockState belowState = level.getBlockState(pos.relative(hitSide.getOpposite()));
        if (fuzzy) {
            if (belowState.isAir()) return false;
        } else {
            if (!belowState.equals(compareState)) return false;
        }

        return true;
    }
}

package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

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

        //Todo isConnected
        //Todo isFuzzy

        ArrayList<StatePos> coordinates = new ArrayList<>();
        AABB box;
        if (isExchanging) {
            box = GadgetUtils.getSquareArea(start, hitSide, bound);
            BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
                if (isPosValid(level, pos) && !level.getBlockState(pos).isAir())
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            });
        } else {
            box = GadgetUtils.getSquareArea(start.above(), hitSide, bound);
            BlockState lookingAtState = level.getBlockState(start);
            BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
                if (isPosValid(level, pos) && level.getBlockState(pos.below()).equals(lookingAtState))
                    coordinates.add(new StatePos(state, pos.subtract(start)));
            });
        }


        return coordinates;
    }
}

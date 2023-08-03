package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class HorizontalRow extends BaseMode {
    public HorizontalRow(boolean exchanging) {
        super(exchanging);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "horizontal_row");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        ArrayList<StatePos> coordinates = new ArrayList<>();
        int range = GadgetNBT.getToolRange(gadget);
        boolean placeontop = GadgetNBT.getSetting(gadget, "placeontop");
        BlockPos startAt = placeontop ? start.above() : start;

        Direction side = hitSide.getAxis() == Direction.Axis.Y ? player.getDirection() : hitSide.getOpposite();
        if (!isExchanging) {
            for (int i = 0; i < range; i++)
                if (isPosValid(player.level(), startAt.relative(side, i), state))
                    coordinates.add(new StatePos(state, startAt.relative(side, i).subtract(start)));
        } else {
            side = side.getClockWise();
            int halfRange = range / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                if (isPosValid(player.level(), startAt.relative(side, i), state))
                    coordinates.add(new StatePos(state, startAt.relative(side, i).subtract(start)));
        }

        return coordinates;
    }
}

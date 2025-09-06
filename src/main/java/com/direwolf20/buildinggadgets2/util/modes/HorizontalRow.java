package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
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
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "horizontal_row");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        ArrayList<StatePos> coordinates = new ArrayList<>();
        int range = GadgetNBT.getToolRange(gadget);
        boolean placeontop = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName());
        BlockPos startAt = placeontop ? start.above() : start;

        Direction side = hitSide.getAxis() == Direction.Axis.Y ? player.getDirection() : hitSide.getOpposite();
        if (!isExchanging) {
            for (int i = 0; i < range; i++)
                if (isPosValid(player.level(), player, startAt.relative(side, i), state))
                    coordinates.add(new StatePos(state, startAt.relative(side, i).subtract(start)));
        } else {
            side = side.getClockWise();
            int halfRange = range / 2;
            for (int i = -halfRange; i <= halfRange; i++)
                if (isPosValid(player.level(), player, startAt.relative(side, i), state))
                    coordinates.add(new StatePos(state, startAt.relative(side, i).subtract(start)));
        }
        boolean connected = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.CONNECTED_AREA.getName());
        if (isExchanging && connected)
            return removeUnConnected(player.level(), player, startAt.subtract(start), coordinates, hitSide);
        return coordinates;
    }
}

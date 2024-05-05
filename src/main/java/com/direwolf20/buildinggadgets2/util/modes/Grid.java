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

public class Grid extends BaseMode {
    public Grid(boolean exchanging) {
        super(exchanging);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "grid");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        ArrayList<StatePos> coordinates = new ArrayList<>();
        int range = GadgetNBT.getToolRange(gadget) + 1;
        boolean placeontop = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName());
        BlockPos startAt = placeontop ? start.above() : start;

        for (int x = range * -7 / 5; x <= range * 7 / 5; x++) {
            for (int z = range * -7 / 5; z <= range * 7 / 5; z++) {
                if (x % (((range - 2) % 6) + 2) != 0 || z % (((range - 2) % 6) + 2) != 0)
                    continue;
                BlockPos coord = new BlockPos(startAt.getX() + x, startAt.getY(), startAt.getZ() + z);
                if (isPosValid(player.level(), player, coord, state))
                    coordinates.add(new StatePos(state, coord.subtract(start)));
            }
        }

        return coordinates;
    }
}

package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.MagicHelpers;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class HorizontalWall extends BaseMode {
    public HorizontalWall() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "horizontal_wall");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        int halfRange = range / 2;
        ArrayList<StatePos> coordinates = new ArrayList<>();
        boolean placeontop = GadgetNBT.getSetting(gadget, "placeontop");

        if (hitSide.getAxis() == Direction.Axis.Y) {
            for (int i = -halfRange; i <= halfRange; i++) {
                for (int j = -halfRange; j <= halfRange; j++) {
                    BlockPos coord = new BlockPos(start.getX() - i, start.getY() + (placeontop ? 1 : 0), start.getZ() + j);
                    if (isPosValid(player.level(), player, coord, state))
                        coordinates.add(new StatePos(state, coord.subtract(start)));
                }
            }

            return coordinates;
        }

        // Draw complete column then expand by half the range on both sides :D
        Direction.Axis axis = hitSide.getAxis();
        for (int i = 1; i < range + 1; i++) {
            for (int j = -halfRange; j <= halfRange; j++) {
                int value = MagicHelpers.invertOnFace(hitSide, i);
                if (isPosValid(player.level(), player, start.relative(hitSide, i), state)) {
                    BlockPos coord =
                            axis == Direction.Axis.X
                                    ? new BlockPos(start.getX() + value, start.getY(), start.getZ() + j)
                                    : new BlockPos(start.getX() + j, start.getY(), start.getZ() + value);
                    if (isPosValid(player.level(), player, coord, state))
                        coordinates.add(new StatePos(state, coord.subtract(start)));
                }
            }
        }
        return coordinates;
    }
}

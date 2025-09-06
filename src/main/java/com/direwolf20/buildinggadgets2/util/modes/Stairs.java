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

public class Stairs extends BaseMode {
    public Stairs() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "stairs");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        ArrayList<StatePos> coordinates = new ArrayList<>();
        boolean placeOnTop = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName());

        Direction side = hitSide;
        if (hitSide.getAxis() == Direction.Axis.Y)
            side = player.getDirection().getOpposite();
        int range = GadgetNBT.getToolRange(gadget);

        for (int i = 0; i < range; i++) {
            int shiftAxis = (i + 1) * (hitSide == Direction.EAST || hitSide == Direction.SOUTH ? 1 : -1);

            // Special case for looking at block under your feet and looking at the horizontal axis
            if (start.getY() < player.getY() && hitSide.getAxis().isHorizontal()) {
                boolean mutateXAxis = hitSide.getAxis() == Direction.Axis.X;
                boolean mutateZAxis = hitSide.getAxis() == Direction.Axis.Z;
                BlockPos coord = start.offset(
                        mutateXAxis ? shiftAxis : 0, // If we're hitting at the X axis we should shift it
                        placeOnTop ? -i : -(i + 1), // If place on top is on, we should place aside the block, otherwise, in the current one
                        mutateZAxis ? shiftAxis : 0 // If we're hitting at the Z axis we should shift the Z axis of the block.
                );
                if (isPosValid(player.level(), player, coord, state))
                    coordinates.add(new StatePos(state, coord.subtract(start)));

                continue;
            }

            shiftAxis = i * (side == Direction.EAST || side == Direction.SOUTH ? -1 : 1);
            if (start.getY() < player.getY() - 2) {
                shiftAxis = shiftAxis * -1;
            }
            BlockPos coord = start.offset(
                    side.getAxis() == Direction.Axis.X ? shiftAxis : 0,
                    (start.getY() > (player.getY() + 1) ? i * -1 : i) + (placeOnTop ? 1 : 0), // Check to see if we should build up or down from the player
                    side.getAxis() == Direction.Axis.Z ? shiftAxis : 0
            );
            if (isPosValid(player.level(), player, coord, state))
                coordinates.add(new StatePos(state, coord.subtract(start)));
        }

        return coordinates;
    }
}

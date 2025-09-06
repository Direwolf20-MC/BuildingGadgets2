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

public class VerticalColumn extends BaseMode {
    public VerticalColumn(boolean exchange) {
        super(exchange);
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "vertical_column");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        ArrayList<StatePos> coordinates = new ArrayList<>();

        // If up or down, full height from start block
        int halfRange = range / 2;

        if (hitSide.getAxis().equals(Direction.Axis.Y)) {
            // The exchanger handles the Y completely differently :sad: means more code
            if (isExchanging) {
                Direction playerFacing = player.getDirection();
                for (int i = -halfRange; i <= halfRange; i++)
                    if (isPosValid(player.level(), player, start.relative(playerFacing, i), state))
                        coordinates.add(new StatePos(state, BlockPos.ZERO.relative(playerFacing, i)));
            } else {
                for (int i = 1; i < range + 1; i++) {
                    if (isPosValid(player.level(), player, start.relative(hitSide, i), state))
                        coordinates.add(new StatePos(state, BlockPos.ZERO.relative(hitSide, i)));
                }
            }
            //Else, half and half
        } else {
            for (int i = -halfRange; i <= halfRange; i++) {
                if (isPosValid(player.level(), player, start.relative(Direction.UP, i), state))
                    coordinates.add(new StatePos(state, BlockPos.ZERO.relative(Direction.UP, i)));
            }
        }
        boolean connected = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.CONNECTED_AREA.getName());
        if (isExchanging && connected)
            return removeUnConnected(player.level(), player, start.subtract(start), coordinates, hitSide);
        return coordinates;
    }
}

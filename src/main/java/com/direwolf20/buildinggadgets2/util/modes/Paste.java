package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class Paste extends BaseMode {
    public Paste() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "paste");
    }

    @Override
    public ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();

        //Todo
        /*int startCoord = hitSide.getAxis().choose(start.getX(), start.getY(), start.getZ());
        int playerCoord = hitSide.getAxis().choose(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(32, Math.abs(startCoord - playerCoord)));
        for (int i = 1; i < difference; i++) {
            if (isPosValid(player.level(), start.relative(hitSide, i)))
                coordinates.add(new StatePos(state, BlockPos.ZERO.relative(hitSide, i)));
        }*/


        return coordinates;
    }
}

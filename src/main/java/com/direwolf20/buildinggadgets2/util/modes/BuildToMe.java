package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class BuildToMe extends BaseMode {
    public BuildToMe() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "build_to_me");
    }

    @Override
    public ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();

        int startCoord = hitSide.getAxis().choose(start.getX(), start.getY(), start.getZ());
        int playerCoord = hitSide.getAxis().choose(player.blockPosition().getX(), player.blockPosition().getY(), player.blockPosition().getZ());

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(32, Math.abs(startCoord - playerCoord))); //TODO Config
        for (int i = 1; i < difference; i++) {
            if (isPosValid(player.level(), start.relative(hitSide, i)))
                coordinates.add(new StatePos(state, BlockPos.ZERO.relative(hitSide, i)));
        }

        return coordinates;
    }
}

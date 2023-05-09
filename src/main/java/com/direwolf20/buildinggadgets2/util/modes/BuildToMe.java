package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class BuildToMe extends BaseMode {
    public BuildToMe() {
        super(false);
    }

    @Override
    public ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(hitSide);

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(player.blockPosition(), facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(32, Math.abs(startCoord - playerCoord))); //TODO Config
        for (int i = 0; i < difference; i++)
            coordinates.add(new StatePos(state, XYZ.extendPosSingle(i, BlockPos.ZERO, hitSide, facingXYZ)));

        return coordinates;
    }
}

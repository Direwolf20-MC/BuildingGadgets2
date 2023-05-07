package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildToMe extends BaseMode {
    public BuildToMe() {
        super(false);
    }

    @Override
    public List<BlockPos> collect(Direction hitSide, Player player, BlockPos start) {
        List<BlockPos> coordinates = new ArrayList<>();

        XYZ facingXYZ = XYZ.fromFacing(hitSide);

        int startCoord = XYZ.posToXYZ(start, facingXYZ);
        int playerCoord = XYZ.posToXYZ(player.blockPosition(), facingXYZ);

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(32, Math.abs(startCoord - playerCoord))); //TODO Config
        for (int i = 0; i < difference; i++)
            coordinates.add(XYZ.extendPosSingle(i, start, hitSide, facingXYZ));

        return coordinates;
    }
}

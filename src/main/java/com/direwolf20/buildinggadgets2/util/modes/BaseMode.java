package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public abstract class BaseMode {
    private boolean isExchanging;

    public BaseMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    abstract List<BlockPos> collect(Direction hitSide, Player player, BlockPos start);

}

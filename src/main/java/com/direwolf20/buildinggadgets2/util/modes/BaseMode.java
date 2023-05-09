package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public abstract class BaseMode {
    private boolean isExchanging;

    public BaseMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    abstract ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state);

}

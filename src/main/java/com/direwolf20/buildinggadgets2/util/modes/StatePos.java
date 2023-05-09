package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class StatePos {
    public BlockState state;
    public BlockPos pos;

    public StatePos(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePos) {
            return ((StatePos) obj).state.equals(this.state) && ((StatePos) obj).pos.equals(this.pos);
        }
        return false;
    }
}

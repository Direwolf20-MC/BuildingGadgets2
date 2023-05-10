package com.direwolf20.buildinggadgets2.util.modes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public abstract class BaseMode {
    /**
     * @deprecated all the modes should be possible without knowing the type of gadget... I'd hope
     */
    private boolean isExchanging;

    public abstract ResourceLocation getId();

    public BaseMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    public abstract ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state);
}

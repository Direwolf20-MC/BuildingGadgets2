package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class Cut extends BaseMode {
    public Cut() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "cut");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        //NoOp
        return coordinates;
    }
}

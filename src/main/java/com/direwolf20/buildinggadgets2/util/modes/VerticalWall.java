package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

public class VerticalWall extends BaseMode {
    public VerticalWall() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "vertical_wall");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        int size = range / 2;
        ArrayList<StatePos> coordinates = new ArrayList<>();

        Direction.Axis side = hitSide.getAxis() == Direction.Axis.Y ? player.getDirection().getOpposite().getAxis() : hitSide.getAxis();

        int startY = hitSide.getAxis() == Direction.Axis.Y ? start.getY() + 1 : start.getY() - size;
        int endY = hitSide.getAxis() == Direction.Axis.Y ? start.getY() + ((range - 1) * (hitSide == Direction.DOWN ? -1 : 1)) + 1 : start.getY() + size;

        AABB box = new AABB(
                start.getX() - (side == Direction.Axis.Z ? size : 0), startY, start.getZ() - (side == Direction.Axis.X ? size : 0),
                start.getX() + (side == Direction.Axis.Z ? size : 0), endY, start.getZ() + (side == Direction.Axis.X ? size : 0)
        );
        BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
            if (isPosValid(player.level(), pos, state))
                coordinates.add(new StatePos(state, pos.subtract(start)));
        });
        return coordinates;
    }
}

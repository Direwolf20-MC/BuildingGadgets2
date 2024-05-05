package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;

public class Surface extends BaseMode {
    public Surface(boolean isExchanging) {
        super(isExchanging);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "surface");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        int range = GadgetNBT.getToolRange(gadget);
        int bound = range / 2;
        Level level = player.level();

        ArrayList<StatePos> coordinates = new ArrayList<>();
        BlockState lookingAtState = level.getBlockState(start);
        BlockPos startAt = isExchanging ? start : start.relative(hitSide);
        AABB box = GadgetUtils.getSquareArea(startAt, hitSide, bound);
        BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(pos -> {
            if (isPosValid(level, player, pos, state) && isPosValidCustom(level, pos, lookingAtState, gadget, hitSide))
                coordinates.add(new StatePos(state, pos.subtract(start)));
        });

        boolean connected = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.CONNECTED_AREA.getName());
        if (isExchanging && connected)
            return removeUnConnected(level, player, startAt.subtract(start), coordinates, hitSide);
        return coordinates;
    }

    public boolean isPosValidCustom(Level level, BlockPos pos, BlockState compareState, ItemStack gadget, Direction hitSide) {
        if (isExchanging) return true; //Handled by isExchangingValid
        boolean fuzzy = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.FUZZY.getName());
        BlockState belowState = level.getBlockState(pos.relative(hitSide.getOpposite()));
        if (fuzzy) {
            if (belowState.isAir()) return false;
        } else {
            if (!belowState.equals(compareState)) return false;
        }

        return true;
    }
}

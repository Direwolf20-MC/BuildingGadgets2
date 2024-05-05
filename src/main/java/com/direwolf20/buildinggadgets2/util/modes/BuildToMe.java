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
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        ItemStack gadget = BaseGadget.getGadget(player);
        boolean placeontop = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName());

        int startCoord = hitSide.getAxis().choose(start.getX(), start.getY(), start.getZ());
        double yPos = placeontop ? player.getEyeY() : player.getBlockY();
        int playerCoord = hitSide.getAxis().choose(player.blockPosition().getX(), (int) yPos, player.blockPosition().getZ());

        // Clamp the value to the max range of the gadgets raytrace
        double difference = Math.max(0, Math.min(32, Math.abs(startCoord - playerCoord)));
        for (int i = 1; i < difference; i++) {
            if (isPosValid(player.level(), player, start.relative(hitSide, i), state))
                coordinates.add(new StatePos(state, BlockPos.ZERO.relative(hitSide, i)));
        }

        return coordinates;
    }
}

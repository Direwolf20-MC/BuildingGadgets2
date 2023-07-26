package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.stream.Stream;

public class Copy extends BaseMode {
    public Copy() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "copy");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) return coordinates; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return coordinates;

        AABB area = new AABB(copyStart, copyEnd);
        Stream<BlockPos> areaStream = BlockPos.betweenClosedStream(area);
        long size = areaStream.count();
        if (size > 100000) { //Todo Config?
            player.displayClientMessage(Component.literal("Area too large, max size is 100,000 blocks, size was: " + size), false);
            return coordinates;
        }
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos))
                coordinates.add(new StatePos(level.getBlockState(pos), pos.subtract(copyStart)));
            else
                coordinates.add(new StatePos(Blocks.AIR.defaultBlockState(), pos.subtract(copyStart))); //We need to have a block in EVERY position, so write air if invalid
        });
        return coordinates;
    }
}

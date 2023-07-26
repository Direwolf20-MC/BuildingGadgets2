package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Cut extends BaseMode {
    public Cut() {
        super(false);
    }

    @Override
    public ResourceLocation getId() {
        return new ResourceLocation(BuildingGadgets2.MODID, "cut");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCutPaste)) return coordinates; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return coordinates;

        AABB area = new AABB(copyStart, copyEnd);

        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            //if (isPosValidCustom(level, pos, heldItem))
            coordinates.add(new StatePos(level.getBlockState(pos), pos.subtract(copyStart)));
        });
        return coordinates;
    }

    public ArrayList<TagPos> collectTileData(Player player) {
        ArrayList<TagPos> teData = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCutPaste)) return teData; //Impossible....right?
        Level level = player.level();
        BlockPos cutStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos cutEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (cutStart.equals(GadgetNBT.nullPos) || cutEnd.equals(GadgetNBT.nullPos)) return teData;

        AABB area = new AABB(cutStart, cutEnd);

        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                TagPos tagPos = new TagPos(blockTag, pos.subtract(cutStart));
                teData.add(tagPos);
            }
        });
        return teData;
    }

    public void removeBlocks(Player player) {
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCutPaste)) return; //Impossible....right?
        Level level = player.level();
        BlockPos cutStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos cutEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (cutStart.equals(GadgetNBT.nullPos) || cutEnd.equals(GadgetNBT.nullPos)) return;

        AABB area = new AABB(cutStart, cutEnd);
        BuildingUtils.remove(level, player, BlockPos.betweenClosedStream(area).map(BlockPos::immutable).collect(Collectors.toList()), false);
    }


    public boolean isPosValidCustom(Level level, BlockPos pos, ItemStack gadget) {
        if (!GadgetUtils.isValidBlockState(level.getBlockState(pos)))
            return false; //Don't Copy Air or other invalid states
        //Todo more validations!
        return true;
    }
}

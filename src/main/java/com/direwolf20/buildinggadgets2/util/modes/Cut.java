package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.datagen.BG2BlockTags;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.stream.Stream;

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
        if (!(heldItem.getItem() instanceof GadgetCutPaste gadgetCutPaste)) return coordinates; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return coordinates;

        AABB area = new AABB(copyStart, copyEnd);

        Stream<BlockPos> areaStream = BlockPos.betweenClosedStream(area);
        long size = areaStream.count();
        int maxSize = 100000; //Todo Config?
        if (size > maxSize) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.areatoolarge", maxSize, size), false);
            return coordinates;
        }

        int totalCost = gadgetCutPaste.getEnergyCost() * (int) size;
        if (!player.isCreative() && !BuildingUtils.hasEnoughEnergy(heldItem, totalCost)) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.notenoughenergy", totalCost, BuildingUtils.getEnergyStored(heldItem)), false);
            return coordinates;
        }

        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos) && customCutValidation(level.getBlockState(pos)))
                coordinates.add(new StatePos(level.getBlockState(pos), pos.subtract(copyStart)));
            else
                coordinates.add(new StatePos(Blocks.AIR.defaultBlockState(), pos.subtract(copyStart))); //We need to have a block in EVERY position, so write air if invalid
        });
        return coordinates;
    }

    public boolean customCutValidation(BlockState blockState) {
        if (blockState.is(BG2BlockTags.NO_MOVE)) return false;
        return true;
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
        if (!(heldItem.getItem() instanceof GadgetCutPaste gadgetCutPaste)) return; //Impossible....right?
        Level level = player.level();
        BlockPos cutStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos cutEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (cutStart.equals(GadgetNBT.nullPos) || cutEnd.equals(GadgetNBT.nullPos)) return;

        AABB area = new AABB(cutStart, cutEnd);
        ArrayList<BlockPos> removeList = new ArrayList<>();
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos) && customCutValidation(level.getBlockState(pos)))
                removeList.add(pos);
        });
        BuildingUtils.remove(level, player, removeList, false, false, heldItem);
    }
}

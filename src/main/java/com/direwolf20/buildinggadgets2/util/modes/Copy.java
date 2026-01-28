package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.VecHelpers;
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

public class Copy extends BaseMode {
    private ArrayList<TagPos> collectedTEData = new ArrayList<>();
    
    public Copy() {
        super(false);
    }
    
    public ArrayList<TagPos> getCollectedTEData() {
        return collectedTEData;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2.MODID, "copy");
    }

    @Override
    public ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ArrayList<StatePos> coordinates = new ArrayList<>();
        collectedTEData = new ArrayList<>();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCopyPaste)) return coordinates; //Impossible....right?
        Level level = player.level();
        BlockPos copyStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos copyEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (copyStart.equals(GadgetNBT.nullPos) || copyEnd.equals(GadgetNBT.nullPos)) return coordinates;

        AABB area = VecHelpers.aabbFromBlockPos(copyStart, copyEnd);
        int maxAxis = 500; //Todo Config?
        if (area.getXsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "x", maxAxis, area.getXsize()), false);
            return coordinates;
        }
        if (area.getYsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "y", maxAxis, area.getYsize()), false);
            return coordinates;
        }
        if (area.getZsize() > maxAxis) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.axistoolarge", "z", maxAxis, area.getZsize()), false);
            return coordinates;
        }
        Stream<BlockPos> areaStream = BlockPos.betweenClosedStream(area);
        long size = areaStream.count();
        int maxSize = 100000;
        if (size > maxSize) { //Todo Config?
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.areatoolarge", maxSize, size), false);
            return coordinates;
        }
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            BlockState blockState = level.getBlockState(pos);
            boolean isValid = GadgetUtils.isValidBlockState(blockState, level, pos);
            boolean isRenderBlock = blockState.getBlock() instanceof RenderBlock;
            
            if (isValid && !isRenderBlock) {
                coordinates.add(new StatePos(GadgetUtils.cleanBlockState(blockState), pos.subtract(copyStart)));
                
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity != null) {
                    CompoundTag blockTag = blockEntity.saveWithFullMetadata(level.registryAccess());
                    TagPos tagPos = new TagPos(blockTag, pos.subtract(copyStart));
                    collectedTEData.add(tagPos);
                }
            } else {
                coordinates.add(new StatePos(Blocks.AIR.defaultBlockState(), pos.subtract(copyStart))); //We need to have a block in EVERY position, so write air if invalid
            }
        });
        return coordinates;
    }
}

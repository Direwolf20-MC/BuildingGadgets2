package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.concurrent.LinkedBlockingQueue;

public class GadgetUtils {
    // TODO: migrate to tags
    private static final ImmutableList<Block> DISALLOWED_BLOCKS = ImmutableList.of(
            Blocks.END_PORTAL, Blocks.NETHER_PORTAL, Blocks.END_PORTAL_FRAME, Blocks.BEDROCK, Blocks.SPAWNER
    );
    private static final ImmutableList<Property> ALLOWED_PROPERTIES = ImmutableList.of(
            BlockStateProperties.FACING, BlockStateProperties.AXIS, BlockStateProperties.HORIZONTAL_FACING
    );

    public static boolean isValidBlockState(BlockState blockState) {
        if (blockState.isAir()) return false;
        if (DISALLOWED_BLOCKS.contains(blockState.getBlock())) return false;
        //TODO: Destroy Speed < 0
        return true;
    }

    public static boolean setBlockState(ItemStack gadget, BlockState blockState) {
        if (!GadgetUtils.isValidBlockState(blockState)) return false;
        BlockState placeState = cleanBlockState(blockState);
        Item item = Item.byBlock(placeState.getBlock());
        System.out.println(item);
        GadgetNBT.setGadgetBlockState(gadget, placeState);
        System.out.println(placeState);
        return true;
    }

    public static BlockState cleanBlockState(BlockState sourceState) {
        BlockState placeState = sourceState.getBlock().defaultBlockState();
        for (Property<?> prop : sourceState.getProperties()) {
            if (ALLOWED_PROPERTIES.contains(prop)) {
                placeState = applyProperty(placeState, sourceState, prop);
            }
        }
        return placeState;
    }

    public static void addToUndoList(ItemStack gadget, ArrayList<StatePos> buildList) {
        ArrayList<BlockState> blockStates = new ArrayList<>(GadgetNBT.getBlockMap(gadget)); //Use array list so we can lookup position, etc
        ListTag listTag = new ListTag();
        for (StatePos statePos : buildList) {
            if (!blockStates.contains(statePos.state))
                blockStates.add(statePos.state);
            listTag.add(statePos.getTag(blockStates));
        }
        GadgetNBT.setBlockMap(gadget, new LinkedHashSet<>(blockStates)); //Convert back to LinkedHashSet to remove duplicates automatically, even though there shouldn't be any....
        GadgetNBT.addToUndoList(gadget, listTag);
    }

    public static ArrayList<StatePos> getLastUndo(ItemStack gadget) {
        ArrayList<StatePos> undoList = new ArrayList<>();
        LinkedHashSet<BlockState> blockStates = GadgetNBT.getBlockMap(gadget);
        LinkedBlockingQueue<ListTag> undoTagList = GadgetNBT.getUndoList(gadget);
        ListTag latestUndo = undoTagList.poll(); //TODO Confirm we wanna remove at this point
        for (int i = 0; i < latestUndo.size(); i++) {
            StatePos statePos = new StatePos(latestUndo.getCompound(i), new ArrayList<>(blockStates));
            undoList.add(statePos);
        }
        return undoList;
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, BlockState from, Property<T> prop) {
        return state.setValue(prop, from.getValue(prop));
    }
}

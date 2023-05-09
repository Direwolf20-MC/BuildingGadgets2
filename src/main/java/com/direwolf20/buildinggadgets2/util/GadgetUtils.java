package com.direwolf20.buildinggadgets2.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

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

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, BlockState from, Property<T> prop) {
        return state.setValue(prop, from.getValue(prop));
    }
}

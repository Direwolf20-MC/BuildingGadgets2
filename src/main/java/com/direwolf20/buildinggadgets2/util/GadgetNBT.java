package com.direwolf20.buildinggadgets2.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class GadgetNBT {
    public static BlockState setGadgetBlockState(ItemStack gadget, BlockState blockState) {
        CompoundTag tag = gadget.getOrCreateTag();
        tag.put("blockstate", NbtUtils.writeBlockState(blockState));
        return blockState;
    }

    public static BlockState getGadgetBlockState(ItemStack gadget) {
        CompoundTag tag = gadget.getTag();
        if (tag == null || !tag.contains("blockstate")) return Blocks.AIR.defaultBlockState();
        return NbtUtils.readBlockState(tag.getCompound("blockstate"));
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("raytrace_fluid");
    }

    public static void toggleRayTraceFluid(ServerPlayer player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean("raytrace_fluid", !shouldRayTraceFluid(stack));
        //player.displayClientMessage(MessageTranslation.RAYTRACE_FLUID.componentTranslation(shouldRayTraceFluid(stack)).setStyle(Styles.AQUA), true);
    }

    public static void setToolRange(ItemStack stack, int range) {
        //Store the tool's range in NBT as an Integer
        CompoundTag tagCompound = stack.getOrCreateTag();
        tagCompound.putInt("range", range);
    }

    public static int getToolRange(ItemStack stack) {
        CompoundTag tagCompound = stack.getOrCreateTag();
        return Mth.clamp(tagCompound.getInt("range"), 1, 15); //TODO Config
    }

    public static boolean getFuzzy(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("fuzzy");
    }

    public static void toggleFuzzy(Player player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean("fuzzy", !getFuzzy(stack));
        //player.displayClientMessage(MessageTranslation.FUZZY_MODE.componentTranslation(getFuzzy(stack)).setStyle(Styles.AQUA), true);
    }
}

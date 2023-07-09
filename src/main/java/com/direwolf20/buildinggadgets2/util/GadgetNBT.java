package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.concurrent.LinkedBlockingQueue;

public class GadgetNBT {
    final static int undoListSize = 10;

    public static BlockState setGadgetBlockState(ItemStack gadget, BlockState blockState) {
        CompoundTag tag = gadget.getOrCreateTag();
        tag.put("blockstate", NbtUtils.writeBlockState(blockState));
        return blockState;
    }

    public static BlockState getGadgetBlockState(ItemStack gadget) {
        CompoundTag tag = gadget.getTag();
        if (tag == null || !tag.contains("blockstate")) return Blocks.AIR.defaultBlockState();
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("blockstate"));
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean("raytrace_fluid");
    }

    public static void toggleRayTraceFluid(ServerPlayer player, ItemStack stack) {
        stack.getOrCreateTag().putBoolean("raytrace_fluid", !shouldRayTraceFluid(stack));
        //player.displayClientMessage(MessageTranslation.RAYTRACE_FLUID.componentTranslation(shouldRayTraceFluid(stack)).setStyle(Styles.AQUA), true);
    }

    public static LinkedHashSet<BlockState> getBlockMap(ItemStack gadget) {
        LinkedHashSet<BlockState> blockMap = new LinkedHashSet<>();
        CompoundTag tag = gadget.getOrCreateTag();
        if (!tag.contains("blockmap")) return blockMap;
        ListTag listTag = tag.getList("blockmap", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), listTag.getCompound(i));
            blockMap.add(blockState);
        }
        return blockMap;
    }

    //Todo Cleanup Block Map? This could grow over time....
    public static void setBlockMap(ItemStack gadget, LinkedHashSet<BlockState> blockMap) {
        CompoundTag tag = gadget.getOrCreateTag();
        ListTag listTag = new ListTag();
        for (BlockState blockState : blockMap) {
            CompoundTag comp = NbtUtils.writeBlockState(blockState);
            listTag.add(comp);
        }
        tag.put("blockmap", listTag);
    }

    public static LinkedBlockingQueue<ListTag> getUndoList(ItemStack gadget) {
        LinkedBlockingQueue<ListTag> undoList = new LinkedBlockingQueue<>(undoListSize);
        CompoundTag tag = gadget.getOrCreateTag();
        if (!tag.contains("undolist")) return undoList;
        ListTag undoListTag = tag.getList("undolist", Tag.TAG_LIST);
        for (int i = 0; i < undoListTag.size(); i++) {
            ListTag listTag = undoListTag.getList(i);
            undoList.offer(listTag);
        }
        return undoList;
    }

    public static void addToUndoList(ItemStack gadget, ListTag listTag) {
        LinkedBlockingQueue<ListTag> undoList = getUndoList(gadget);
        boolean added = undoList.offer(listTag);
        if (!added) {
            undoList.poll();  // Remove the head of the queue.
            undoList.offer(listTag);  // Try adding the element again.
        }
        CompoundTag tag = gadget.getOrCreateTag();
        ListTag undoListTag = new ListTag();
        undoListTag.addAll(undoList);
        tag.put("undolist", undoListTag);
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

    /**
     * Safely get a mode based on the given mode id. When one is not present, we'll default to the first one in the list.
     *
     * @param stack the gadget
     * @return the correct mode for the gadget based on the gadget modes registry
     */
    public static BaseMode getMode(ItemStack stack) {
        // Checks if the current item if a gadget, if it's not, throw the game! You shouldn't be using this if you're not a gadget!
        Preconditions.checkArgument(stack.getItem() instanceof BaseGadget, "You can not get a mode of a non-gadget item");

        String mode = stack.getOrCreateTag().getString("mode");
        GadgetTarget gadgetTarget = ((BaseGadget) stack.getItem()).gadgetTarget();

        ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(gadgetTarget);
        if (mode.isEmpty()) {
            return modesForGadget.first();
        }

        var id = new ResourceLocation(mode);
        return modesForGadget.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(modesForGadget.first());
    }

    public static void setMode(ItemStack gadget, BaseMode mode) {
        gadget.getOrCreateTag().putString("mode", mode.getId().toString());
    }
}

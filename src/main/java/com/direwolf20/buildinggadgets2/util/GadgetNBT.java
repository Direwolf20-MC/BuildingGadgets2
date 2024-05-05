package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.items.*;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.BG2DataComponents;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class GadgetNBT {
    public enum ToggleableSettings {
        AFFECT_TILES,
        RAYTRACE_FLUID,
        PLACE_ON_TOP,
        PASTE_REPLACE,
        BIND,
        FUZZY,
        CONNECTED_AREA;

        public static ToggleableSettings byName(String name) {
            return ToggleableSettings.valueOf(name.toUpperCase(Locale.ROOT));
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    public enum IntSettings {
        BIND_DIRECTION,
        LEFT,
        RIGHT,
        UP,
        DOWN,
        DEPTH;

        public static IntSettings byName(String name) {
            return IntSettings.valueOf(name.toUpperCase(Locale.ROOT));
        }

        public String getName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /*public enum NBTValues {
        FUZZY("fuzzy"),
        CONNECTED_AREA("connected_area");

        public final String value;

        NBTValues(String value) {
            this.value = value;
        }
    }*/

    public enum RenderTypes {
        GROW("buildinggadgets2.grow"),
        FADE("buildinggadgets2.fade"),
        SQUISH("buildinggadgets2.squish"),
        GROWUP("buildinggadgets2.growup"),
        RISEUP("buildinggadgets2.riseup"),
        SNAP("buildinggadgets2.snap"),
        ;

        private final String lang;

        RenderTypes(String lang) {
            this.lang = lang;
        }

        public RenderTypes next() {
            // This will return the next value, wrapping around to the start if necessary
            return values()[(this.ordinal() + 1) % values().length];
        }

        public byte getPosition() {
            return (byte) this.ordinal();
        }

        public String getLang() {
            return lang;
        }

        public static RenderTypes getByOrdinal(byte ordinal) {
            return RenderTypes.values()[ordinal];
        }
    }

    public final static BlockPos nullPos = new BlockPos(-999, -999, -999);
    final static int undoListSize = 10;

    public static void setBoundPos(ItemStack gadget, GlobalPos globalPos) {
        gadget.set(BG2DataComponents.BOUND_GLOBAL_POS, globalPos);
    }

    public static GlobalPos getBoundPos(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.BOUND_GLOBAL_POS, null);
    }

    public static void clearBoundPos(ItemStack gadget) {
        gadget.remove(BG2DataComponents.BOUND_GLOBAL_POS);
    }

    public static void setAnchorPos(ItemStack gadget, BlockPos blockPos) {
        gadget.set(BG2DataComponents.ANCHOR_POS, blockPos);
    }

    public static void setRenderType(ItemStack gadget, byte renderType) {
        gadget.set(BG2DataComponents.RENDER_TYPE, renderType);
    }

    public static byte getRenderTypeByte(ItemStack stack) {
        return stack.getOrDefault(BG2DataComponents.RENDER_TYPE, 0).byteValue();
    }

    public static RenderTypes getRenderType(ItemStack stack) {
        return RenderTypes.getByOrdinal(getRenderTypeByte(stack));
    }

    public static BlockPos getAnchorPos(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.ANCHOR_POS, nullPos);
    }

    public static void clearAnchorPos(ItemStack gadget) {
        gadget.remove(BG2DataComponents.ANCHOR_POS);
        gadget.remove(BG2DataComponents.ANCHOR_LIST);
        gadget.remove(BG2DataComponents.ANCHOR_SIDE);
    }

    public static List<BlockPos> getAnchorList(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.ANCHOR_LIST, new ArrayList<>());
    }

    public static void setAnchorList(ItemStack gadget, ArrayList<BlockPos> anchorList) {
        gadget.set(BG2DataComponents.ANCHOR_LIST, anchorList);
    }

    public static void setAnchorSide(ItemStack stack, Direction side) {
        if (side == null)
            stack.remove(BG2DataComponents.ANCHOR_SIDE);
        else
            stack.set(BG2DataComponents.ANCHOR_SIDE, side.ordinal());
    }

    public static Direction getAnchorSide(ItemStack stack) {
        if (!stack.has(BG2DataComponents.ANCHOR_SIDE)) return null;
        return Direction.values()[stack.get(BG2DataComponents.ANCHOR_SIDE)];
    }

    public static void setCopyStartPos(ItemStack gadget, BlockPos blockPos) {
        gadget.set(BG2DataComponents.COPY_START_POS, blockPos);
    }

    public static BlockPos getCopyStartPos(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.COPY_START_POS, nullPos);
    }

    public static void setRelativePaste(ItemStack gadget, BlockPos blockPos) {
        gadget.set(BG2DataComponents.RELATIVE_PASTE, blockPos);
    }

    public static BlockPos getRelativePaste(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.RELATIVE_PASTE, BlockPos.ZERO);
    }

    public static void setCopyEndPos(ItemStack gadget, BlockPos blockPos) {
        gadget.set(BG2DataComponents.COPY_END_POS, blockPos);
    }

    public static BlockPos getCopyEndPos(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.COPY_END_POS, nullPos);
    }

    public static UUID setUUID(ItemStack gadget) {
        UUID uuid = UUID.randomUUID();
        gadget.set(BG2DataComponents.GADGET_UUID, uuid);
        return uuid;
    }

    public static UUID getUUID(ItemStack gadget) {
        if (!gadget.has(BG2DataComponents.GADGET_UUID))
            return setUUID(gadget);
        return gadget.get(BG2DataComponents.GADGET_UUID);
    }

    public static UUID setCopyUUID(ItemStack gadget) {
        UUID uuid = UUID.randomUUID();
        gadget.set(BG2DataComponents.COPY_UUID, uuid);
        return uuid;
    }

    public static UUID setCopyUUID(ItemStack gadget, UUID uuid) {
        gadget.set(BG2DataComponents.COPY_UUID, uuid);
        return uuid;
    }

    public static UUID getCopyUUID(ItemStack gadget) {
        if (!gadget.has(BG2DataComponents.COPY_UUID))
            return setCopyUUID(gadget);
        return gadget.get(BG2DataComponents.COPY_UUID);
    }

    public static boolean hasCopyUUID(ItemStack gadget) {
        return gadget.has(BG2DataComponents.COPY_UUID);
    }

    public static void clearCopyUUID(ItemStack gadget) {
        gadget.remove(BG2DataComponents.COPY_UUID);
    }

    public static void setGadgetBlockState(ItemStack gadget, BlockState blockState) {
        gadget.set(BG2DataComponents.GADGET_BLOCKSTATE, blockState);
    }

    public static BlockState getGadgetBlockState(ItemStack gadget) {
        return gadget.getOrDefault(BG2DataComponents.GADGET_BLOCKSTATE, Blocks.AIR.defaultBlockState());
    }

    public static boolean shouldRayTraceFluid(ItemStack stack) {
        return getSetting(stack, ToggleableSettings.RAYTRACE_FLUID.getName());
    }

    public static LinkedList<UUID> getUndoList(ItemStack gadget) {
        return new LinkedList<>(gadget.getOrDefault(BG2DataComponents.UNDO_LIST, new ArrayList<>()));
    }

    public static void setUndoList(ItemStack gadget, LinkedList<UUID> undoList) {
        gadget.set(BG2DataComponents.UNDO_LIST, undoList);
    }

    public static void addToUndoList(ItemStack gadget, UUID uuid, BG2Data bg2Data) {
        LinkedList<UUID> undoList = getUndoList(gadget);
        if (undoList.size() >= undoListSize) {
            UUID removal = undoList.removeFirst();
            bg2Data.removeFromUndoList(removal);
        }
        undoList.add(uuid);
        setUndoList(gadget, undoList);
    }

    public static UUID peekUndoList(ItemStack gadget) {
        LinkedList<UUID> undoList = getUndoList(gadget);
        if (undoList.isEmpty()) return null;
        return undoList.getLast();
    }

    public static UUID popUndoList(ItemStack gadget) {
        LinkedList<UUID> undoList = getUndoList(gadget);
        if (undoList.isEmpty()) return null;
        UUID uuid = undoList.removeLast();
        setUndoList(gadget, undoList);
        return uuid;
    }

    public static boolean toggleSetting(ItemStack stack, String setting) {
        ToggleableSettings toggleableSetting = ToggleableSettings.byName(setting);
        stack.update(BG2DataComponents.SETTING_TOGGLES.get(toggleableSetting), false, k -> !k);
        return stack.getOrDefault(BG2DataComponents.SETTING_TOGGLES.get(toggleableSetting), false);
    }

    public static boolean getSetting(ItemStack stack, String setting) {
        ToggleableSettings toggleableSetting = ToggleableSettings.byName(setting);
        return stack.getOrDefault(BG2DataComponents.SETTING_TOGGLES.get(toggleableSetting), false);
    }

    public static boolean getPasteReplace(ItemStack stack) {
        if (!stack.has(BG2DataComponents.SETTING_TOGGLES.get(ToggleableSettings.PASTE_REPLACE))) {
            if (stack.getItem() instanceof GadgetCutPaste)
                return toggleSetting(stack, ToggleableSettings.PASTE_REPLACE.getName()); //Make PasteReplace true by default for cut/paste gadget
            else
                return false;
        }
        return getSetting(stack, ToggleableSettings.PASTE_REPLACE.getName());
    }

    public static void setToolRange(ItemStack stack, int range) {
        stack.set(BG2DataComponents.GADGET_RANGE, range);
    }

    public static int getToolRange(ItemStack stack) {
        return stack.getOrDefault(BG2DataComponents.GADGET_RANGE, 1);
    }

    public static void setToolValue(ItemStack stack, int value, String valueName) {
        stack.set(BG2DataComponents.SETTING_VALUES.get(IntSettings.byName(valueName)), value);
    }

    public static int getToolValue(ItemStack stack, String valueName) {
        return stack.getOrDefault(BG2DataComponents.SETTING_VALUES.get(IntSettings.byName(valueName)), 0);
    }

    public static void setTemplateName(ItemStack stack, String name) {
        stack.set(BG2DataComponents.TEMPLATE_NAME, name);
    }

    public static String getTemplateName(ItemStack stack) {
        return stack.getOrDefault(BG2DataComponents.TEMPLATE_NAME, "");
    }

    public static boolean getFuzzy(ItemStack stack) {
        return getSetting(stack, ToggleableSettings.FUZZY.getName());
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

        String mode = stack.getOrDefault(BG2DataComponents.GADGET_MODE, "");
        GadgetTarget gadgetTarget = ((BaseGadget) stack.getItem()).gadgetTarget();

        ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(gadgetTarget);
        if (mode.isEmpty()) {
            if (stack.getItem() instanceof GadgetBuilding)
                return modesForGadget.stream()
                        .filter(m -> m.getId().getPath().equals("build_to_me"))
                        .findFirst()
                        .orElse(modesForGadget.first());
            if (stack.getItem() instanceof GadgetExchanger)
                return modesForGadget.stream()
                        .filter(m -> m.getId().getPath().equals("surface"))
                        .findFirst()
                        .orElse(modesForGadget.first());
            if (stack.getItem() instanceof GadgetCutPaste)
                return modesForGadget.stream()
                        .filter(m -> m.getId().getPath().equals("cut"))
                        .findFirst()
                        .orElse(modesForGadget.first());
            if (stack.getItem() instanceof GadgetCopyPaste)
                return modesForGadget.stream()
                        .filter(m -> m.getId().getPath().equals("copy"))
                        .findFirst()
                        .orElse(modesForGadget.first());
            return modesForGadget.first();
        }

        var id = new ResourceLocation(mode);
        return modesForGadget.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst()
                .orElse(modesForGadget.first());
    }

    public static void setMode(ItemStack gadget, BaseMode mode) {
        gadget.set(BG2DataComponents.GADGET_MODE, mode.getId().toString());
    }
}

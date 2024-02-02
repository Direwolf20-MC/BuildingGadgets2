package com.direwolf20.buildinggadgets2.client.screen.widgets;


import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.direwolf20.buildinggadgets2.client.screen.MaterialListGUI.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

// Todo change to AbstractList as it's an easy fix compared to duping the class
public class ScrollingMaterialList extends EntryList<ScrollingMaterialList.Entry> {
    private static final int UPDATE_MILLIS = 1000;
    public static final int TOP = 16;
    public static final int BOTTOM = 32;

    private static final int SLOT_SIZE = 18;
    private static final int MARGIN = 2;
    private static final int ENTRY_HEIGHT = Math.max(SLOT_SIZE + MARGIN * 2, Minecraft.getInstance().font.lineHeight * 2 + MARGIN * 3);
    private static final int LINE_SIDE_MARGIN = 8;

    private Screen gui;

    private SortingModes sortingMode;
    private long lastUpdate;
    private ArrayList<StatePos> statePosArrayList;
    private Map<ItemStackKey, Integer> itemCountsMap;
    private ItemStack templateItem;
    public List<Component> hoveringText;

    /**
     * This class draws a list of entries, which is an object overriden and defined below. Each entry draws the icon and text, etc
     * Extended from the MC Base class that handles things like the server select screen
     */
    public ScrollingMaterialList(Screen gui, int windowLeftX, int windowTopY, int windowWidth, int windowHeight, ItemStack templateItem) {
        super(windowLeftX, windowTopY, windowWidth, windowHeight, ENTRY_HEIGHT);

        this.gui = gui;
        this.setSortingMode(SortingModes.NAME);
        setTemplateItem(templateItem);
        updateEntries();
    }

    public void setTemplateItem(ItemStack templateItem) {
        this.templateItem = templateItem;
        statePosArrayList = new ArrayList<>();
        updateEntries();
    }

    private void updateEntries() {
        this.lastUpdate = System.currentTimeMillis();
        this.clearEntries();
        this.setScrollAmount(0);

        //Get the statePos list - since this screen can only be called from 'paste' mode, the client side should always be up to date in theory?
        if (statePosArrayList == null || statePosArrayList.isEmpty()) {
            statePosArrayList = BG2DataClient.getLookupFromUUID(GadgetNBT.getUUID(templateItem));
        }

        Player player = Minecraft.getInstance().player;

        // Could likely just assert
        if (player == null)
            return;

        //Get a list of ItemStackkey -> Amount required (Integer)
        itemCountsMap = StatePos.getItemList(statePosArrayList);

        for (Map.Entry<ItemStackKey, Integer> entry : itemCountsMap.entrySet()) {
            if (entry.getKey().getStack().isEmpty()) continue;
            int itemCount = BuildingUtils.countItemStacks(player, entry.getKey().getStack());
            //Add entries to the list
            addEntry(new com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry(this, entry.getKey().getStack(), entry.getValue(), itemCount));
        }

        sort();
    }

    @Override
    protected int getScrollbarPosition() {
        return getRight() - MARGIN - SCROLL_BAR_WIDTH;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_E) {
            assert Minecraft.getInstance().player != null;

            Minecraft.getInstance().player.closeContainer();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //if (lastUpdate + UPDATE_MILLIS < System.currentTimeMillis())
        //    updateEntries();  //Why?

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    public void reset() {
        itemCountsMap = null;
    }

    /**
     * This class defines what each entry in the list looks like and how it behaves
     */
    public static class Entry extends ObjectSelectionList.Entry<com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry> {

        private ScrollingMaterialList parent;
        private int required;
        private int available;

        private ItemStack stack;

        private String itemName;
        private String amount;

        private int widthItemName;
        private int widthAmount;

        private int hoveringTextX;
        private int hoveringTextY;

        public Entry(ScrollingMaterialList parent, ItemStack item, int required, int available) {
            this.parent = parent;
            this.required = required;
            this.available = Mth.clamp(available, 0, required);

            this.stack = item;
            this.itemName = stack.getHoverName().getString();

            // Use this.available since the parameter is not clamped
            this.amount = this.available + "/" + required;
            this.widthItemName = Minecraft.getInstance().font.width(itemName);
            this.widthAmount = Minecraft.getInstance().font.width(amount);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int topY, int leftX, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float particleTicks) {
            // Weird render issue with GuiSlot where the right border is slightly offset
            // MARGIN * 2 is just a magic number that made it look nice
            int right = leftX + entryWidth - MARGIN * 2;
            // Centralize entry vertically, for some reason this.getY() is not inclusive on the bottom
            int bottom = topY + entryHeight;

            int slotX = leftX + MARGIN - 15;
            int slotY = topY + MARGIN;

            drawIcon(guiGraphics, stack, slotX, slotY);
            drawTextOverlay(guiGraphics, right, topY, bottom, slotX);
            drawHoveringText(stack, slotX, slotY, mouseX, mouseY);
        }

        private void drawTextOverlay(GuiGraphics guiGraphics, int right, int top, int bottom, int slotX) {
            int itemNameX = slotX + SLOT_SIZE + MARGIN;
            // -1 because the bottom x coordinate is exclusive
            Font fontRenderer = Minecraft.getInstance().font;
            int rightEdge = getXForAlignedRight(right, fontRenderer.width(amount)) - 5;
            renderTextVerticalCenter(guiGraphics, itemName, itemNameX, rightEdge, top, bottom, Color.WHITE.getRGB());
            renderTextHorizontalRight(guiGraphics, amount, right, getYForAlignedCenter(top, bottom, Minecraft.getInstance().font.lineHeight), getTextColor());

            //drawGuidingLine(right, top, bottom, itemNameX, widthItemName, widthAmount); //Todo what even was this?
        }

        private void drawGuidingLine(int right, int top, int bottom, int itemNameX, int widthItemName, int widthAmount) {
            if (!isSelected()) {
                int lineXStart = itemNameX + widthItemName + LINE_SIDE_MARGIN;
                int lineXEnd = right - widthAmount - LINE_SIDE_MARGIN;
                int lineY = getYForAlignedCenter(top, bottom - 1, 1);
                RenderSystem.enableBlend();
//                RenderSystem.disableTexture();
                RenderSystem.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor(255, 255, 255, 34);

//                glLineWidth(1);
//                glBegin(GL_LINES);
//                glVertex3f(lineXStart, lineY, 0);
//                glVertex3f(lineXEnd, lineY, 0);
//                glEnd();

//                RenderSystem.enableTexture();
                RenderSystem.setShaderColor(1, 1, 1, 1);
                RenderSystem.disableBlend();
            }
        }

        private void drawHoveringText(ItemStack item, int slotX, int slotY, int mouseX, int mouseY) {
            if (isPointInBox(mouseX, mouseY, slotX, slotY, 18, 18))
                setTaskHoveringText(mouseX, mouseY, getTooltipFromItem(Minecraft.getInstance(), item));
        }

        private void drawIcon(GuiGraphics guiGraphics, ItemStack item, int slotX, int slotY) {
            Lighting.setupForFlatItems();
            guiGraphics.renderItem(item, slotX, slotY);
            Lighting.setupFor3DItems();
        }

        private boolean hasEnoughItems() {
            return required == available;
        }

        private int getTextColor() {
            return hasEnoughItems() ? Color.GREEN.getRGB() : available == 0 ? Color.RED.getRGB() : Color.YELLOW.getRGB();
        }

        public int getRequired() {
            return required;
        }

        public int getAvailable() {
            return available;
        }

        public int getMissing() {
            return required - available;
        }

        public ItemStack getStack() {
            return stack;
        }

        public String getItemName() {
            return itemName;
        }

        public String getFormattedRequired() {
            int maxSize = stack.getMaxStackSize();
            int stacks = required / maxSize; // Integer division automatically floors
            int leftover = required % maxSize;
            if (stacks == 0)
                return String.valueOf(leftover);
            return stacks + "×" + maxSize + "+" + leftover;
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            // TODO add replacement function and make entries selectable
            if (isMouseOver(x, y)) {
                parent.setSelected(this);
                return true;
            }
            return false;
        }

        public boolean isSelected() {
            return parent.getSelected() == this;
        }

        @Override
        public Component getNarration() {
            return Component.empty();
        }

        public void setTaskHoveringText(int x, int y, List<Component> text) {
            hoveringTextX = x;
            hoveringTextY = y;
            parent.hoveringText = text;
        }
    }


    public SortingModes getSortingMode() {
        return sortingMode;
    }

    public void setSortingMode(SortingModes sortingMode) {
        this.sortingMode = sortingMode;
        sort();
    }

    private void sort() {
        children().sort(sortingMode.getComparator());
    }

    public enum SortingModes {

        NAME(Comparator.comparing(com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry::getItemName), Component.translatable("buildinggadgets2.screen.sortaz")),
        NAME_REVERSED(NAME.getComparator().reversed(), Component.translatable("buildinggadgets2.screen.sortza")),
        REQUIRED(Comparator.comparingInt(com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry::getRequired), Component.translatable("buildinggadgets2.screen.requiredasc")),
        REQUIRED_REVERSED(REQUIRED.getComparator().reversed(), Component.translatable("buildinggadgets2.screen.requireddesc")),
        MISSING(Comparator.comparingInt(com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry::getMissing), Component.translatable("buildinggadgets2.screen.missingasc")),
        MISSING_REVERSED(MISSING.getComparator().reversed(), Component.translatable("buildinggadgets2.screen.missingdesc"));

        private final Comparator<com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry> comparator;
        private final Component translatable;

        SortingModes(Comparator<com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry> comparator, Component translatable) {
            this.comparator = comparator;
            this.translatable = translatable;
        }

        public Comparator<com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList.Entry> getComparator() {
            return comparator;
        }

        public Component getTranslatable() {
            return translatable;
        }

        public SortingModes next() {
            int nextIndex = ordinal() + 1;
            return VALUES[nextIndex >= VALUES.length ? 0 : nextIndex];
        }

        public static final SortingModes[] VALUES = SortingModes.values();
    }
}

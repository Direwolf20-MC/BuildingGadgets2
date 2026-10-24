package com.direwolf20.buildinggadgets2.client.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

public class EntryList<E extends Entry<E>> extends ObjectSelectionList<E> {

    public static final int SCROLL_BAR_WIDTH = 6;

    public EntryList(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, slotHeight);
        this.setX(left);
    }

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!scrollable()) return;

        int left = this.scrollBarX();
        int right = left + SCROLL_BAR_WIDTH;
        int top = this.getY();
        int bottom = this.getBottom();

        int scrollerHeight = this.scrollerHeight();
        scrollerHeight = Mth.clamp(scrollerHeight, 32, bottom - top - 8);
        int scrollerTop = this.scrollBarY();

        graphics.fill(left, top, right, bottom, 0xFF000000);
        graphics.fill(left, scrollerTop, right, scrollerTop + scrollerHeight, 0xFF808080);
        graphics.fill(left, scrollerTop, right - 1, scrollerTop + scrollerHeight - 1, 0xFFC0C0C0);
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {
        graphics.fillGradient(getX(), getY(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
    }

    @Override
    protected void extractSelection(GuiGraphicsExtractor graphics, E entry, int outlineColor) {
        int top = entry.getY();
        int bottom = top + entry.getHeight();
        int left = this.getX() + 3;
        int right = this.getX() + (this.width + this.getRowWidth()) / 2;
        graphics.fill(left, top - 2, right, bottom + 2, outlineColor);
        graphics.fill(left + 1, top - 1, right - 1, bottom + 1, 0xFF000000);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        setDragging(true);
        super.mouseClicked(event, doubleClick);
        return isMouseOver(event.x(), event.y());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        setDragging(false);
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (super.mouseDragged(event, dx, dy))
            return true;

        if (isMouseOver(event.x(), event.y())) {
            setScrollAmount(scrollAmount() - dy);
        }
        return true;
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 30;
    }
}

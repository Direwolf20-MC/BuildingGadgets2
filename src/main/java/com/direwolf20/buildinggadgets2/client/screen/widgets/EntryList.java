package com.direwolf20.buildinggadgets2.client.screen.widgets;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.util.Mth;

public class EntryList<E extends Entry<E>> extends ObjectSelectionList<E> {

    public static final int SCROLL_BAR_WIDTH = 6;

    public EntryList(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, slotHeight);
        // Set left x and right x, somehow MCP gave it a weird name
        this.setX(left);
        double guiScaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (children().isEmpty()) return;
        //This Scissor stuff is what keeps the item list in the dark area, without it it bleeds into the white area while scrolling (try it)
        guiGraphics.enableScissor(getX(), getY(), getX() + width, getY() + height);
        renderParts(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
    }

    // Copied and modified from AbstractLists#render(int, int, float)
    private void renderParts(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        renderContentBackground(guiGraphics);

        int k = getRowLeft();
        int l = getY() + 4 - (int) getScrollAmount();

        //Don't ask my why this has to go first, but it does!
        int j1 = getMaxScroll();
        //This section renders the scroll bar. If we renderItems() first the scrollbar is always black - no idea why
        if (j1 > 0) {
            int k1 = (int) ((float) ((getBottom() - getY()) * (getBottom() - getY())) / (float) getMaxPosition());
            k1 = Mth.clamp(k1, 32, getBottom() - getY() - 8);
            int l1 = (int) getScrollAmount() * (getBottom() - getY() - k1) / j1 + getY();
            if (l1 < getY()) {
                l1 = getY();
            }
            int x1 = getScrollbarPosition();
            int x2 = x1 + 6;

            bufferbuilder.addVertex(x1, getBottom(), 0.0F).setColor(0, 0, 0, 255);
            bufferbuilder.addVertex(x2, getBottom(), 0.0F).setColor(0, 0, 0, 255);
            bufferbuilder.addVertex(x2, getY(), 0.0F).setColor(0, 0, 0, 255);
            bufferbuilder.addVertex(x1, getY(), 0.0F).setColor(0, 0, 0, 255);

            bufferbuilder.addVertex(x1, (l1 + k1), 0.0F).setColor(128, 128, 128, 255);
            bufferbuilder.addVertex(x2, (l1 + k1), 0.0F).setColor(128, 128, 128, 255);
            bufferbuilder.addVertex(x2, l1, 0.0F).setColor(128, 128, 128, 255);
            bufferbuilder.addVertex(x1, l1, 0.0F).setColor(128, 128, 128, 255);

            bufferbuilder.addVertex(x1, (l1 + k1 - 1), 0.0F).setColor(192, 192, 192, 255);
            bufferbuilder.addVertex((x2 - 1), (l1 + k1 - 1), 0.0F).setColor(192, 192, 192, 255);
            bufferbuilder.addVertex((x2 - 1), l1, 0.0F).setColor(192, 192, 192, 255);
            bufferbuilder.addVertex(x1, l1, 0.0F).setColor(192, 192, 192, 255);
        }
        renderListItems(guiGraphics, k, l, partialTicks);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    protected void renderContentBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(getX(), getY(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
    }

    @Override
    protected void renderSelection(GuiGraphics p_283589_, int p_240142_, int p_240143_, int p_240144_, int p_240145_, int p_240146_) {
        int i = this.getX() + 3;
        int j = this.getX() + (this.width + p_240143_) / 2;
        p_283589_.fill(i, p_240142_ - 2, j, p_240142_ + p_240144_ + 2, p_240145_);
        p_283589_.fill(i + 1, p_240142_ - 1, j - 1, p_240142_ + p_240144_ + 1, p_240146_);
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        setDragging(true);
        super.mouseClicked(x, y, button);
        return isMouseOver(x, y);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        setDragging(false);
        return super.mouseReleased(x, y, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (super.mouseDragged(x, y, button, dx, dy))
            return true;

        // Dragging elements in panel
        if (isMouseOver(x, y)) {
            setScrollAmount(getScrollAmount() - dy);
        }
        return true;
    }

    // Copied from AbstractList#getMaxScroll because it is private
    public final int getMaxScroll() {
        return Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getY() - 4));
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 30;
    }
}

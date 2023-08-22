package com.direwolf20.buildinggadgets2.client.screen.widgets;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.util.Mth;

public class EntryList<E extends Entry<E>> extends ObjectSelectionList<E> {

    public static final int SCROLL_BAR_WIDTH = 6;

    public EntryList(int left, int top, int width, int height, int slotHeight) {
        super(Minecraft.getInstance(), width, height, top, top + height, slotHeight);
        // Set left x and right x, somehow MCP gave it a weird name
        this.setLeftPos(left);
        double guiScaleFactor = Minecraft.getInstance().getWindow().getGuiScale();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //This Scissor stuff is what keeps the item list in the dark area, without it it bleeds into the white area while scrolling (try it)
        guiGraphics.enableScissor(getLeft(), getTop(), getLeft() + width, getTop() + height);
        renderParts(guiGraphics, mouseX, mouseY, partialTicks);
        guiGraphics.disableScissor();
    }

    // Copied and modified from AbstractLists#render(int, int, float)
    private void renderParts(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        renderBackground(guiGraphics);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        renderContentBackground(guiGraphics, tessellator, bufferbuilder);

        int k = getRowLeft();
        int l = getTop() + 4 - (int) getScrollAmount();

        //Don't ask my why this has to go first, but it does!
        int j1 = getMaxScroll();
        //This section renders the scroll bar. If we renderItems() first the scrollbar is always black - no idea why
        if (j1 > 0) {
            int k1 = (int) ((float) ((getBottom() - getTop()) * (getBottom() - getTop())) / (float) getMaxPosition());
            k1 = Mth.clamp(k1, 32, getBottom() - getTop() - 8);
            int l1 = (int) getScrollAmount() * (getBottom() - getTop() - k1) / j1 + getTop();
            if (l1 < getTop()) {
                l1 = getTop();
            }
            int x1 = getScrollbarPosition();
            int x2 = x1 + 6;

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex(x1, getBottom(), 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(x2, getBottom(), 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(x2, getTop(), 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex(x1, getTop(), 0.0D).color(0, 0, 0, 255).endVertex();

            bufferbuilder.vertex(x1, (l1 + k1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(x2, (l1 + k1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(x2, l1, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex(x1, l1, 0.0D).color(128, 128, 128, 255).endVertex();

            bufferbuilder.vertex(x1, (l1 + k1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((x2 - 1), (l1 + k1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((x2 - 1), l1, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex(x1, l1, 0.0D).color(192, 192, 192, 255).endVertex();
            tessellator.end();
        }
        renderList(guiGraphics, k, l, partialTicks);
        //renderDecorations(guiGraphics, mouseX, mouseX);
        //RenderSystem.disableBlend();
    }

    protected void renderContentBackground(GuiGraphics guiGraphics, Tesselator tessellator, BufferBuilder bufferbuilder) {
        guiGraphics.fillGradient(getLeft(), getTop(), getRight(), getBottom(), 0xC0101010, 0xD0101010);
    }

    @Override
    protected void renderBackground(GuiGraphics guiGraphics) {
        super.renderBackground(guiGraphics);
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
        return Math.max(0, this.getMaxPosition() - (this.getBottom() - this.getTop() - 4));
    }

    @Override
    public int getRowWidth() {
        return getWidth() - 30;
    }
}

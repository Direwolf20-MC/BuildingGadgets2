package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class MaterialListGUI extends Screen {

    public static final int BUTTON_HEIGHT = 20;
    public static final int BUTTONS_PADDING = 4;

    public static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "textures/gui/material_list.png");
    public static final int BACKGROUND_WIDTH = 256;
    public static final int BACKGROUND_HEIGHT = 200;
    public static final int BORDER_SIZE = 4;

    public static final int WINDOW_WIDTH = BACKGROUND_WIDTH - BORDER_SIZE * 2;
    public static final int WINDOW_HEIGHT = BACKGROUND_HEIGHT - BORDER_SIZE * 2;

    private int backgroundX;
    private int backgroundY;
    private ItemStack gadget;

    private String title;
    private int titleLeft;
    private int titleTop;

    private ScrollingMaterialList scrollingList;

    private Button buttonClose;
    private Button buttonSortingModes;
    private Button buttonCopyList;


    public MaterialListGUI(ItemStack itemStack) {
        super(Component.translatable("buildinggadgets2.screen.componentslist"));
        this.gadget = itemStack;
    }

    /**
     * The real stuff happens in ScrollingMaterialList
     * This screen really just draws a background and the buttons, thats about it
     */
    @Override
    public void init() {
        this.backgroundX = getXForAlignedCenter(0, width, BACKGROUND_WIDTH);
        this.backgroundY = getYForAlignedCenter(0, height, BACKGROUND_HEIGHT);

        // Make it receive mouse scroll events, so that the player can use his mouse wheel at the start

        this.scrollingList = new ScrollingMaterialList(this, getWindowLeftX(), getWindowTopY() + 16, getWindowWidth(), getWindowHeight() - 16 - 32, gadget);
        this.setFocused(scrollingList);
        this.addRenderableWidget(scrollingList);

        int buttonY = getWindowBottomY() - (ScrollingMaterialList.BOTTOM / 2 + BUTTON_HEIGHT / 2);
        this.buttonClose = Button.builder(Component.translatable("buildinggadgets2.screen.close"), b -> getMinecraft().player.closeContainer())
                .pos(0, buttonY)
                .size(0, BUTTON_HEIGHT)
                .build();

        this.buttonSortingModes = Button.builder(scrollingList.getSortingMode().getTranslatable(), (button) -> {
                    scrollingList.setSortingMode(scrollingList.getSortingMode().next());
                    buttonSortingModes.setMessage(scrollingList.getSortingMode().getTranslatable());
                })
                .pos(0, buttonY)
                .size(0, BUTTON_HEIGHT)
                .build();

        //Todo Maybe bring this back someday :) when I feel like writing json
        /*this.buttonCopyList = Button.builder(Component.translatable("buildinggadgets2.screen.copy"), (button) -> {
            getMinecraft().keyboardHandler.setClipboard(evaluateTemplateHeader().toJson(false, hasControlDown()));

            if (getMinecraft().player != null)
                getMinecraft().player.displayClientMessage(Component.translatable("buildinggadgets2.messages.copysuccess"), true);
        })
                .pos(0, buttonY)
                .size(0, BUTTON_HEIGHT)
                .build();
*/

        // Buttons will be placed left to right in this order
        this.addRenderableWidget(buttonSortingModes);
        //this.addRenderableWidget(buttonCopyList);
        this.addRenderableWidget(buttonClose);

        this.calculateButtonsWidthAndX();
    }

    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float particleTicks) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, backgroundX, backgroundY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        super.extractRenderState(guiGraphics, mouseX, mouseY, particleTicks);

        if (scrollingList.hoveringText != null) {
            guiGraphics.setTooltipForNextFrame(font, scrollingList.hoveringText, Optional.empty(), mouseX, mouseY);
            scrollingList.hoveringText = null;
        }

    }

    private void calculateButtonsWidthAndX() {
        // This part would can create narrower buttons when there are too few of them, due to the vanilla button texture is 200 pixels wide
        int amountButtons = (int) children().stream().filter(e -> e instanceof Button).count();
        int amountMargins = amountButtons - 1;
        int totalMarginWidth = amountMargins * BUTTONS_PADDING;
        int usableWidth = getWindowWidth();
        int buttonWidth = (usableWidth - totalMarginWidth) / amountButtons;

        // Align the box of buttons in the center, and start from the left
        int nextX = getWindowLeftX();

        for (GuiEventListener widget : children()) {
            if (widget instanceof Button btn) {
                btn.setWidth(buttonWidth);
                btn.setX(nextX);
                nextX += buttonWidth + BUTTONS_PADDING;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public int getWindowLeftX() {
        return backgroundX + BORDER_SIZE;
    }

    public int getWindowRightX() {
        return backgroundX + BACKGROUND_WIDTH - BORDER_SIZE;
    }

    public int getWindowTopY() {
        return backgroundY + BORDER_SIZE;
    }

    public int getWindowBottomY() {
        return backgroundY + BACKGROUND_HEIGHT - BORDER_SIZE;
    }

    public int getWindowWidth() {
        return WINDOW_WIDTH;
    }

    public int getWindowHeight() {
        return WINDOW_HEIGHT;
    }

    public ItemStack getTemplateItem() {
        return gadget;
    }

    public static int getXForAlignedRight(int right, int width) {
        return right - width;
    }

    public static int getXForAlignedCenter(int left, int right, int width) {
        return left + (right - left) / 2 - width / 2;
    }

    public static int getYForAlignedCenter(int top, int bottom, int height) {
        return top + (bottom - top) / 2 - height / 2;
    }

    public static void renderTextVerticalCenter(GuiGraphicsExtractor guiGraphics, String text, int leftX, int rightX, int top, int bottom, int color) {
        Font fontRenderer = Minecraft.getInstance().font;
        int y = getYForAlignedCenter(top, bottom, fontRenderer.lineHeight);
        renderScrollingString(guiGraphics, fontRenderer, Component.literal(text), leftX, y, rightX, color);
    }

    public static void renderTextHorizontalRight(GuiGraphicsExtractor guiGraphics, String text, int right, int y, int color) {
        Font fontRenderer = Minecraft.getInstance().font;
        int x = getXForAlignedRight(right, fontRenderer.width(text));
        guiGraphics.text(fontRenderer, text, x, y, color, false);
    }

    protected static void renderScrollingString(GuiGraphicsExtractor graphics, Font fontRenderer, Component text, int xStart, int yStart, int xEnd, int textColor) {
        int textWidth = fontRenderer.width(text);
        int yEnd = yStart + fontRenderer.lineHeight;
        int maxRenderWidth = xEnd - xStart;

        if (textWidth > maxRenderWidth) {
            int textOverflow = textWidth - maxRenderWidth;
            double currentTime = (double) Util.getMillis() / 1000.0D;
            double scrollDuration = Math.max((double) textOverflow * 0.5D, 3.0D);
            double oscillation = Math.sin((Math.PI / 2D) * Math.cos((Math.PI * 2D) * currentTime / scrollDuration)) / 2.0D + 0.5D;
            double scrollOffset = Mth.lerp(oscillation, 0.0D, (double) textOverflow);

            graphics.enableScissor(xStart, yStart, xEnd, yEnd);
            graphics.text(fontRenderer, text, xStart - (int) scrollOffset, yStart, textColor);
            graphics.disableScissor();
        } else {
            graphics.text(fontRenderer, text, xStart, yStart, textColor, false);
        }
    }

    public static boolean isPointInBox(double x, double y, int bx, int by, int width, int height) {
        return x >= bx &&
                y >= by &&
                x < bx + width &&
                y < by + height;
    }
}

package com.direwolf20.buildinggadgets2.client.screen.widgets;

import com.direwolf20.buildinggadgets2.client.OurSounds;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import java.awt.*;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * A flat colored, incremental (+ and - buttons) slider widget
 */
public class IncrementalSliderWidget extends ExtendedSlider {
    private static final int BACKGROUND = createAlphaColor(Color.DARK_GRAY, 200).getRGB();
    private static final int SLIDER_BACKGROUND = createAlphaColor(Color.DARK_GRAY.darker(), 200).getRGB();
    private static final int SLIDER_COLOR = createAlphaColor(Color.DARK_GRAY.brighter().brighter(), 200).getRGB();

    public final Consumer<IncrementalSliderWidget> onUpdate;

    public IncrementalSliderWidget(int x, int y, int width, int height, double min, double max, Component prefix, double current, Consumer<IncrementalSliderWidget> onUpdate) {
        super(x, y, width, height, prefix, Component.empty(), min, max, current, 1D, 1, true);
        this.onUpdate = onUpdate;
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, BACKGROUND);
        this.drawBorderedRect(guiGraphics, (this.getX() + (int) (this.value * (double) (this.width - 8)) + 4) - 4, this.getY(), 8, this.height);
        this.renderText(guiGraphics);
    }

    private void renderText(GuiGraphicsExtractor guiGraphics) {
        int color = !active ? 0xFFA0A0A0 : (isHovered ? 0xFFFFFF60 : -1);

        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.centeredText(minecraft.font, this.prefix.copy().append(this.getValueString()), getX() + getWidth() / 2, getY() + (getHeight() - 8) / 2, color);
    }

    private void drawBorderedRect(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, SLIDER_BACKGROUND);
        guiGraphics.fill(++x, ++y, x + width - 2, y + height - 2, SLIDER_COLOR);
    }

    @Override
    protected void applyValue() {
        this.onUpdate.accept(this);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onRelease(MouseButtonEvent event) {
        this.dragging = false;
        OurSounds.playSound(Registration.BEEP.get());
    }

    private static Color createAlphaColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    // This is lazy, I should really just build it into a single widget render
    public Collection<AbstractWidget> getComponents() {
        return ImmutableSet.of(
                this,
                new GuiButtonIncrement(getX() - height - 5, getY(), height, height, Component.literal("-"), b -> {
                    this.setValue(this.getValueInt() - 1);
                    IncrementalSliderWidget.this.applyValue();
                }),
                new GuiButtonIncrement(getX() + width + 5, getY(), height, height, Component.literal("+"), b -> {
                    this.setValue(this.getValueInt() + 1);
                    IncrementalSliderWidget.this.applyValue();
                })
        );
    }

    private class GuiButtonIncrement extends Button {
        public GuiButtonIncrement(int x, int y, int width, int height, Component message, OnPress action) {
            super(x, y, width, height, message, action, Button.DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partial) {
            Minecraft minecraft = Minecraft.getInstance();
            guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, IncrementalSliderWidget.BACKGROUND);
            IncrementalSliderWidget.this.drawBorderedRect(guiGraphics, this.getX(), this.getY(), this.width, this.height);
            guiGraphics.centeredText(minecraft.font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor() | 0xFF000000);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            OurSounds.playSound(Registration.BEEP.get());
        }
    }
}

package com.direwolf20.buildinggadgets2.client.screen.widgets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;

public class GuiIncrementer extends AbstractWidget {
    // this is the width of all components in a line
    public static final int WIDTH = 64;

    private int x;
    private int y;
    private int min;
    private int max;

    private int value;
    private IIncrementerChanged onChange;

    private DireButton minusButton;
    private GuiTextFieldBase field;
    private DireButton plusButton;

    public GuiIncrementer(int x, int y, int min, int max, @Nullable IIncrementerChanged onChange) {
        super(x, y, WIDTH, 20, Component.empty());

        this.x = x;
        this.y = y;
        this.min = min;
        this.max = max;
        this.value = 0;
        this.onChange = onChange;

        this.minusButton = new DireButton(this.x, this.y - 1, 12, 17, Component.literal("-"), (button) -> this.updateValue(true));
        this.field = new GuiTextFieldBase(Minecraft.getInstance().font, x + 13, y, 40).setDefaultInt(this.value).restrictToNumeric();
        this.plusButton = new DireButton(this.x + 40 + 14, this.y - 1, 12, 17, Component.literal("+"), (button) -> this.updateValue(false));

        this.field.setValue(String.valueOf(this.value));
    }

    public GuiIncrementer(int x, int y) {
        this(x, y, Integer.MIN_VALUE, Integer.MAX_VALUE, null);
    }

    public GuiIncrementer(int x, int y, @Nullable IIncrementerChanged onChange) {
        this(x, y, Integer.MIN_VALUE, Integer.MAX_VALUE, onChange);
    }

    public int getValue() {
        return this.value;
    }

    private void updateValue(boolean isMinus) {
        int modifier = 1;
        if (Minecraft.getInstance().hasShiftDown())
            modifier *= 10;

        int value = isMinus ? this.value - modifier : this.value + modifier;
        this.setValue(value);
    }

    public void setValue(int value) {
        // We don't want to fire events for no reason
        if (value == this.value)
            return;

        this.value = Mth.clamp(value, this.min, this.max);
        this.field.setValue(String.valueOf(this.value));

        if (this.onChange != null)
            this.onChange.onChange(value);
    }

    public void setValue(int value, boolean onChange) {
        // We don't want to fire events for no reason
        this.value = Mth.clamp(value, this.min, this.max);
        this.field.setValue(String.valueOf(this.value));

        if (onChange)
            this.onChange.onChange(value);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.plusButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.minusButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.field.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.field.mouseClicked(event, doubleClick);
        this.plusButton.mouseClicked(event, doubleClick);
        this.minusButton.mouseClicked(event, doubleClick);

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!this.field.isFocused())
            return false;

        this.field.keyPressed(event);
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!this.field.isFocused())
            return false;

        this.field.charTyped(event);
        if (this.field.getValue().length() > 1 && this.field.getValue().charAt(0) == '0')
            this.field.setValue(String.valueOf(this.field.getInt()));

        if (this.field.getInt() > this.max)
            this.field.setValue(String.valueOf(this.max));

        return true;
    }

    @Override
    public void setFocused(boolean isFocused) {
        this.field.setFocused(isFocused);
        super.setFocused(isFocused);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_259858_) {

    }

    public interface IIncrementerChanged {
        void onChange(int value);
    }
}

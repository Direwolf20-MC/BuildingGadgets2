package com.direwolf20.buildinggadgets2.client.screen.widgets;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.awt.*;
import java.util.function.Predicate;

/**
 * A one stop shop for all your icon gui related needs. We support colors,
 * icons, selected and deselected states, sound and loads more. Come on
 * down!
 */
public class GuiIconActionable extends Button {
    private Predicate<Boolean> action;
    private boolean selected;
    private boolean isSelectable;

    private final Color selectedColor = new Color(0, 255, 0, 50);
    private final Color deselectedColor = new Color(255, 255, 255, 50);
    private Color activeColor;

    private final Identifier selectedTexture;
    private final Identifier deselectedTexture;

    public GuiIconActionable(int x, int y, String texture, Component message, boolean isSelectable, Predicate<Boolean> action) {
        super(x, y, 25, 25, message, (button) -> {
        }, Button.DEFAULT_NARRATION);
        this.activeColor = deselectedColor;
        this.isSelectable = isSelectable;
        this.action = action;

        this.setSelected(action.test(false));

        // Set the selected and deselected textures.
        String assetLocation = "textures/gui/setting/%s.png";

        this.deselectedTexture = Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, String.format(assetLocation, texture));
        this.selectedTexture = !isSelectable ? this.deselectedTexture : Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, String.format(assetLocation, texture + "_selected"));
    }

    /**
     * If yo do not need to be able to select / toggle something then use this constructor as
     * you'll hit missing texture issues if you don't have an active (_selected) texture.
     */
    public GuiIconActionable(int x, int y, String texture, Component message, Predicate<Boolean> action) {
        this(x, y, texture, message, false, action);
    }

    public void setFaded(boolean faded) {
        alpha = faded ? .6f : 1f;
    }

    /**
     * This should be used when ever-changing select.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.activeColor = selected ? selectedColor : deselectedColor;
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        soundHandler.play(SimpleSoundInstance.forUI(Registration.BEEP.get(), selected ? .6F : 1F));
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        super.onClick(event, doubleClick);
        this.action.test(true);

        if (!this.isSelectable)
            return;

        this.setSelected(!this.selected);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, activeColor.getRGB());

        Identifier texture = selected ? selectedTexture : deselectedTexture;
        int tint = ARGB.multiplyAlpha(
                ARGB.color(activeColor.getRed(), activeColor.getGreen(), activeColor.getBlue()),
                this.alpha);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height, tint);

        if (mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height) {
            Minecraft minecraft = Minecraft.getInstance();
            int labelX = mouseX > (minecraft.getWindow().getGuiScaledWidth() / 2) ? mouseX + 2 : mouseX - minecraft.font.width(getMessage().getString());
            guiGraphics.text(minecraft.font, this.getMessage().getString(), labelX, mouseY - 10, activeColor.getRGB() | 0xFF000000);
        }
    }
}

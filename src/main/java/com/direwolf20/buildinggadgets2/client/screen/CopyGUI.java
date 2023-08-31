package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.client.screen.widgets.GuiIncrementer;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketCopyCoords;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CopyGUI extends Screen {
    private GuiIncrementer startX, startY, startZ, endX, endY, endZ;

    private boolean absoluteCoords = false;

    private int x;
    private int y;

    private ItemStack copyPasteTool;
    private BlockPos startPos;
    private BlockPos endPos;
    private Button absoluteButton;

    private List<GuiIncrementer> fields = new ArrayList<>();

    public CopyGUI(ItemStack tool) {
        super(Component.literal(""));
        this.copyPasteTool = tool;
    }

    @Override
    public void init() {
        super.init();

        this.fields.clear();

        this.x = width / 2;
        this.y = height / 2;

        startPos = GadgetNBT.getCopyStartPos(copyPasteTool);
        endPos = GadgetNBT.getCopyEndPos(copyPasteTool);

        int incrementerWidth = GuiIncrementer.WIDTH + (GuiIncrementer.WIDTH / 2);

        fields.add(startX = new GuiIncrementer(x - incrementerWidth - 35, y - 40, this::onChange));
        fields.add(startY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 40, this::onChange));
        fields.add(startZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 40, this::onChange));
        fields.add(endX = new GuiIncrementer(x - incrementerWidth - 35, y - 15, this::onChange));
        fields.add(endY = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 15, this::onChange));
        fields.add(endZ = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 35, y - 15, this::onChange));
        fields.forEach(this::addRenderableWidget);

        List<AbstractButton> buttons = new ArrayList<AbstractButton>() {{
            add(new CenteredButton(y + 20, 50, Component.translatable("buildinggadgets2.screen.confirm"), (button) -> {
                sendPacket();
                onClose();
            }));
            add(new CenteredButton(y + 20, 50, Component.translatable("buildinggadgets2.screen.close"), (button) -> onClose()));
            add(new CenteredButton(y + 20, 50, Component.translatable("buildinggadgets2.screen.clear"), (button) -> {
                PacketHandler.sendToServer(new PacketCopyCoords(GadgetNBT.nullPos, GadgetNBT.nullPos));
                onClose();
            }));

            absoluteButton = new CenteredButton(y + 20, 120, Component.translatable("buildinggadgets2.screen.absolutecoords"), (button) -> {
                coordsModeSwitch();
                updateTextFields();
            });
            add(absoluteButton);
        }};

        updateTextFields();
        CenteredButton.centerButtonList(buttons, x);
        buttons.forEach(this::addRenderableWidget);
    }

    private void drawFieldLabel(GuiGraphics guiGraphics, Component name, int x, int y) {
        guiGraphics.drawString(font, name, this.x + x, this.y + y, 0xFFFFFF);
    }

    private void onChange(int value) {
        sendPacket();
    }

    private void sendPacket() {
        if (absoluteCoords) {
            startPos = new BlockPos(startX.getValue(), startY.getValue(), startZ.getValue());
            endPos = new BlockPos(endX.getValue(), endY.getValue(), endZ.getValue());
        } else {
            startPos = new BlockPos(startPos.getX() + startX.getValue(), startPos.getY() + startY.getValue(), startPos.getZ() + startZ.getValue());
            endPos = new BlockPos(startPos.getX() + endX.getValue(), startPos.getY() + endY.getValue(), startPos.getZ() + endZ.getValue());
            startX.setValue(0, false);
            startY.setValue(0, false);
            startZ.setValue(0, false);
        }
        //System.out.println("Firing Packet!");
        PacketHandler.sendToServer(new PacketCopyCoords(startPos, endPos));
    }

    private void coordsModeSwitch() {
        absoluteCoords = !absoluteCoords;
    }

    private void updateTextFields() {
        if (absoluteCoords) {
            absoluteButton.setMessage(Component.translatable("buildinggadgets2.screen.relativecoords"));
            BlockPos start = startX.getValue() != 0 ? new BlockPos(startPos.getX() + startX.getValue(), startPos.getY() + startY.getValue(), startPos.getZ() + startZ.getValue()) : startPos;
            BlockPos end = endX.getValue() != 0 ? new BlockPos(startPos.getX() + endX.getValue(), startPos.getY() + endY.getValue(), startPos.getZ() + endZ.getValue()) : endPos;

            startX.setValue(start.getX(), false);
            startY.setValue(start.getY(), false);
            startZ.setValue(start.getZ(), false);
            endX.setValue(end.getX(), false);
            endY.setValue(end.getY(), false);
            endZ.setValue(end.getZ(), false);
        } else {
            absoluteButton.setMessage(Component.translatable("buildinggadgets2.screen.absolutecoords"));
            startX.setValue(startX.getValue() != 0 ? startX.getValue() - startPos.getX() : 0, false);
            startY.setValue(startY.getValue() != 0 ? startY.getValue() - startPos.getY() : 0, false);
            startZ.setValue(startZ.getValue() != 0 ? startZ.getValue() - startPos.getZ() : 0, false);
            endX.setValue(endX.getValue() != 0 ? endX.getValue() - startPos.getX() : endPos.getX() - startPos.getX(), false);
            endY.setValue(endY.getValue() != 0 ? endY.getValue() - startPos.getY() : endPos.getY() - startPos.getY(), false);
            endZ.setValue(endZ.getValue() != 0 ? endZ.getValue() - startPos.getZ() : endPos.getZ() - startPos.getZ(), false);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawFieldLabel(guiGraphics, Component.translatable("buildinggadgets2.screen.start").append(" X"), -175, -36);
        drawFieldLabel(guiGraphics, Component.literal("Y"), -45, -36);
        drawFieldLabel(guiGraphics, Component.literal("Z"), 55, -36);
        drawFieldLabel(guiGraphics, Component.translatable("buildinggadgets2.screen.end").append(" X"), 8 - 175, -11);
        drawFieldLabel(guiGraphics, Component.literal("Y"), -45, -11);
        drawFieldLabel(guiGraphics, Component.literal("Z"), 55, -11);

        guiGraphics.drawCenteredString(font, Component.translatable("buildinggadgets2.screen.copyheading"), this.x, this.y - 80, 0xFFFFFF);
        guiGraphics.drawCenteredString(font, Component.translatable("buildinggadgets2.screen.copysubheading"), this.x, this.y - 68, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(int mouseX, int mouseY, int __unused) {
        fields.forEach(button -> button.keyPressed(mouseX, mouseY, __unused));
        return super.keyPressed(mouseX, mouseY, __unused);
    }

    @Override
    public boolean charTyped(char charTyped, int __unused) {
        fields.forEach(button -> button.charTyped(charTyped, __unused));
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static class CenteredButton extends Button {
        CenteredButton(int y, int width, Component text, OnPress onPress) {
            super(builder(text, onPress)
                    .pos(0, y)
                    .size(width, 20)
            );
        }

        static void centerButtonList(List<AbstractButton> buttons, int startX) {
            int collectiveWidth = buttons.stream().mapToInt(AbstractButton::getWidth).sum() + (buttons.size() - 1) * 5;

            int nextX = startX - collectiveWidth / 2;
            for (AbstractButton button : buttons) {
                button.setX(nextX);
                nextX += button.getWidth() + 5;
            }
        }
    }
}

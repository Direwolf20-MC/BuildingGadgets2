/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.client.screen.widgets.GuiIncrementer;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.GadgetActionPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.gadgetaction.ActionGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class PasteGUI extends Screen {
    private GuiIncrementer X, Y, Z;
    private List<GuiIncrementer> fields = new ArrayList<>();
    private ItemStack copyPasteTool;

    PasteGUI(ItemStack tool) {
        super(Component.literal(""));
        this.copyPasteTool = tool;
    }

    @Override
    public void init() {
        super.init();

        int x = width / 2;
        int y = height / 2;

        fields.add(X = new GuiIncrementer(x - (GuiIncrementer.WIDTH + (GuiIncrementer.WIDTH / 2)) - 10, y - 10, -16, 16, this::onChange));
        fields.add(Y = new GuiIncrementer(x - GuiIncrementer.WIDTH / 2, y - 10, -16, 16, this::onChange));
        fields.add(Z = new GuiIncrementer(x + (GuiIncrementer.WIDTH / 2) + 10, y - 10, -16, 16, this::onChange));

        BlockPos currentOffset = GadgetNBT.getRelativePaste(this.copyPasteTool);
        X.setValue(currentOffset.getX());
        Y.setValue(currentOffset.getY());
        Z.setValue(currentOffset.getZ());

        List<AbstractButton> buttons = new ArrayList<AbstractButton>() {{
            add(new CopyGUI.CenteredButton(y + 20, 70, Component.translatable("buildinggadgets2.screen.confirm"), (button) -> {
                sendPacket();
                onClose();
            }));

            add(new CopyGUI.CenteredButton(y + 20, 40, Component.translatable("buildinggadgets2.screen.clear"), (button) -> {
                X.setValue(0);
                Y.setValue(0);
                Z.setValue(0);
                sendPacket();
            }));
        }};

        CopyGUI.CenteredButton.centerButtonList(buttons, x);

        buttons.forEach(this::addRenderableWidget);
        fields.forEach(this::addRenderableWidget);
    }

    private void sendPacket() {
        PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.RELATIVE_PASTE, NbtUtils.writeBlockPos(new BlockPos(X.getValue(), Y.getValue(), Z.getValue()))));
    }

    private void onChange(int value) {
        sendPacket();
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

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        drawLabel(guiGraphics, "X", -75);
        drawLabel(guiGraphics, "Y", 0);
        drawLabel(guiGraphics, "Z", 75);

        guiGraphics.drawCenteredString(font, Component.translatable("buildinggadgets2.screen.pasteheading"), (int) (width / 2f), (int) (height / 2f) - 60, 0xFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    private void drawLabel(GuiGraphics guiGraphics, String name, int x) {
        guiGraphics.drawString(font, name, (width / 2f) + x, (height / 2f) - 30, 0xFFFFFF, false);
    }
}

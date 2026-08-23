package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.screen.widgets.GuiIconActionable;
import com.direwolf20.buildinggadgets2.client.screen.widgets.IncrementalSliderWidget;
import com.direwolf20.buildinggadgets2.common.network.data.*;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class DestructionGUI extends Screen {
    private final Set<IncrementalSliderWidget> sliders = new HashSet<>();

    private IncrementalSliderWidget left;
    private IncrementalSliderWidget right;
    private IncrementalSliderWidget up;
    private IncrementalSliderWidget down;
    private IncrementalSliderWidget depth;

    private int originalLeft;
    private int originalRight;
    private int originalUp;
    private int originalDown;
    private int originalDepth;

    private Button confirm;

    private String sizeString = "";
    private boolean isValidSize = true;
    private boolean keyDown = false;

    private final ItemStack destructionGadget;

    private Button renderTypeButton;
    private GadgetNBT.RenderTypes renderType;

    public DestructionGUI(ItemStack tool, boolean keyDown) {
        super(Component.empty());
        this.destructionGadget = tool;
        this.keyDown = keyDown;
        renderType = GadgetNBT.getRenderType(tool);
    }

    @Override
    public void init() {
        super.init();

        int x = width / 2;
        int y = height / 2;

        /*if (!keyDown)
            this.addRenderableWidget(confirm = Button.builder(Component.translatable("buildinggadgets2.screen.close"), b -> {
                        this.onClose();
                    })
                    .pos((x - 30) - 32, y + 65)
                    .size(60, 20)
                    .build());*/

        this.addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.screen.revert"), b -> {
                    resetSliderMaxima();
                    depth.setValue(originalDepth);
                    right.setValue(originalRight);
                    left.setValue(originalLeft);
                    up.setValue(originalUp);
                    down.setValue(originalDown);
                    updateDynamicSliderLimits();
                    updateSizeString();
                    updateIsValid();
                    sendPacket();
                })
                .pos((x - 30) + 32, y + 65)
                .size(60, 20)
                .build());

        Button undo_button = new GuiIconActionable(x - 55, y - 75, "undo", Component.translatable("buildinggadgets2.radialmenu.undo"), false, send -> {
            if (send) {
                PacketDistributor.sendToServer(new UndoPayload());
            }

            return false;
        });
        this.addRenderableWidget(undo_button);

        Button anchorButton = new GuiIconActionable(x - 25, y - 75, "anchor", Component.translatable("buildinggadgets2.radialmenu.anchor"), true, send -> {
            if (send) {
                PacketDistributor.sendToServer(new AnchorPayload());
            }

            return !GadgetNBT.getAnchorPos(destructionGadget).equals(GadgetNBT.nullPos);
        });
        this.addRenderableWidget(anchorButton);

        Button affectTiles = new GuiIconActionable(x + 5, y - 75, "affecttiles", Component.translatable("buildinggadgets2.screen.affecttiles"), true, send -> {
            if (send) {
                PacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.AFFECT_TILES.getName()));
            }

            return GadgetNBT.getSetting(destructionGadget, GadgetNBT.ToggleableSettings.AFFECT_TILES.getName());
        });
        this.addRenderableWidget(affectTiles);

        Button rayTrace = new GuiIconActionable(x + 35, y - 75, "raytrace_fluid", Component.translatable("buildinggadgets2.radialmenu.raytracefluids"), true, send -> {
            if (send) {
                PacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.RAYTRACE_FLUID.getName()));
            }

            return GadgetNBT.getSetting(destructionGadget, GadgetNBT.ToggleableSettings.RAYTRACE_FLUID.getName());
        });
        this.addRenderableWidget(rayTrace);

        renderTypeButton = new GuiIconActionable(x + 65, y - 75, "raytrace_fluid", Component.translatable(renderType.getLang()), false, send -> {
            if (send) {
                renderType = renderType.next();
                renderTypeButton.setMessage(Component.translatable(renderType.getLang()));
                PacketDistributor.sendToServer(new RenderChangePayload(renderType.getPosition()));
            }

            return false;
        });
        this.addRenderableWidget(renderTypeButton);

        sliders.clear();
        int hardMax = getHardMax();
        sliders.add(depth = this.createSlider(x - (70 / 2), y - (14 / 2), Component.translatable("buildinggadgets2.screen.depth"),
                GadgetNBT.getToolValue(destructionGadget, GadgetNBT.IntSettings.DEPTH.getName()), 1D, hardMax));
        sliders.add(right = this.createSlider(x + (70 + 5), y - (14 / 2), Component.translatable("buildinggadgets2.screen.right"),
                GadgetNBT.getToolValue(destructionGadget, GadgetNBT.IntSettings.RIGHT.getName()), 0D, hardMax));
        sliders.add(left = this.createSlider(x - (70 * 2) - 5, y - (14 / 2), Component.translatable("buildinggadgets2.screen.left"),
                GadgetNBT.getToolValue(destructionGadget, GadgetNBT.IntSettings.LEFT.getName()), 0D, hardMax));
        sliders.add(up = this.createSlider(x - (70 / 2), y - 35, Component.translatable("buildinggadgets2.screen.up"),
                GadgetNBT.getToolValue(destructionGadget, GadgetNBT.IntSettings.UP.getName()), 0D, hardMax));
        sliders.add(down = this.createSlider(x - (70 / 2), y + 20, Component.translatable("buildinggadgets2.screen.down"),
                GadgetNBT.getToolValue(destructionGadget, GadgetNBT.IntSettings.DOWN.getName()), 0D, hardMax));

        originalDepth = depth.getValueInt();
        originalLeft = left.getValueInt();
        originalRight = right.getValueInt();
        originalUp = up.getValueInt();
        originalDown = down.getValueInt();

        updateDynamicSliderLimits();
        updateSizeString();
        updateIsValid();

        // Adds their buttons to the gui
        sliders.forEach(gui -> gui.getComponents().forEach(this::addRenderableWidget));
    }

    public IncrementalSliderWidget createSlider(int x, int y, MutableComponent prefix, int value, double min, double max) {
        return new IncrementalSliderWidget(x, y, 70, 14, min, max, prefix.append(": "), value, this::onSliderUpdate);
    }

    public void onSliderUpdate(IncrementalSliderWidget widget) {
        updateDynamicSliderLimits();
        this.updateSizeString();
        this.updateIsValid();
        sendPacket();
    }

    private int getHardMax() {
        return Math.max(1, Config.DESTRUCTIONGADGET_HARDMAX.get());
    }

    private long getMaxBlocks() {
        return Math.max(1L, Config.DESTRUCTIONGADGET_MAXBLOCKS.get());
    }

    private int clampDynamicMax(long calculatedMax, int minimum) {
        long clamped = Math.max(minimum, calculatedMax);
        clamped = Math.min(clamped, getHardMax());
        return (int) clamped;
    }

    private int calculateLeftMax() {
        long height = 1L + up.getValueInt() + down.getValueInt();
        int effectiveDepth = Math.max(1, depth.getValueInt());
        long maxWidth = getMaxBlocks() / (height * effectiveDepth);
        return clampDynamicMax(maxWidth - 1L - right.getValueInt(), 0);
    }

    private int calculateRightMax() {
        long height = 1L + up.getValueInt() + down.getValueInt();
        int effectiveDepth = Math.max(1, depth.getValueInt());
        long maxWidth = getMaxBlocks() / (height * effectiveDepth);
        return clampDynamicMax(maxWidth - 1L - left.getValueInt(), 0);
    }

    private int calculateUpMax() {
        long width = 1L + left.getValueInt() + right.getValueInt();
        int effectiveDepth = Math.max(1, depth.getValueInt());
        long maxHeight = getMaxBlocks() / (width * effectiveDepth);
        return clampDynamicMax(maxHeight - 1L - down.getValueInt(), 0);
    }

    private int calculateDownMax() {
        long width = 1L + left.getValueInt() + right.getValueInt();
        int effectiveDepth = Math.max(1, depth.getValueInt());
        long maxHeight = getMaxBlocks() / (width * effectiveDepth);
        return clampDynamicMax(maxHeight - 1L - up.getValueInt(), 0);
    }

    private int calculateDepthMax() {
        long width = 1L + left.getValueInt() + right.getValueInt();
        long height = 1L + up.getValueInt() + down.getValueInt();
        long maxBlocks = getMaxBlocks();

        if (width > maxBlocks || height > maxBlocks / width) {
            return 1;
        }

        long maxDepth = maxBlocks / (width * height);
        return clampDynamicMax(maxDepth, 1);
    }

    private void applyDynamicSliderLimits() {
        left.setMaxValue(calculateLeftMax());
        right.setMaxValue(calculateRightMax());
        up.setMaxValue(calculateUpMax());
        down.setMaxValue(calculateDownMax());
        depth.setMaxValue(calculateDepthMax());
    }

    private void updateDynamicSliderLimits() {
        // First pass repairs any invalid/legacy values by clamping downward.
        // Second pass expands any maxima that became available after those clamps.
        applyDynamicSliderLimits();
        applyDynamicSliderLimits();
    }

    private void resetSliderMaxima() {
        int hardMax = getHardMax();
        left.setMaxValue(hardMax);
        right.setMaxValue(hardMax);
        up.setMaxValue(hardMax);
        down.setMaxValue(hardMax);
        depth.setMaxValue(hardMax);
    }

    private boolean isWithinBounds() {
        int leftValue = left.getValueInt();
        int rightValue = right.getValueInt();
        int upValue = up.getValueInt();
        int downValue = down.getValueInt();
        int depthValue = depth.getValueInt();
        int hardMax = getHardMax();

        if (leftValue < 0 || leftValue > hardMax ||
                rightValue < 0 || rightValue > hardMax ||
                upValue < 0 || upValue > hardMax ||
                downValue < 0 || downValue > hardMax ||
                depthValue < 1 || depthValue > hardMax) {
            return false;
        }

        int effectiveDepth = Math.max(1, depthValue);
        long width = 1L + leftValue + rightValue;
        long height = 1L + upValue + downValue;
        long maxBlocks = getMaxBlocks();

        // Division-based checks avoid overflowing long if a config is set unusually high.
        if (width > maxBlocks) {
            return false;
        }

        long remainingAfterWidth = maxBlocks / width;
        if (height > remainingAfterWidth) {
            return false;
        }

        return effectiveDepth <= remainingAfterWidth / height;
    }

    private String getSizeString() {
        int x = 1 + left.getValueInt() + right.getValueInt();
        int y = 1 + up.getValueInt() + down.getValueInt();
        int z = Math.max(1, depth.getValueInt());

        return String.format("%d x %d x %d", x, y, z);
    }

    private long getCurrentBlockCount() {
        long width = 1L + left.getValueInt() + right.getValueInt();
        long height = 1L + up.getValueInt() + down.getValueInt();
        int effectiveDepth = Math.max(1, depth.getValueInt());
        return width * height * effectiveDepth;
    }

    private String getBlockCountString() {
        return String.format("Block Count: %d / %d", getCurrentBlockCount(), getMaxBlocks());
    }

    private void updateIsValid() {
        this.isValidSize = isWithinBounds();
    }

    private void updateSizeString() {
        this.sizeString = getSizeString();
    }

    private void sendPacket() {
        if (isWithinBounds()) {
            PacketDistributor.sendToServer(new DestructionRangesPayload(left.getValueInt(), right.getValueInt(), up.getValueInt(), down.getValueInt(), depth.getValueInt()));
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font, this.sizeString, width / 2, (height / 2) + 40, this.isValidSize ? 0x00FF00 : 0xFF2000);
        guiGraphics.drawCenteredString(font, this.getBlockCountString(), width / 2, (height / 2) + 50, this.isValidSize ? 0x00FF00 : 0xFF2000);
        if (!this.isValidSize) {
            guiGraphics.drawCenteredString(font, Component.translatable("buildinggadgets2.screen.destructiontoolarge"), width / 2, (height / 2) + 50, 0xFF2000);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
        return super.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        if (keyDown && !InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), KeyBindings.menuSettings.getKey().getValue())) {
            onClose();
        }
        super.tick();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (keyDown)
            return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
        InputConstants.Key mouseKey = InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_);
        if (p_keyPressed_1_ == 256 || minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            onClose();
            return true;
        }

        return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

}

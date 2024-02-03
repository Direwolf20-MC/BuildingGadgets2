package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.screen.widgets.GuiIconActionable;
import com.direwolf20.buildinggadgets2.client.screen.widgets.IncrementalSliderWidget;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.GadgetActionPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.gadgetaction.ActionGadget;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.gadgetaction.GadgetActionCodecs;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
                    depth.setValue(originalDepth);
                    right.setValue(originalRight);
                    left.setValue(originalLeft);
                    up.setValue(originalUp);
                    down.setValue(originalDown);
                    sendPacket();
                })
                .pos((x - 30) + 32, y + 65)
                .size(60, 20)
                .build());

        Button undo_button = new GuiIconActionable(x - 55, y - 75, "undo", Component.translatable("buildinggadgets2.radialmenu.undo"), false, send -> {
            if (send) {
                PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.UNDO));
            }

            return false;
        });
        this.addRenderableWidget(undo_button);

        Button anchorButton = new GuiIconActionable(x - 25, y - 75, "anchor", Component.translatable("buildinggadgets2.radialmenu.anchor"), true, send -> {
            if (send) {
                PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.ANCHOR));
            }

            return !GadgetNBT.getAnchorPos(destructionGadget).equals(GadgetNBT.nullPos);
        });
        this.addRenderableWidget(anchorButton);

        Button affectTiles = new GuiIconActionable(x + 5, y - 75, "affecttiles", Component.translatable("buildinggadgets2.screen.affecttiles"), true, send -> {
            if (send) {
                PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.TOGGLE_SETTING, Util.make(new CompoundTag(), c -> c.putString("setting", "affecttiles"))));
            }

            return GadgetNBT.getSetting(destructionGadget, "affecttiles");
        });
        this.addRenderableWidget(affectTiles);

        Button rayTrace = new GuiIconActionable(x + 35, y - 75, "raytrace_fluid", Component.translatable("buildinggadgets2.radialmenu.raytracefluids"), true, send -> {
            if (send) {
                PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.TOGGLE_SETTING, Util.make(new CompoundTag(), c -> c.putString("setting", "raytracefluid"))));
            }

            return GadgetNBT.getSetting(destructionGadget, "raytracefluid");
        });
        this.addRenderableWidget(rayTrace);

        renderTypeButton = new GuiIconActionable(x + 65, y - 75, "raytrace_fluid", Component.translatable(renderType.getLang()), false, send -> {
            if (send) {
                renderType = renderType.next();
                renderTypeButton.setMessage(Component.translatable(renderType.getLang()));
                PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.RENDER_CHANGE, Util.make(new CompoundTag(), c -> c.putByte("renderType", renderType.getPosition()))));
            }

            return false;
        });
        this.addRenderableWidget(renderTypeButton);

        sliders.clear();
        sliders.add(depth = this.createSlider(x - (70 / 2), y - (14 / 2), Component.translatable("buildinggadgets2.screen.depth"), GadgetNBT.getToolValue(destructionGadget, "depth")));
        sliders.add(right = this.createSlider(x + (70 + 5), y - (14 / 2), Component.translatable("buildinggadgets2.screen.right"), GadgetNBT.getToolValue(destructionGadget, "right")));
        sliders.add(left = this.createSlider(x - (70 * 2) - 5, y - (14 / 2), Component.translatable("buildinggadgets2.screen.left"), GadgetNBT.getToolValue(destructionGadget, "left")));
        sliders.add(up = this.createSlider(x - (70 / 2), y - 35, Component.translatable("buildinggadgets2.screen.up"), GadgetNBT.getToolValue(destructionGadget, "up")));
        sliders.add(down = this.createSlider(x - (70 / 2), y + 20, Component.translatable("buildinggadgets2.screen.down"), GadgetNBT.getToolValue(destructionGadget, "down")));

        originalDepth = depth.getValueInt();
        originalLeft = left.getValueInt();
        originalRight = right.getValueInt();
        originalUp = up.getValueInt();
        originalDown = down.getValueInt();

        updateSizeString();
        updateIsValid();

        // Adds their buttons to the gui
        sliders.forEach(gui -> gui.getComponents().forEach(this::addRenderableWidget));
    }

    public IncrementalSliderWidget createSlider(int x, int y, MutableComponent prefix, int value) {
        return new IncrementalSliderWidget(x, y, 70, 14, 0D, 16D, prefix.append(": "), value, this::onSliderUpdate);
    }

    public void onSliderUpdate(IncrementalSliderWidget widget) {
        this.updateSizeString();
        this.updateIsValid();
        sendPacket();
    }

    private boolean isWithinBounds() {
        int x = left.getValueInt() + right.getValueInt();
        int y = up.getValueInt() + down.getValueInt();
        int z = depth.getValueInt();
        int dim = 16;

        return x <= dim && y <= dim && z <= dim;
    }

    private String getSizeString() {
        int x = 1 + left.getValueInt() + right.getValueInt();
        int y = 1 + up.getValueInt() + down.getValueInt();
        int z = depth.getValueInt();

        return String.format("%d x %d x %d", x, y, z);
    }

    private void updateIsValid() {
        this.isValidSize = isWithinBounds();
        /*if (!isValidSize && this.confirm.active) {
            this.confirm.setFGColor(0xFF2000);
            this.confirm.active = false;
        }

        if (isValidSize && !this.confirm.active) {
            this.confirm.clearFGColor();
            this.confirm.active = true;
        }*/
    }

    private void updateSizeString() {
        this.sizeString = getSizeString();
    }

    private void sendPacket() {
        if (isWithinBounds()) {
            PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(
                    ActionGadget.DESTRUCTION_RANGES,
                    (CompoundTag) GadgetActionCodecs.DestructionRanges.CODEC.encodeStart(NbtOps.INSTANCE, new GadgetActionCodecs.DestructionRanges(left.getValueInt(), right.getValueInt(), up.getValueInt(), down.getValueInt(), depth.getValueInt())).get().orThrow()
            ));
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.drawCenteredString(font, this.sizeString, width / 2, (height / 2) + 40, this.isValidSize ? 0x00FF00 : 0xFF2000);
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

/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */
package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.OurSounds;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.client.screen.widgets.GuiIconActionable;
import com.direwolf20.buildinggadgets2.client.screen.widgets.IncrementalSliderWidget;
import com.direwolf20.buildinggadgets2.common.items.*;
import com.direwolf20.buildinggadgets2.common.network.data.*;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class ModeRadialMenu extends Screen {
    private static final ImmutableList<Identifier> signsCopyPaste = ImmutableList.of(
            Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "textures/gui/mode/copy.png"),
            Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "textures/gui/mode/paste.png")
    );
    private final List<Button> conditionalButtons = new ArrayList<>();
    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    private ArrayList<BaseMode> arrayOfModes = new ArrayList();
    private boolean cutForSure = false;
    private BaseMode mode;
    private Button renderTypeButton;
    private GadgetNBT.RenderTypes renderType;

    public ModeRadialMenu(ItemStack stack) {
        super(Component.literal(""));

        if (stack.getItem() instanceof BaseGadget) {
            this.setSocketable(stack);
        }
        mode = GadgetNBT.getMode(stack);
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(baseVec.dot(mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y
                ? 360F - ang
                : ang;
    }

    public void setSocketable(ItemStack stack) {
        if (stack.getItem() instanceof BaseGadget actualGadget) {
            this.segments = GadgetModes.INSTANCE.getModesForGadget(actualGadget.gadgetTarget()).size();
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

    }

    @Override
    public void init() {
        ItemStack tool = this.getGadget();
        renderType = GadgetNBT.getRenderType(tool);
        if (tool.getItem() instanceof BaseGadget actualGadget) {
            ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(actualGadget.gadgetTarget());
            arrayOfModes = new ArrayList<>(modesForGadget); // This is required to work with index's
        } else {
            return;
        }
        this.conditionalButtons.clear();

        Button rayTrace = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.raytracefluids"), "raytrace_fluid", ScreenPosition.RIGHT, send -> {
            if (send) {
                ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.RAYTRACE_FLUID.getName()));
            }

            return GadgetNBT.getSetting(tool, GadgetNBT.ToggleableSettings.RAYTRACE_FLUID.getName());
        });
        this.addRenderableWidget(rayTrace);

        //Building Gadget Only
        if (tool.getItem() instanceof GadgetBuilding) {
            Button placeOnTop = new PositionedIconActionable(Component.translatable("buildinggadgets2.screen.placeatop"), "building_place_atop", ScreenPosition.RIGHT, true, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName()));
                }

                return GadgetNBT.getSetting(tool, GadgetNBT.ToggleableSettings.PLACE_ON_TOP.getName());
            });
            this.addRenderableWidget(placeOnTop);
        }

        //Building and Exchanging Gadget Only
        if (tool.getItem() instanceof GadgetBuilding || tool.getItem() instanceof GadgetExchanger) {
            int widthSlider = 82;
            IncrementalSliderWidget sliderRange = new IncrementalSliderWidget(width / 2 - widthSlider / 2, height / 2 + 72, widthSlider, 14, 1, /*Config.GADGETS.maxRange.get()*/15, Component.translatable("buildinggadgets2.gui.range").append(": "), GadgetNBT.getToolRange(tool), slider -> {
                sendRangeUpdate(slider.getValueInt());
            });
            sliderRange.getComponents().forEach(this::addRenderableWidget);
        }

        //Exchanging Gadget Only
        if (tool.getItem() instanceof GadgetExchanger) {
            Button affectTiles = new PositionedIconActionable(Component.translatable("buildinggadgets2.screen.affecttiles"), "affecttiles", ScreenPosition.RIGHT, true, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.AFFECT_TILES.getName()));
                }

                return GadgetNBT.getSetting(tool, GadgetNBT.ToggleableSettings.AFFECT_TILES.getName());
            });
            this.addRenderableWidget(affectTiles);
        }

        //Cut Paste Gadget Only
        if (tool.getItem() instanceof GadgetCutPaste) {
            addRenderableWidget(new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.cut"), "cut", ScreenPosition.LEFT, false, send -> {
                if (send) {
                    if (GadgetNBT.hasCopyUUID(tool) && !cutForSure) {
                        this.getMinecraft().player.sendOverlayMessage(Component.translatable("buildinggadgets2.messages.overwritecut"));
                        cutForSure = true;
                        return false;
                    }
                    ClientPacketDistributor.sendToServer(new CutPayload());

                    int modeIndex = arrayOfModes.indexOf(mode);
                    modeIndex = modeIndex == 0 ? 1 : 0;

                    ClientPacketDistributor.sendToServer(new ModeSwitchPayload(false, arrayOfModes.get(modeIndex).getId()));
                    mode = arrayOfModes.get(modeIndex);
                }

                return false;
            }));
        }

        //Copy Paste Gadget Only
        if (tool.getItem() instanceof GadgetCopyPaste) {
            Button materialList = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.materiallist"), "copypaste_materiallist", ScreenPosition.RIGHT, false, send -> {
                if (send) {
                    var mode = GadgetNBT.getMode(tool);
                    if (GadgetNBT.hasCopyUUID(tool) && mode.getId().getPath().equals("paste")) {
                        getMinecraft().setScreen(new MaterialListGUI(tool));
                    }
                }

                return false;
            });
            addRenderableWidget(materialList);
            conditionalButtons.add(materialList);
        }

        //Cut Paste or Copy Paste Gadget Only
        if (tool.getItem() instanceof GadgetCutPaste || tool.getItem() instanceof GadgetCopyPaste) {
            Button pastereplace = new PositionedIconActionable(Component.translatable("buildinggadgets2.screen.paste_replace"), "paste_replace", ScreenPosition.RIGHT, true, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.PASTE_REPLACE.getName()));
                }

                return GadgetNBT.getPasteReplace(tool);
            });
            this.addRenderableWidget(pastereplace);

            addRenderableWidget(new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.copypastemenu"), "copypaste_opengui", ScreenPosition.RIGHT, false, send -> {
                if (!send)
                    return false;

                assert this.getMinecraft().player != null;

                getMinecraft().player.closeContainer();
                if (mode.getId().getPath().equals("paste"))
                    getMinecraft().setScreen(new PasteGUI(tool));
                else
                    getMinecraft().setScreen(new CopyGUI(tool));
                return true;
            }));

            addRenderableWidget(new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.rotate"), "rotate", ScreenPosition.LEFT, false, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new RotatePayload());
                }

                return false;
            }));
        }

        //Everything but Cut and Paste Gadget
        if (!(tool.getItem() instanceof GadgetCutPaste)) {
            Button undo_button = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.undo"), "undo", ScreenPosition.LEFT, false, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new UndoPayload());
                }

                return false;
            });
            addRenderableWidget(undo_button);

            Button bind_button = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.bind"), "building_place_atop", ScreenPosition.LEFT, true, send -> {
                if (send) {
                    ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.BIND.getName()));
                }

                return GadgetNBT.getSetting(tool, GadgetNBT.ToggleableSettings.BIND.getName());
            });
            addRenderableWidget(bind_button);
        }

        Button fuzzy_button = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.fuzzy"), "fuzzy", ScreenPosition.RIGHT, send -> {
            if (send) {
                ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.FUZZY.getName()));
            }

            return GadgetNBT.getSetting(this.getGadget(), GadgetNBT.ToggleableSettings.FUZZY.getName());
        });
        addRenderableWidget(fuzzy_button);
        conditionalButtons.add(fuzzy_button);

        Button connected_button = new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.connected_area"), "connected_area", ScreenPosition.RIGHT, send -> {
            if (send) {
                ClientPacketDistributor.sendToServer(new ToggleSettingPayload(GadgetNBT.ToggleableSettings.CONNECTED_AREA.getName()));
            }

            return GadgetNBT.getSetting(this.getGadget(), GadgetNBT.ToggleableSettings.CONNECTED_AREA.getName());
        });
        addRenderableWidget(connected_button);
        conditionalButtons.add(connected_button);

        addRenderableWidget(new PositionedIconActionable(Component.translatable("buildinggadgets2.radialmenu.anchor"), "anchor", ScreenPosition.LEFT, send -> {
            if (send) {
                ClientPacketDistributor.sendToServer(new AnchorPayload());
            }

            return !GadgetNBT.getAnchorPos(tool).equals(GadgetNBT.nullPos);
        }));

        renderTypeButton = new PositionedIconActionable(Component.translatable(renderType.getLang()), "raytrace_fluid", ScreenPosition.LEFT, false, send -> {
            if (send) {
                renderType = renderType.next();
                renderTypeButton.setMessage(Component.translatable(renderType.getLang()));
                ClientPacketDistributor.sendToServer(new RenderChangePayload(renderType.getPosition()));
            }

            return false;
        });
        this.addRenderableWidget(renderTypeButton);


        this.updateButtons();
    }

    private void updateButtons() {
        int buttonSize = 24;
        int paddingBetweenButtonsY = 10;
        int step = buttonSize + paddingBetweenButtonsY;

        // Count visible buttons per side
        int countLeft = 0;
        int countRight = 0;
        for (GuiEventListener widget : children()) {
            if (!(widget instanceof PositionedIconActionable button) || !button.visible)
                continue;
            if (button.position == ScreenPosition.LEFT) countLeft++;
            else if (button.position == ScreenPosition.RIGHT) countRight++;
        }

        // Center each column around the vertical midpoint (height / 2)
        int centerY = height / 2;
        int totalLeftHeight = countLeft * buttonSize + (countLeft - 1) * paddingBetweenButtonsY;
        int totalRightHeight = countRight * buttonSize + (countRight - 1) * paddingBetweenButtonsY;
        int yPosLeft = centerY - totalLeftHeight / 2;
        int yPosRight = centerY - totalRightHeight / 2;

        for (GuiEventListener widget : children()) {
            if (!(widget instanceof PositionedIconActionable button) || !button.visible)
                continue;

            if (button.position == ScreenPosition.RIGHT) {
                int xPos = width / 2 + 70;
                button.setWidth(buttonSize);
                button.setHeight(buttonSize);
                button.setX(xPos);
                button.setY(yPosRight);
                yPosRight += step;
            } else if (button.position == ScreenPosition.LEFT) {
                int xPos = width / 2 - 70 - buttonSize;
                button.setWidth(buttonSize);
                button.setHeight(buttonSize);
                button.setX(xPos);
                button.setY(yPosLeft);
                yPosLeft += step;
            }
        }
    }

    private ItemStack getGadget() {
        assert this.getMinecraft().player != null;
        return BaseGadget.getGadget(this.getMinecraft().player);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mx, int my, float partialTicks) {
        Matrix3x2fStack matrices = guiGraphics.pose();
        float speedOfButtonGrowth = 7f; //How fast the buttons move during initial window opening
        float fract = Math.min(speedOfButtonGrowth, this.timeIn + partialTicks) / speedOfButtonGrowth;
        int x = this.width / 2;
        int y = this.height / 2;

        int radiusMin = 26;
        int radiusMax = 60;
        double dist = new Vec3(x, y, 0).distanceTo(new Vec3(mx, my, 0));
        boolean inRange = this.segments != 0 && dist > radiusMin && dist < radiusMax;


        // This triggers the animation on creation - only affects side buttons and slider(s)
        matrices.pushMatrix();
        matrices.translate((1 - fract) * x, (1 - fract) * y);
        matrices.scale(fract, fract);
        super.extractRenderState(guiGraphics, mx, my, partialTicks);
        matrices.popMatrix();

        if (this.segments == 0) {
            return;
        }

        float angle = mouseAngle(x, y, mx, my);

        float totalDeg = 0;
        float degPer = 360F / this.segments;

        List<NameDisplayData> nameData = new ArrayList<>();

        ItemStack tool = this.getGadget();
        if (tool.isEmpty()) {
            return;
        }

        this.slotSelected = -1;

        int modeIndex = arrayOfModes.indexOf(mode);

        boolean shouldCenter = (this.segments + 2) % 4 == 0;
        int indexBottom = this.segments / 4;
        int indexTop = indexBottom + this.segments / 2;
        Matrix3x2f sliceMatrix = new Matrix3x2f(matrices);
        ScreenRectangle scissorArea = guiGraphics.peekScissorStack();
        for (int seg = 0; seg < this.segments; seg++) {
            boolean mouseInSector = this.isCursorInSlice(angle, totalDeg, degPer, inRange);
            //This makes the individual segments pop up one after another, a cool lil animation. Adjust the 6f to change it
            float delayBetweenSegments = 1f;
            float speedOfSegmentGrowth = 10f;
            float radius = Math.max(0F, Math.min((this.timeIn + partialTicks - seg * delayBetweenSegments / this.segments) * speedOfSegmentGrowth, radiusMax));
            float gs = 0.25F;
            if (seg % 2 == 0) {
                gs += 0.1F;
            }

            float r = gs;
            float g = gs + (seg == modeIndex
                    ? 1F
                    : 0.0F);
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                this.slotSelected = seg;
                r = g = b = 1F;
            }

            float midRad = (float) ((degPer / 2 + totalDeg) / 180F * Math.PI);
            int xp = (int) (x + Math.cos(midRad) * radius);
            int yp = (int) (y + Math.sin(midRad) * radius);
            nameData.add(new NameDisplayData(xp, yp, mouseInSector, shouldCenter && (seg == indexBottom || seg == indexTop)));

            int color = ARGB.color((int) (a * 255F), (int) (r * 255F), (int) (g * 255F), (int) (b * 255F));
            guiGraphics.submitGuiElementRenderState(new PieSliceRenderState(
                    OurRenderTypes.DEBUG_TRIANGLE_STRIP,
                    sliceMatrix,
                    x,
                    y,
                    totalDeg,
                    degPer,
                    radius,
                    color,
                    scissorArea
            ));
            totalDeg += degPer;
        }

        // This is the naming logic for the text that pops up
        for (int i = 0; i < nameData.size(); i++) {
            matrices.pushMatrix();
            NameDisplayData data = nameData.get(i);
            int xp = data.getX();
            int yp = data.getY();

            String name = Component.translatable(arrayOfModes.get(i).i18n()).getString();

            int xsp = xp - 4;
            int ysp = yp;
            int width = font.width(name);

            if (xsp < x) {
                xsp -= width - 8;
            }
            if (ysp < y) {
                ysp -= 9;
            }

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (data.isSelected())
                guiGraphics.text(font, name, xsp + (data.isCentralized() ? width / 2 - 4 : 0), ysp, color.getRGB(), true);

            double mod = 0.7;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            int tint = ARGB.color(color.getRed(), color.getGreen(), color.getBlue());
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, arrayOfModes.get(i).icon(), xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16, tint);

            matrices.popMatrix();
        }
        float s = 1.8F * fract;
        matrices.pushMatrix();
        matrices.scale(s, s);
        matrices.translate(x / s - (tool.getItem() instanceof GadgetCopyPaste ? 8 : 8.5f), y / s - 8);
        guiGraphics.item(tool, 0, 0);
        matrices.popMatrix();
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private void changeMode() {
        if (this.slotSelected >= 0) {
            assert getMinecraft().player != null;
            ClientPacketDistributor.sendToServer(new ModeSwitchPayload(false, arrayOfModes.get(this.slotSelected).getId()));

            mode = arrayOfModes.get(this.slotSelected);
            OurSounds.playSound(Registration.BEEP.get());
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.changeMode();
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void tick() {
        if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), KeyBindings.menuSettings.getKey().getValue())) {
            onClose();
            changeMode();
        }

        ImmutableSet<KeyMapping> set = ImmutableSet.of(getMinecraft().options.keyUp, getMinecraft().options.keyLeft, getMinecraft().options.keyDown, getMinecraft().options.keyRight, getMinecraft().options.keyShift, getMinecraft().options.keySprint, getMinecraft().options.keyJump);
        for (KeyMapping k : set)
            KeyMapping.set(k.getKey(), k.isDown());

        this.timeIn++;
        ItemStack tool = this.getGadget();

        boolean showButton = true;
        boolean changed = false;
        for (Button button : this.conditionalButtons) {
            if (button.getMessage().equals(Component.translatable("buildinggadgets2.radialmenu.fuzzy")) || button.getMessage().equals(Component.translatable("buildinggadgets2.radialmenu.connected_area"))) {
                if (tool.getItem() instanceof GadgetBuilding)
                    showButton = mode.getId().getPath().equals("surface");
                else
                    showButton = tool.getItem() instanceof GadgetExchanger;
            } else if (button.getMessage().equals(Component.translatable("buildinggadgets2.radialmenu.materiallist"))) {
                showButton = mode.getId().getPath().equals("paste");
            }
            if (button.visible != showButton) {
                button.visible = showButton;
                changed = true;
            }
        }
        if (changed) {
            this.updateButtons();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void sendRangeUpdate(int valueNew) {
        if (valueNew != GadgetNBT.getToolRange(this.getGadget())) {
            ClientPacketDistributor.sendToServer(new RangeChangePayload(valueNew));
        }
    }

    public enum ScreenPosition {
        RIGHT, LEFT, BOTTOM, TOP
    }

    private static final class NameDisplayData {
        private final int x;
        private final int y;
        private final boolean selected;
        private final boolean centralize;

        private NameDisplayData(int x, int y, boolean selected, boolean centralize) {
            this.x = x;
            this.y = y;
            this.selected = selected;
            this.centralize = centralize;
        }

        private int getX() {
            return this.x;
        }

        private int getY() {
            return this.y;
        }

        private boolean isSelected() {
            return this.selected;
        }

        private boolean isCentralized() {
            return this.centralize;
        }
    }

    private static class PositionedIconActionable extends GuiIconActionable {
        private ScreenPosition position;

        PositionedIconActionable(Component message, String icon, ScreenPosition position, boolean isSelectable, Predicate<Boolean> action) {
            super(0, 0, icon, message, isSelectable, action);

            this.position = position;
        }

        PositionedIconActionable(Component message, String icon, ScreenPosition position, Predicate<Boolean> action) {
            this(message, icon, position, true, action);
        }
    }

    private record PieSliceRenderState(
            RenderPipeline pipeline,
            Matrix3x2f pose,
            int cx,
            int cy,
            float startDeg,
            float spanDeg,
            float outerRadius,
            int color,
            @Nullable ScreenRectangle scissorArea
    ) implements GuiElementRenderState {
        private static final float INNER_RATIO = 1F / 2.3F;

        @Override
        public void buildVertices(VertexConsumer buffer) {
            if (outerRadius <= 0F || spanDeg <= 0F) return;
            float innerRadius = outerRadius * INNER_RATIO;
            for (float i = spanDeg; i >= 0; i--) {
                float rad = (i + startDeg) * (float) Math.PI / 180F;
                float cos = (float) Math.cos(rad);
                float sin = (float) Math.sin(rad);
                buffer.addVertexWith2DPose(pose, cx + cos * innerRadius, cy + sin * innerRadius).setColor(color);
                buffer.addVertexWith2DPose(pose, cx + cos * outerRadius, cy + sin * outerRadius).setColor(color);
            }
        }

        @Override
        public TextureSetup textureSetup() {
            return TextureSetup.noTexture();
        }

        @Override
        public @Nullable ScreenRectangle bounds() {
            int r = (int) Math.ceil(outerRadius) + 1;
            ScreenRectangle rect = new ScreenRectangle(cx - r, cy - r, r * 2, r * 2).transformMaxBounds(pose);
            return scissorArea != null ? scissorArea.intersection(rect) : rect;
        }
    }

    private static class Vector2f {
        public float x;
        public float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public final float dot(Vector2f v1) {
            return (this.x * v1.x + this.y * v1.y);
        }

        public final float length() {
            return (float) Math.sqrt(this.x * this.x + this.y * this.y);
        }
    }
}

/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.renderer.GuiTemplatePreview;
import com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.common.items.Redprint;
import com.direwolf20.buildinggadgets2.common.items.TemplateItem;
import com.direwolf20.buildinggadgets2.common.network.data.SendPastePayload;
import com.direwolf20.buildinggadgets2.common.network.data.UpdateTemplateManagerPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.Template;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.joml.Matrix3x2f;

import java.util.ArrayList;
import java.util.UUID;

public class TemplateManagerGUI extends AbstractContainerScreen<TemplateManagerContainer> {
    private static final Identifier background = Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "textures/gui/template_manager.png");

    private final Rect2i panel = new Rect2i((8 - 20), 12, 136, 80);
    private boolean panelClicked;
    private int clickButton, clickX, clickY;
    private float initRotX, initRotY, initZoom, initPanX, initPanY;
    private float momentumX, momentumY;
    private float rotX = 0, rotY = 0, zoom = 1;
    private float panX = 0, panY = 0;

    private EditBox nameField;
    private Button buttonSave, buttonLoad, buttonCopy, buttonPaste, buttonToggleViewport;

    private int renderSlot = 0;
    public static UUID gadgetUUID = UUID.randomUUID(); //Cached version of whatevers in slot 0
    public static UUID templateUUID = UUID.randomUUID(); //Cached version of whatevers in slot 1
    public static UUID copyPasteUUIDCache = UUID.randomUUID(); //A unique ID of the copy/paste, which we'll use to determine if we need to request an update from the server Its initialized as random to avoid having to null check it
    private static ArrayList<StatePos> statePosCache;

    private final TemplateManagerBE be;
    private final TemplateManagerContainer container;

    private ScrollingMaterialList scrollingList;
    private boolean showMaterialList = false;

    public TemplateManagerGUI(TemplateManagerContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, Component.literal(""));

        this.container = container;
        this.be = container.getTe();
    }

    @Override
    public void init() {
        super.init();
        this.nameField = new EditBox(this.font, (this.leftPos - 20) + 8, topPos - 5, imageWidth - 16, this.font.lineHeight + 3, Component.translatable("buildinggadgets2.screen.namefieldtext"));
        updateNameField();

        int x = (leftPos - 20) + 180;
        buttonSave = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.save"), b -> onSave()).pos(x, topPos + 15).size(60, 15).build());
        buttonLoad = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.load"), b -> onLoad()).pos(x, topPos + 32).size(60, 15).build());
        buttonCopy = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.copy"), b -> onCopy()).pos(x, topPos + 50).size(60, 15).build());
        buttonPaste = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.paste"), b -> onPaste()).pos(x, topPos + 67).size(60, 15).build());
        buttonToggleViewport = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.render"), b -> onToggleViewport()).pos(x, topPos + 85).size(60, 15).build());

        this.renderSlot = 1;

        this.nameField.setMaxLength(50);
        this.nameField.setVisible(true);
        addRenderableWidget(nameField);

        this.scrollingList = new ScrollingMaterialList(this, leftPos + panel.getX(), (topPos + panel.getY()), panel.getWidth(), panel.getHeight(), container.getSlot(renderSlot).getItem());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTicks);
        updateAsNeeded();
        if (showMaterialList) {
            if (!renderables.contains(scrollingList))
                this.addRenderableWidget(scrollingList);
        } else {
            this.removeWidget(scrollingList);
            submit3DPreview(graphics);
        }
    }

    /**
     * Fill the preview rect with an opaque black backdrop, then queue a {@link GuiTemplatePreview.State}
     * onto the PiP pipeline. Vanilla's {@code GuiRenderer} will spin up / reuse a
     * {@link GuiTemplatePreview} (registered in {@code ClientSetup#registerPictureInPictureRenderers}),
     * give it a private render target sized to the panel rect × guiScale, run
     * {@link GuiTemplatePreview#renderToTexture} (which bakes the retained GPU mesh on cache miss
     * then issues our manual RenderPass against the pre-uploaded vertex buffers), and blit the
     * result into this rectangle with the current scissor applied.
     */
    private void submit3DPreview(GuiGraphicsExtractor graphics) {
        int x0 = leftPos + panel.getX();
        int y0 = topPos + panel.getY();
        int x1 = x0 + panel.getWidth();
        int y1 = y0 + panel.getHeight();

        // Opaque backdrop behind the preview. PictureInPictureRenderer clears the PiP texture to
        // the transparent color before renderToTexture runs; without this fill the GUI background
        // bleeds through when the rotated preview's bounding box doesn't fill the panel.
        graphics.fill(x0, y0, x1, y1, 0xFF202020);

        if (statePosCache == null || statePosCache.isEmpty()) return;

        int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
        if (guiScale < 1) guiScale = 1;
        ScreenRectangle scissor = graphics.peekScissorStack();

        GuiTemplatePreview.State state = new GuiTemplatePreview.State(
                x0, y0, x1, y1,
                new Matrix3x2f(graphics.pose()),
                scissor,
                guiScale,
                rotX, rotY, zoom, panX, panY,
                copyPasteUUIDCache,
                statePosCache);
        graphics.submitPictureInPictureRenderState(state);
    }

    public void updateNameField() {
        ItemStack gadgetStack = container.getSlot(0).getItem();
        ItemStack templateStack = container.getSlot(1).getItem();

        String gadgetName = GadgetNBT.getTemplateName(gadgetStack);
        String templateName = GadgetNBT.getTemplateName(templateStack);

        this.nameField.setValue(templateName.isEmpty() ? gadgetName : templateName);
    }

    public void updateAsNeeded() {
        if (scrollingList == null) return; // init() hasn't finished the first time extractRenderState fires
        ItemStack gadgetStack = container.getSlot(0).getItem();
        ItemStack templateStack = container.getSlot(1).getItem();
        boolean updatePanel = false;

        UUID gadgetStackUUID = GadgetNBT.getUUID(gadgetStack);
        UUID templateStackUUID = GadgetNBT.getUUID(templateStack);

        if (!gadgetStackUUID.equals(gadgetUUID)) {
            gadgetUUID = gadgetStackUUID;
            updatePanel = true;
        }
        if (!templateStackUUID.equals(templateUUID)) {
            templateUUID = templateStackUUID;
            updatePanel = true;
        }

        if (updatePanel) //Only update the panel if the stacks were changed
            updateNameField();
        updatePanelIfNeeded();
    }

    public boolean updatePanelIfNeeded() {
        ItemStack gadget = container.getSlot(renderSlot).getItem();
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof TemplateItem || gadget.getItem() instanceof Redprint || gadget.getItem() instanceof GadgetCutPaste)) {
            copyPasteUUIDCache = UUID.randomUUID(); //Randomize the cached UUID so it rebuilds for next time
            resetViewport();
            scrollingList.setTemplateItem(gadget);
            return false;
        }
        if (!BG2DataClient.isClientUpToDate(gadget)) { //Have the BG2DataClient class check if its up to date
            return false; //If not up to date, we need to return false, since theres no need to regen the render if its out of date! We'll check again next draw frame
        }
        UUID BG2ClientUUID = BG2DataClient.getCopyUUID(gadgetUUID);
        if (BG2ClientUUID != null && copyPasteUUIDCache.equals(BG2ClientUUID)) //If the cache this class has matches the client cache for this gadget, no need to rebuild
            return false;
        //If we get here, the copy paste we have stored here differs from whats in the client AND the client is up to date, so rebuild!
        copyPasteUUIDCache = BG2ClientUUID; //Cache the new copyPasteUUID for next cycle
        statePosCache = BG2DataClient.getLookupFromUUID(gadgetUUID);
        // GuiTemplatePreview's renderToTexture rebuilds its retained-GPU-mesh cache on identity
        // mismatch against the statePosCache reference we hand it, so no explicit bake step here.
        scrollingList.setTemplateItem(gadget);
        return true; //Need a render update!
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);

        graphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos - 20, topPos - 12, 0, 0, imageWidth, imageHeight + 25, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, background, (leftPos - 20) + imageWidth, topPos + 8, imageWidth + 3, 30, 71, imageHeight, 256, 256);

        if (buttonCopy != null && buttonPaste != null && buttonLoad != null && !buttonCopy.isHovered() && !buttonPaste.isHovered()) {
            if (buttonLoad.isHovered())
                graphics.blit(RenderPipelines.GUI_TEXTURED, background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth, 0, 17, 24, 256, 256);
            else
                graphics.blit(RenderPipelines.GUI_TEXTURED, background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth + 17, 0, 16, 24, 256, 256);
        }
    }

    private void resetViewport() {
        rotX = 0;
        rotY = 0;
        zoom = 1;
        momentumX = 0;
        momentumY = 0;
        panX = 0;
        panY = 0;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        int mouseButton = event.button();
        if (panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
            if (showMaterialList)
                this.setFocused(scrollingList);
            else {
                clickButton = mouseButton;
                panelClicked = true;
                clickX = (int) getMinecraft().mouseHandler.xpos();
                clickY = (int) getMinecraft().mouseHandler.ypos();
            }
        }

        if (!panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
            this.scrollingList.setSelected(null);
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        panelClicked = false;
        initRotX = rotX;
        initRotY = rotY;
        initPanX = panX;
        initPanY = panY;
        initZoom = zoom;

        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (showMaterialList)
            return this.getFocused() != null && this.isDragging() && event.button() == 0 && this.getFocused().mouseDragged(event, dx, dy);
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            this.onClose();
            return true;
        }

        return this.nameField.isFocused() ? this.nameField.keyPressed(event) : super.keyPressed(event);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        super.extractContents(graphics, mouseX, mouseY, partialTicks);

        // Post-slot overlay. In 1.21.1 this used PoseStack.translate(0, 0, 1000) to draw above
        // the slots; in 26.1, pose() is a Matrix3x2fStack (2D only), so we use nextStratum() to
        // push onto a higher GUI render stratum that is drawn on top of everything above.
        if (buttonSave != null && buttonLoad != null && buttonPaste != null
                && (buttonSave.isHovered() || buttonLoad.isHovered() || buttonPaste.isHovered())) {
            graphics.nextStratum();
            graphics.pose().pushMatrix();
            graphics.pose().translate(leftPos, topPos);
            drawSlotOverlay(graphics, buttonLoad.isHovered() ? container.getSlot(0) : container.getSlot(1));
            graphics.pose().popMatrix();
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (panelClicked) {
            if (clickButton == 0) {
                rotX = initRotX - ((int) getMinecraft().mouseHandler.ypos() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHandler.xpos() - clickX);
            } else if (clickButton == 1) {
                panX = initPanX + ((int) getMinecraft().mouseHandler.xpos() - clickX) / 8f;
                panY = initPanY + ((int) getMinecraft().mouseHandler.ypos() - clickY) / 8f;
            } else if (clickButton == 2) {
                resetViewport();
            }
        }

        rotX += momentumX;
        rotY += momentumY;
        float momentumDampening = 0.98f;
        momentumX *= momentumDampening;
        momentumY *= momentumDampening;

        if (!nameField.isFocused() && nameField.getValue().isEmpty())
            graphics.text(font, Component.translatable("buildinggadgets2.screen.templateplaceholder"), nameField.getX() - leftPos + 4, (nameField.getY() + 2) - topPos, 0xFF636363);
    }

    private void drawSlotOverlay(GuiGraphicsExtractor graphics, Slot slot) {
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x9C00FFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double mouseDeltaX, double mouseDeltaY) {
        zoom = initZoom + ((float) mouseDeltaY * 2);
        if (zoom < -200) zoom = -200;
        if (zoom > 5000) zoom = 5000;

        return super.mouseScrolled(mouseX, mouseY, mouseDeltaX, mouseDeltaY);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (!panelClicked) {
            initRotX = rotX;
            initRotY = rotY;
            initZoom = zoom;
            initPanX = panX;
            initPanY = panY;
        }
    }

    private void onToggleViewport() {
        this.showMaterialList = !this.showMaterialList;
        if (showMaterialList)
            buttonToggleViewport.setMessage(Component.translatable("buildinggadgets2.buttons.materials"));
        else
            buttonToggleViewport.setMessage(Component.translatable("buildinggadgets2.buttons.render"));
    }

    private void onSave() {
        ClientPacketDistributor.sendToServer(new UpdateTemplateManagerPayload(be.getBlockPos(), 0, nameField.getValue()));
    }

    private void onLoad() {
        ClientPacketDistributor.sendToServer(new UpdateTemplateManagerPayload(be.getBlockPos(), 1, nameField.getValue()));
    }

    private Template getTemplate() {
        ItemStack templateStack = container.getSlot(1).getItem();
        Template template = new Template("", new ArrayList<>());
        if (templateStack.isEmpty()) return template;
        UUID templateUUID = GadgetNBT.getUUID(templateStack);
        ArrayList<StatePos> statePosCache = BG2DataClient.getLookupFromUUID(templateUUID);
        if (statePosCache == null || statePosCache.isEmpty()) return template;
        template = new Template(nameField.getValue(), statePosCache);
        return template;
    }

    private void onCopy() {
        Template template = getTemplate();
        if (template.statePosArrayList.isEmpty()) return;
        try {
            String json = template.toJson();
            getMinecraft().keyboardHandler.setClipboard(json);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void onPaste() {
        assert getMinecraft().player != null;

        String CBString = getMinecraft().keyboardHandler.getClipboard();
        ArrayList<StatePos> statePosArrayList = new ArrayList<>();
        try {
            Template template = new Template(CBString);
            if (template.statePosArrayList == null || template.statePosArrayList.equals("")) return;
            CompoundTag deserializedNBT = TagParser.parseCompoundFully(template.statePosArrayList);
            statePosArrayList = BG2Data.statePosListFromNBTMapArray(deserializedNBT);
        } catch (Exception e) {
            getMinecraft().gui.setOverlayMessage(Component.translatable("buildinggadgets2.screen.invalidjson"), false);
            // Handle the exception if the string isn't a valid NBT
            return;
        }
        if (statePosArrayList.isEmpty())
            return;
        CompoundTag serverTag = BG2Data.statePosListToNBTMapArray(statePosArrayList);
        UUID copyUUID = UUID.randomUUID();
        ClientPacketDistributor.sendToServer(new SendPastePayload(copyUUID, serverTag));
    }
}

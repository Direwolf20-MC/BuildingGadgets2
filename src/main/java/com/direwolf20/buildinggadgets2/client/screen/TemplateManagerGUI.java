/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.client.renderer.VBORenderer;
import com.direwolf20.buildinggadgets2.client.screen.widgets.ScrollingMaterialList;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.TemplateItem;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketSendCopyDataToServer;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketUpdateTemplateManager;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.util.FakeRenderingWorld;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.Template;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.direwolf20.buildinggadgets2.client.renderer.VBORenderer.isModelRender;

public class TemplateManagerGUI extends AbstractContainerScreen<TemplateManagerContainer> {
    private static final ResourceLocation background = new ResourceLocation(BuildingGadgets2.MODID, "textures/gui/template_manager.png");

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
    public static UUID copyPasteUUIDCache = UUID.randomUUID(); //A unique ID of the copy/paste, which we'll use to determine if we need to request an update from the server Its initialized as random to avoid having to null check it
    private static ArrayList<StatePos> statePosCache;

    private final TemplateManagerBE be;
    private final TemplateManagerContainer container;

    private ScrollingMaterialList scrollingList;
    private boolean showMaterialList = false;

    private static Map<RenderType, VertexBuffer> vertexBuffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (type) -> new VertexBuffer(VertexBuffer.Usage.STATIC)));

    public TemplateManagerGUI(TemplateManagerContainer container, Inventory playerInventory, Component title) {
        super(container, playerInventory, Component.literal(""));

        this.container = container;
        this.be = container.getTe();

    }

    @Override
    public void init() {
        super.init();
        this.nameField = new EditBox(this.font, (this.leftPos - 20) + 8, topPos - 5, imageWidth - 16, this.font.lineHeight + 3, Component.translatable("buildinggadgets2.screen.namefieldtext"));

        int x = (leftPos - 20) + 180;
        buttonSave = new ExtendedButton(x, topPos + 15, 60, 15, Component.translatable("buildinggadgets2.buttons.save"), (button) -> {
            onSave();
        });
        buttonLoad = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.load"), b -> onLoad()).pos(x, topPos + 32).size(60, 15).build());
        buttonCopy = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.copy"), b -> onCopy()).pos(x, topPos + 50).size(60, 15).build());
        buttonPaste = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.paste"), b -> onPaste()).pos(x, topPos + 67).size(60, 15).build());
        buttonToggleViewport = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.render"), b -> onToggleViewport()).pos(x, topPos + 85).size(60, 15).build());

        this.renderSlot = 1;

        addRenderableWidget(buttonSave);
        this.nameField.setMaxLength(50);
        this.nameField.setVisible(true);
        addRenderableWidget(nameField);

        this.scrollingList = new ScrollingMaterialList(this, leftPos + panel.getX(), (topPos + panel.getY()), panel.getWidth(), panel.getHeight(), container.getSlot(renderSlot).getItem());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        updatePanelIfNeeded();
        if (showMaterialList) {
            if (!renderables.contains(scrollingList))
                this.addRenderableWidget(scrollingList);
            this.setFocused(scrollingList);
        } else {
            this.removeWidget(scrollingList);
            this.renderPanel(guiGraphics);
        }
    }

    public boolean updatePanelIfNeeded() {
        ItemStack gadget = container.getSlot(renderSlot).getItem();
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);
        if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof TemplateItem)) {
            vertexBuffers = null; //Clear vertex buffers when player removes item from the slot we're rendering
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
        vertexBuffers = VBORenderer.generateRender(getMinecraft().level, BlockPos.ZERO, gadget, 1f, statePosCache);
        scrollingList.setTemplateItem(gadget);
        return true; //Need a render update!
    }

    private void renderPanel(GuiGraphics guiGraphics) {
        double scale = getMinecraft().getWindow().getGuiScale();
        ItemStack gadget = container.getSlot(renderSlot).getItem();
        if (gadget.isEmpty()) return;
        ArrayList<StatePos> statePosCache = BG2DataClient.getLookupFromUUID(GadgetNBT.getUUID(gadget));
        if (statePosCache == null || statePosCache.isEmpty()) return;

        BlockPos startPos = statePosCache.get(0).pos;
        BlockPos endPos = statePosCache.get(statePosCache.size() - 1).pos;

        float lengthX = Math.abs(startPos.getX() - endPos.getX());
        float lengthY = Math.abs(startPos.getY() - endPos.getY());
        float lengthZ = Math.abs(startPos.getZ() - endPos.getZ());

        final float maxW = 6 * 16;
        final float maxH = 11 * 16;

        float overW = Math.max(lengthX * 16 - maxW, lengthZ * 16 - maxW);
        float overH = lengthY * 16 - maxH;

        float sc = 1;
        float zoomScale = 1;

        if (overW > 0 && overW >= overH) {
            sc = maxW / (overW + maxW);
            zoomScale = overW / 40;
        } else if (overH > 0 && overH >= overW) {
            sc = maxH / (overH + maxH);
            zoomScale = overH / 40;
        }

        int x1 = (int) Math.round((leftPos + panel.getX()) * scale);
        int y1 = (int) Math.round(getMinecraft().getWindow().getHeight() - (topPos + panel.getY() + panel.getHeight()) * scale);
        int x2 = (int) Math.round(panel.getWidth() * scale);
        int y2 = (int) Math.round(panel.getHeight() * scale);


        RenderSystem.viewport(x1, y1, x2, y2); //The viewport is like a mini world where things get drawn
        RenderSystem.backupProjectionMatrix();

        float fov = 60.0F;  // or whatever field of view you want
        float aspectRatio = (float) width / height;  // width and height of your GUI or the "viewport" you want to use
        float near = 0.1F;
        float far = 1000.0F;

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float) Math.toRadians(fov), aspectRatio, near, far);
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z); //This is needed to switch to 3d rendering instead of 2d for the screen

        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        poseStack.translate(-lengthZ / 2 + panX, -lengthY / 2 - panY, -lengthZ + zoom); //Move the objects in the world being drawn inside the viewport around
        poseStack.mulPose(new Quaternionf().setAngleAxis(rotX / 180 * (float) Math.PI, 1, 0, 0)); //Rotate
        poseStack.mulPose(new Quaternionf().setAngleAxis(rotY / 180 * (float) Math.PI, 0, 1, 0)); //Rotate
        RenderSystem.applyModelViewMatrix();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false); //Clear the depth buffer so it can draw where it is

        drawRenderScreen(poseStack, Minecraft.getInstance().player, statePosCache); //Draw VBO

        poseStack.popPose();

        RenderSystem.applyModelViewMatrix();
        RenderSystem.viewport(0, 0, getMinecraft().getWindow().getWidth(), getMinecraft().getWindow().getHeight());
        RenderSystem.restoreProjectionMatrix();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false); //Clear the depth buffer so it can draw where it is
        RenderSystem.applyModelViewMatrix();
    }

    public static void drawRenderScreen(PoseStack matrix, Player player, ArrayList<StatePos> statePosCache) {
        if (vertexBuffers == null) {
            return;
        }

        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();

        //Draw the renders in the specified order
        ArrayList<RenderType> drawSet = new ArrayList<>();
        drawSet.add(RenderType.solid());
        drawSet.add(RenderType.cutout());
        drawSet.add(RenderType.cutoutMipped());
        drawSet.add(RenderType.translucent());
        drawSet.add(RenderType.tripwire());
        try {
            for (RenderType renderType : drawSet) {
                RenderType drawRenderType;
                if (renderType.equals(RenderType.cutout()))
                    drawRenderType = OurRenderTypes.RenderBlock;
                else
                    drawRenderType = RenderType.translucent();
                VertexBuffer vertexBuffer = vertexBuffers.get(renderType);
                if (vertexBuffer.getFormat() == null)
                    continue; //IDE says this is never null, but if we remove this check we crash because its null so....
                drawRenderType.setupRenderState();
                vertexBuffer.bind();
                vertexBuffer.drawWithShader(matrix.last().pose(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
                VertexBuffer.unbind();
                drawRenderType.clearRenderState();
            }
        } catch (Exception e) {
            System.out.println(e);
        }


        //if (true) return; //Remove this will render Tiles (Like chests) but remove tooltips - can't figure out how to fix tooltips!

        matrix.pushPose();
        matrix.setIdentity();
        MyRenderMethods.MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MyRenderMethods.MultiplyAlphaRenderTypeBuffer(buffersource, 1f);
        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        FakeRenderingWorld fakeRenderingWorld = new FakeRenderingWorld(player.level(), statePosCache, BlockPos.ZERO);
        for (StatePos pos : statePosCache.stream().filter(pos -> !isModelRender(pos.state)).toList()) {
            if (pos.state.isAir()) continue;
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            BlockEntityRenderDispatcher blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            BlockEntity blockEntity = fakeRenderingWorld.getBlockEntity(pos.pos);
            if (blockEntity != null) {
                var renderer = blockEntityRenderer.getRenderer(blockEntity);
                renderer.render(blockEntity, 0, matrix, multiplyAlphaRenderTypeBuffer, 15728640, OverlayTexture.NO_OVERLAY);
                //blockEntityRenderer.render(blockEntity, 0, matrix, buffersource);
            } else
                MyRenderMethods.renderBETransparent(fakeRenderingWorld.getBlockState(pos.pos), matrix, buffersource, 15728640, 655360, 0.5f);
            matrix.popPose();
        }
        matrix.popPose();

        buffersource.endLastBatch(); //Needed to draw the tiles at this point in the render pipeline or whatever - only for screens
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        renderBackground(guiGraphics);

        guiGraphics.blit(background, leftPos - 20, topPos - 12, 0, 0, imageWidth, imageHeight + 25);
        guiGraphics.blit(background, (leftPos - 20) + imageWidth, topPos + 8, imageWidth + 3, 30, 71, imageHeight);

        if (!buttonCopy.isHovered() && !buttonPaste.isHovered()) {
            if (buttonLoad.isHovered())
                guiGraphics.blit(background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth, 0, 17, 24);
            else
                guiGraphics.blit(background, (leftPos + imageWidth) - 44, topPos + 38, imageWidth + 17, 0, 16, 24);
        }

        this.nameField.render(guiGraphics, mouseX, mouseY, partialTicks);
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
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (!showMaterialList) {
            if (panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
                clickButton = mouseButton;
                panelClicked = true;
                clickX = (int) getMinecraft().mouseHandler.xpos();
                clickY = (int) getMinecraft().mouseHandler.ypos();
            }
        }
        if (!panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
            this.scrollingList.setSelected(null);
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        panelClicked = false;
        initRotX = rotX;
        initRotY = rotY;
        initPanX = panX;
        initPanY = panY;
        initZoom = zoom;

        return super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        if (showMaterialList)
            return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(x, y, button, dx, dy) : false;
        return super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 256) {
            this.onClose();
            return true;
        }

        return this.nameField.isFocused() ? this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (panelClicked) {
            if (clickButton == 0) {
                float prevRotX = rotX;
                float prevRotY = rotY;
                rotX = initRotX - ((int) getMinecraft().mouseHandler.ypos() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHandler.xpos() - clickX);
                //momentumX = rotX - prevRotX;
                //momentumY = rotY - prevRotY;
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
            guiGraphics.drawString(font, Component.translatable("buildinggadgets2.screen.templateplaceholder"), nameField.getX() - leftPos + 4, (nameField.getY() + 2) - topPos, -10197916);

        if (buttonSave.isHovered() || buttonLoad.isHovered() || buttonPaste.isHovered())
            drawSlotOverlay(guiGraphics, buttonLoad.isHovered() ? container.getSlot(0) : container.getSlot(1));


    }

    private void drawSlotOverlay(GuiGraphics guiGraphics, Slot slot) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, -1660903937);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta) {
        zoom = initZoom + ((float) scrollDelta * 2);
        if (zoom < -200) zoom = -200;
        if (zoom > 5000) zoom = 5000;

        return super.mouseScrolled(mouseX, mouseY, scrollDelta);
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        nameField.tick();
        if (!panelClicked) {
            initRotX = rotX;
            initRotY = rotY;
            initZoom = zoom;
            initPanX = panX;
            initPanY = panY;
        }
    }

    private void rename(ItemStack stack) {
        if (nameField.getValue().isEmpty())
            return;
        //TODO Implement Naming
    }

    private void onToggleViewport() {
        this.showMaterialList = !this.showMaterialList;
        if (showMaterialList)
            buttonToggleViewport.setMessage(Component.translatable("buildinggadgets2.buttons.materials"));
        else
            buttonToggleViewport.setMessage(Component.translatable("buildinggadgets2.buttons.render"));
    }

    private void onSave() {
        PacketHandler.sendToServer(new PacketUpdateTemplateManager(be.getBlockPos(), 0));
    }

    private void onLoad() {
        PacketHandler.sendToServer(new PacketUpdateTemplateManager(be.getBlockPos(), 1));
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
            CompoundTag deserializedNBT = TagParser.parseTag(template.statePosArrayList);
            statePosArrayList = BG2Data.statePosListFromNBTMapArray(deserializedNBT);
        } catch (Exception e) {
            getMinecraft().player.displayClientMessage(Component.translatable("buildinggadgets2.screen.invalidjson"), true);
            // Handle the exception if the string isn't a valid NBT
            return;
        }
        if (statePosArrayList.isEmpty())
            return;
        CompoundTag serverTag = BG2Data.statePosListToNBTMapArray(statePosArrayList);
        PacketHandler.sendToServer(new PacketSendCopyDataToServer(serverTag));
    }

    //TODO WIP
    private void onReplace(BlockState targetState) {
        if (!showMaterialList || this.scrollingList.getSelected() == null)
            return;

        //doReplace(this.scrollingList.getSelected().getStack(), targetState);
        //System.out.println(this.scrollingList.getSelected().getItemName());
    }

    private void doReplace(BlockState sourceState, BlockState targetState) {
        Template template = getTemplate();
        if (template.statePosArrayList.isEmpty()) return;

        template.replaceBlocks(sourceState, targetState);

    }
}

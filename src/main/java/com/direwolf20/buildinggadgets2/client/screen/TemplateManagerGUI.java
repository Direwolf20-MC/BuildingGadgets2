/**
 * Parts of this class were adapted from code written by TTerrag for the Chisel mod: https://github.com/Chisel-Team/Chisel
 * Chisel is Open Source and distributed under GNU GPL v2
 */

package com.direwolf20.buildinggadgets2.client.screen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.client.renderer.VBORenderer;
import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketRequestCopyData;
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
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.UUID;

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
    private Button buttonSave, buttonLoad, buttonCopy, buttonPaste;

    private final TemplateManagerBE be;
    private final TemplateManagerContainer container;

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
        buttonSave = new ExtendedButton(x, topPos + 17, 60, 20, Component.translatable("buildinggadgets2.buttons.save"), (button) -> {
            onSave();
        });
        buttonLoad = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.load"), b -> onLoad()).pos(x, topPos + 39).size(60, 20).build());
        buttonCopy = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.copy"), b -> onCopy()).pos(x, topPos + 66).size(60, 20).build());
        buttonPaste = addRenderableWidget(Button.builder(Component.translatable("buildinggadgets2.buttons.paste"), b -> onPaste()).pos(x, topPos + 89).size(60, 20).build());

        addRenderableWidget(buttonSave);
        this.nameField.setMaxLength(50);
        this.nameField.setVisible(true);
        addRenderableWidget(nameField);
        updateClientData(0);
        updateClientData(1);
    }

    public void updateClientData(int slot) {
        ItemStack itemStack = container.getSlot(slot).getItem();
        if (itemStack.isEmpty() || !GadgetNBT.hasCopyUUID(itemStack))
            return; //If the gadget hasn't copied anything yet, lets just bail out now!
        UUID itemUUID = GadgetNBT.getUUID(itemStack);
        UUID copyUUID = GadgetNBT.getCopyUUID(itemStack);
        UUID dataClientUUID = BG2DataClient.getCopyUUID(itemUUID);
        if (dataClientUUID != null && dataClientUUID.equals(copyUUID)) //If the Cache'd UUID of the copy/paste matches whats on the item, we don't need to request an update
            return;
        //Request an update for this item from the server, it'll be stored in the BG2DataClient class
        PacketHandler.sendToServer(new PacketRequestCopyData(itemUUID, copyUUID));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
        this.renderPanel(guiGraphics);
//        guiGraphics.drawString(font, "Preview disabled for now...", leftPos - 10, topPos + 40, 0xFFFFFF);
    }

    private void renderPanel(GuiGraphics guiGraphics) {
        double scale = getMinecraft().getWindow().getGuiScale();
        ItemStack gadget = container.getSlot(0).getItem();
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

        //RenderSystem.viewport(x1,y1,x2, y2);
        RenderSystem.backupProjectionMatrix();

        PoseStack poseStack = RenderSystem.getModelViewStack();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        PoseStack newPose = new PoseStack();

        poseStack.pushPose();
        poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);
        poseStack.translate(120, 220, 350);
        poseStack.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
        poseStack.scale((float) 30, (float) 30, (float) 30);
        poseStack.mulPose(new Quaternionf().setAngleAxis(45f / 180 * (float) Math.PI, 1, 0, 0));
        poseStack.mulPose(new Quaternionf().setAngleAxis(90f / 180 * (float) Math.PI, 0, 1, 0));
        //RenderSystem.setProjectionMatrix(poseStack.last().pose(), VertexSorting.ORTHOGRAPHIC_Z);
        poseStack.pushPose();
        poseStack.translate(0 - 0.5, 0 - 0.5, 0 - 0.5);


        FakeRenderingWorld fakeRenderingWorld = new FakeRenderingWorld(Minecraft.getInstance().level, statePosCache, BlockPos.ZERO);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        BlockState renderState = Blocks.OAK_LOG.defaultBlockState();
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(renderState);
        ModelData modelData = model.getModelData(Minecraft.getInstance().level, new BlockPos(0, 1000, 0), renderState, ModelData.EMPTY);
        for (RenderType renderType : model.getRenderTypes(renderState, RandomSource.create(42), modelData))
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(Minecraft.getInstance().level, model, renderState, new BlockPos(0, 100, 0), poseStack, bufferSource.getBuffer(renderType), false, RandomSource.create(42), 42, OverlayTexture.NO_OVERLAY, modelData, renderType);
        bufferSource.endBatch();

        VBORenderer.drawRender2(poseStack, container.getTe().getBlockPos().above(), Minecraft.getInstance().player, container.getSlot(0).getItem());
        poseStack.popPose();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        MyRenderMethods.MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MyRenderMethods.MultiplyAlphaRenderTypeBuffer(bufferSource, 1f);
        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        BlockPos renderPos = container.getTe().getBlockPos().above();
        fakeRenderingWorld = new FakeRenderingWorld(Minecraft.getInstance().player.level(), statePosCache, renderPos);
        for (StatePos pos : statePosCache.stream().filter(pos -> !isModelRender(pos.state)).toList()) {
            if (pos.state.isAir()) continue;
            poseStack.pushPose();
            poseStack.translate(0 - 0.5, 0 - 0.5, 0 - 0.5);
            poseStack.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            poseStack.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
            poseStack.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            //MyRenderMethods.renderBETransparent(mockBuilderWorld.getBlockState(pos.pos), matrix, buffersource, 15728640, 655360, 0.5f);
            BlockEntityRenderDispatcher blockEntityRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            BlockEntity blockEntity = fakeRenderingWorld.getBlockEntity(pos.pos);
            if (blockEntity != null)
                blockEntityRenderer.render(blockEntity, 0, poseStack, multiplyAlphaRenderTypeBuffer);
            else
                MyRenderMethods.renderBETransparent(fakeRenderingWorld.getBlockState(pos.pos), poseStack, bufferSource, 15728640, 655360, 0.5f);
            poseStack.popPose();
        }


        poseStack.popPose();

        getMinecraft().getTextureManager().bindForSetup(TextureAtlas.LOCATION_BLOCKS);
        Tesselator tesselator = Tesselator.getInstance();


        guiGraphics.pose().translate(0, 0, 100);
        /*BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(x1/scale, (topPos + panel.getY() + panel.getHeight()), 0.0D).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x1/scale + x2/scale, (topPos + panel.getY() + panel.getHeight()), 0.0D).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x1/scale + x2/scale, (topPos + panel.getY()), 0.0D).color(255, 255, 255, 255).endVertex();
        buffer.vertex(x1/scale, (topPos + panel.getY()), 0.0D).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(buffer.end());*/

        //RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, true);
        //RenderSystem.setProjectionMatrix(matrix4f,  VertexSorting.ORTHOGRAPHIC_Z);

        /*buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(220, 185, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.vertex(330, 185, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.vertex(330, 105, 0.0D).color(0, 0, 0, 255).endVertex();
        buffer.vertex(220, 105, 0.0D).color(0, 0, 0, 255).endVertex();*/
        //newPose.translate(0, -1, -3);
        // Minecraft.getInstance().gameRenderer.renderLevel(1f, 0, newPose);
        //buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        /*GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        GuiCamera mainCamera = new GuiCamera();

        double d0 = 70;
        poseStack.mulPoseMatrix(gameRenderer.getProjectionMatrix(d0));
        Matrix4f matrix4f = poseStack.last().pose();
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorting.DISTANCE_TO_ORIGIN);
        mainCamera.setup(this.minecraft.level, this.minecraft.player, !this.minecraft.options.getCameraType().isFirstPerson(), this.minecraft.options.getCameraType().isMirrored(), 1f);
        mainCamera.setPosition(0,0,0);
        net.minecraftforge.client.event.ViewportEvent.ComputeCameraAngles cameraSetup = net.minecraftforge.client.ForgeHooksClient.onCameraSetup(gameRenderer, mainCamera, 1f);

        mainCamera.setAnglesInternal(cameraSetup.getYaw(), cameraSetup.getPitch());
        newPose.mulPose(Axis.ZP.rotationDegrees(cameraSetup.getRoll()));

        newPose.mulPose(Axis.XP.rotationDegrees(mainCamera.getXRot()));
        newPose.mulPose(Axis.YP.rotationDegrees(mainCamera.getYRot() + 180.0F));
        Matrix3f matrix3f = (new Matrix3f(newPose.last().normal())).invert();
        RenderSystem.setInverseViewRotationMatrix(matrix3f);

        VBORenderer.drawRender2(new PoseStack(), new BlockPos(0,0,0), Minecraft.getInstance().player, container.getSlot(0).getItem());*/
        //poseStack.pushPose();
        //poseStack.scale(20, 20, 20);
        //poseStack.translate(0, 1, -3);
        //blockRenderDispatcher.getModelRenderer().renderModel(newPose.last(), buffer, renderState, ibakedmodel, f, f1, f2, 0,0,net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        //blockRenderDispatcher.getModelRenderer().tesselateBlock(Minecraft.getInstance().level, ibakedmodel, renderState, new BlockPos(0,1,0), poseStack, buffer, false, RandomSource.create(), renderState.getSeed(new BlockPos(0,0,0)), 0, ModelData.EMPTY, null);
        //BufferUploader.drawWithShader(buffer.end());

        //poseStack.pushPose();
        //poseStack.scale(4, 4, 4);
        //poseStack.translate(0, 1, 3);
        //blockRenderDispatcher.getModelRenderer().renderModel(poseStack.last(), bufferbuilder, renderState, ibakedmodel, f, f1, f2, 0,0,net.minecraftforge.client.model.data.ModelData.EMPTY, null);
        //MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        //VertexConsumer builder = buffersource.getBuffer(OurRenderTypes.MissingBlockOverlay);
        //MyRenderMethods.renderBoxSolid(poseStack.last().pose(), bufferbuilder, new BlockPos(0,0,-3), 1, 0, 0, 1f);
        //tesselator.end();
        RenderSystem.viewport(0, 0, getMinecraft().getWindow().getWidth(), getMinecraft().getWindow().getHeight());
        RenderSystem.restoreProjectionMatrix();
        //poseStack.pushPose();
        //RenderSystem.matrixMode(GL11.GL_PROJECTION);
        //RenderSystem.pushMatrix();
        //RenderSystem.loadIdentity();

        //RenderSystem.multMatrix(Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000));
        //RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        /*RenderSystem.viewport((int) Math.round((leftPos + panel.getX()) * scale),
                (int) Math.round(getMinecraft().getWindow().getHeight() - (topPos + panel.getY() + panel.getHeight()) * scale),
                (int) Math.round(panel.getWidth() * scale),
                (int) Math.round(panel.getHeight() * scale));*/
        /*RenderSystem.viewport(0,0,40,40);
        //RenderSystem.applyModelViewMatrix();
        RenderSystem.backupProjectionMatrix();
        float fovY = (float) Math.toRadians(45);   // Convert 45 degrees to radians.
        float aspectRatio = (float) 40 / 40; // Assuming width and height are the dimensions of your viewport.
        float zNear = 0.1f;
        float zFar = 1000.0f;

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective(fovY, aspectRatio, zNear, zFar);
        RenderSystem.setProjectionMatrix(projectionMatrix, VertexSorting.ORTHOGRAPHIC_Z);
        BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        poseStack.pushPose();
        poseStack.scale(3,3,3);
        blockRenderDispatcher.renderBatched(Blocks.STONE.defaultBlockState(), new BlockPos(0,0,0), Minecraft.getInstance().level, poseStack, Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.solid()), true,
                Minecraft.getInstance().level.random);
        poseStack.popPose();
        RenderSystem.viewport(0, 0, getMinecraft().getWindow().getWidth(), getMinecraft().getWindow().getHeight());
        //RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, true);*/

        sc = (293 * sc) + zoom / zoomScale;
        //RenderSystem.scaled(sc, sc, sc);
        int moveX = startPos.getX() - endPos.getX();

        //RenderSystem.rotatef(30, 0, 1, 0);
        if (startPos.getX() >= endPos.getX())
            moveX--;

        //RenderSystem.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
        //RenderSystem.translated(panX, -panY, 0);
        //RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f) * -1, ((startPos.getY() - endPos.getY()) / 2f) * -1, ((startPos.getZ() - endPos.getZ()) / 2f) * -1);
        //RenderSystem.rotatef(-rotX, 1, 0, 0);
        //RenderSystem.rotatef(rotY, 0, 1, 0);
        //RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f), ((startPos.getY() - endPos.getY()) / 2f), ((startPos.getZ() - endPos.getZ()) / 2f));

        //getMinecraft().getTextureManager().(InventoryMenu.BLOCK_ATLAS);
//        RenderSystem.callList(displayList);

        //RenderSystem.popMatrix();
        //RenderSystem.matrixMode(GL11.GL_PROJECTION);
        //RenderSystem.popMatrix();
        //RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        //poseStack.popPose();

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

    /*private void validateCache(float partialTicks) {
        // Invalidate the render
        if (container.getSlot(0).getItem().isEmpty() && template != null) {
            template = null;
            resetViewport();
            return;
        }

        container.getSlot(0).getItem().getCapability(CapabilityTemplate.TEMPLATE_KEY_CAPABILITY).ifPresent(key -> templateProvider.ifPresent(provider -> {
            // Make sure we're not re-creating the same cache.
            Template template = provider.getTemplateForKey(key);
            if (this.template == template)
                return;

            this.template = template;

//            IBuildView view = template.createViewInContext(
//                    SimpleBuildContext.builder()
//                            .player(getMinecraft().player)
//                            .stack(container.getSlot(0).getStack())
//                            .build(new MockDelegationWorld(getMinecraft().world)));

//            int displayList = GLAllocation.generateDisplayLists(1);
//            GlStateManager.newList(displayList, GL11.GL_COMPILE);

//            renderStructure(view, partialTicks);

//            GlStateManager.endList();
//            this.displayList = displayList;
        }));
    }

    private void renderStructure(IBuildView view, float partialTicks) {
        Random rand = new Random();
        BlockRenderDispatcher dispatcher = getMinecraft().getBlockRenderer();

        BufferBuilder bufferBuilder = new BufferBuilder(2097152);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

        for (PlacementTarget target : view) {
            target.placeIn(view.getContext());
            BlockPos targetPos = target.getPos();
            BlockState renderBlockState = view.getContext().getWorld().getBlockState(targetPos);
            BlockEntity te = view.getContext().getWorld().getBlockEntity(targetPos);

            if (renderBlockState.getRenderShape() == RenderShape.MODEL) {
                BakedModel model = dispatcher.getBlockModel(renderBlockState);
//                dispatcher.getBlockModelRenderer().renderModelFlat()
//                        .renderModelFlat(getWorld(), model, renderBlockState, target.getPos(), bufferBuilder, false,
//                        rand, 0L, te != null ? te.getModelData() : EmptyModelData.INSTANCE);
            }

            if (te != null) {
                try {
                    BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(te);
                    if (renderer != null) {
//                        if (te.hasFastRenderer())
//                            renderer.renderTileEntityFast(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1, bufferBuilder);
//                        else
//                            renderer.render(te, targetPos.getX(), targetPos.getY(), targetPos.getZ(), partialTicks, - 1);
                    }
                    //remember vanilla Tiles rebinding the TextureAtlas
                    RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
                } catch (Exception e) {
                    BuildingGadgets.LOG.error("Error rendering TileEntity", e);
                }
            }
        }

        bufferBuilder.end();

//        if (bufferBuilder.getVertexCount() > 0) {
//            VertexFormat vertexformat = bufferBuilder.getVertexFormat();
//            int i = vertexformat.getSize();
//            ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
//            List<VertexFormatElement> list = vertexformat.getElements();
//
//            for (int j = 0; j < list.size(); ++ j) {
//                VertexFormatElement vertexformatelement = list.get(j);
//                bytebuffer.position(vertexformat.getOffset(j));
//                vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
//            }
//
//            GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
//            int i1 = 0;
//
//            for (int j1 = list.size(); i1 < j1; ++ i1) {
//                VertexFormatElement vertexformatelement1 = list.get(i1);
//                vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
//            }
//        }
    }

    private void renderRequirement(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        MaterialList requirements = this.template.getHeaderAndForceMaterials(BuildContext.builder().build(getWorld())).getRequiredItems();
        if (requirements == null)
            return;

        Lighting.setupForFlatItems();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos - 30, topPos - 5, 200);
        guiGraphics.pose().scale(.8f, .8f, .8f);

        String title = "Requirements"; // Todo lang;
        guiGraphics.drawString(getMinecraft().font, title, 5 - (font.width(title)), 0, Color.WHITE.getRGB());

        // The things you have to do to get anything from this system is just stupid.
        MatchResult list = InventoryHelper.CREATIVE_INDEX.tryMatch(requirements);
        ImmutableMultiset<IUniqueObject<?>> foundItems = list.getFoundItems();

        // Reverse sorted list of items required.
        List<Multiset.Entry<IUniqueObject<?>>> sortedEntries = ImmutableList.sortedCopyOf(Comparator
                .<Multiset.Entry<IUniqueObject<?>>, Integer>comparing(Multiset.Entry::getCount)
                .reversed(), list.getChosenOption().entrySet());

        int index = 0, column = 0;
        for (Multiset.Entry<IUniqueObject<?>> e : sortedEntries) {
            ItemStack stack = e.getElement().createStack();
            int x = (-20 - (column * 25)), y = (20 + (index * 25));

            guiGraphics.renderItem(stack, x + 4, y + 4);
            guiGraphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + 4, y + 4, GadgetUtils.withSuffix(foundItems.count(e.getElement())));

            int space = (int) (25 - (.2f * 25));
            int zoneX = ((leftPos - 32) + (-15 - (column * space))), zoneY = (topPos - 9) + (20 + (index * space));

            if (mouseX > zoneX && mouseX < (zoneX + space) && mouseY > zoneY && mouseY < (zoneY + space)) {
                guiGraphics.renderTooltip(font, Lists.transform(stack.getTooltipLines(this.getMinecraft().player, TooltipFlag.Default.NORMAL), Component::getVisualOrderText), x + 15, y + 25);
            }

            index++;
            if (index % 8 == 0) {
                column++;
                index = 0;
            }
        }

        Lighting.setupFor3DItems();
        guiGraphics.pose().popPose();
    }

    private void renderPanel() {
//        double scale = getMinecraft().getWindow().getGuiScale();
//
//        BlockPos startPos = template.getHeader().getBoundingBox().getMin();
//        BlockPos endPos = template.getHeader().getBoundingBox().getMax();
//
//        double lengthX = Math.abs(startPos.getX() - endPos.getX());
//        double lengthY = Math.abs(startPos.getY() - endPos.getY());
//        double lengthZ = Math.abs(startPos.getZ() - endPos.getZ());
//
//        final double maxW = 6 * 16;
//        final double maxH = 11 * 16;
//
//        double overW = Math.max(lengthX * 16 - maxW, lengthZ * 16 - maxW);
//        double overH = lengthY * 16 - maxH;
//
//        double sc = 1;
//        double zoomScale = 1;
//
//        if (overW > 0 && overW >= overH) {
//            sc = maxW / (overW + maxW);
//            zoomScale = overW / 40;
//        } else if (overH > 0 && overH >= overW) {
//            sc = maxH / (overH + maxH);
//            zoomScale = overH / 40;
//        }
//
//        RenderSystem.pushMatrix();
//        RenderSystem.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.pushMatrix();
//        RenderSystem.loadIdentity();
//
//        RenderSystem.multMatrix(Matrix4f.perspective(60, (float) panel.getWidth() / panel.getHeight(), 0.01F, 4000));
//        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//        RenderSystem.viewport((int) Math.round((leftPos + panel.getX()) * scale),
//                (int) Math.round(getMinecraft().getWindow().getHeight() - (topPos + panel.getY() + panel.getHeight()) * scale),
//                (int) Math.round(panel.getWidth() * scale),
//                (int) Math.round(panel.getHeight() * scale));
//
//        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, true);
//
//        sc = (293 * sc) + zoom / zoomScale;
//        RenderSystem.scaled(sc, sc, sc);
//        int moveX = startPos.getX() - endPos.getX();
//
//        RenderSystem.rotatef(30, 0, 1, 0);
//        if (startPos.getX() >= endPos.getX())
//            moveX--;
//
//        RenderSystem.translated((moveX) / 1.75, -Math.abs(startPos.getY() - endPos.getY()) / 1.75, 0);
//        RenderSystem.translated(panX, -panY, 0);
//        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f) * -1, ((startPos.getY() - endPos.getY()) / 2f) * -1, ((startPos.getZ() - endPos.getZ()) / 2f) * -1);
//        RenderSystem.rotatef(-rotX, 1, 0, 0);
//        RenderSystem.rotatef(rotY, 0, 1, 0);
//        RenderSystem.translated(((startPos.getX() - endPos.getX()) / 2f), ((startPos.getY() - endPos.getY()) / 2f), ((startPos.getZ() - endPos.getZ()) / 2f));
//
//        getMinecraft().getTextureManager().bind(InventoryMenu.BLOCK_ATLAS);
//
////        RenderSystem.callList(displayList);
//
//        RenderSystem.popMatrix();
//        RenderSystem.matrixMode(GL11.GL_PROJECTION);
//        RenderSystem.popMatrix();
//        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
//        RenderSystem.viewport(0, 0, getMinecraft().getWindow().getWidth(), getMinecraft().getWindow().getHeight());
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
        if (panel.contains((int) mouseX - leftPos, (int) mouseY - topPos)) {
            clickButton = mouseButton;
            panelClicked = true;
            clickX = (int) getMinecraft().mouseHandler.xpos();
            clickY = (int) getMinecraft().mouseHandler.ypos();
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
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (p_keyPressed_1_ == 256) {
            this.onClose();
            return true;
        }

        return this.nameField.isFocused() ? this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
    }*/

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (panelClicked) {
            if (clickButton == 0) {
                float prevRotX = rotX;
                float prevRotY = rotY;
                rotX = initRotX - ((int) getMinecraft().mouseHandler.ypos() - clickY);
                rotY = initRotY + ((int) getMinecraft().mouseHandler.xpos() - clickX);
                momentumX = rotX - prevRotX;
                momentumY = rotY - prevRotY;
            } else if (clickButton == 1) {
                panX = initPanX + ((int) getMinecraft().mouseHandler.xpos() - clickX) / 8f;
                panY = initPanY + ((int) getMinecraft().mouseHandler.ypos() - clickY) / 8f;
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
        zoom = initZoom + ((float) scrollDelta * 20);
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

//    @Override
//    public void tick() {
//        super.tick();
//        nameField.tick();
//        if (! panelClicked) {
//            initRotX = rotX;
//            initRotY = rotY;
//            initZoom = zoom;
//            initPanX = panX;
//            initPanY = panY;
//        }
//    }

    private void rename(ItemStack stack) {
        if (nameField.getValue().isEmpty())
            return;
        //TODO Implement Naming
    }

    private void onSave() {
        PacketHandler.sendToServer(new PacketUpdateTemplateManager(be.getBlockPos(), 0));
    }

    private void onLoad() {
        PacketHandler.sendToServer(new PacketUpdateTemplateManager(be.getBlockPos(), 1));
    }

    private void onCopy() {
        ItemStack templateStack = container.getSlot(1).getItem();
        if (templateStack.isEmpty()) return; //Todo messaging
        UUID templateUUID = GadgetNBT.getUUID(templateStack);
        ArrayList<StatePos> statePosCache = BG2DataClient.getLookupFromUUID(templateUUID);
        if (statePosCache == null || statePosCache.isEmpty()) return;
        Template template = new Template(nameField.getValue(), statePosCache);
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
            if (template.statePosArrayList.equals("")) return;
            CompoundTag deserializedNBT = TagParser.parseTag(template.statePosArrayList);
            statePosArrayList = BG2Data.statePosListFromNBTMapArray(deserializedNBT);
        } catch (CommandSyntaxException e) {
            // Handle the exception if the string isn't a valid NBT
            return;
        }
        if (statePosArrayList.isEmpty())
            return;
        //Todo JSON validations, Older JSON Versions
        CompoundTag serverTag = BG2Data.statePosListToNBTMapArray(statePosArrayList);
        PacketHandler.sendToServer(new PacketSendCopyDataToServer(serverTag));
    }

    public class GuiCamera extends Camera {
        public GuiCamera() {
        }

        @Override
        public void setPosition(double p_90585_, double p_90586_, double p_90587_) {
            super.setPosition(p_90585_, p_90586_, p_90587_);
        }
    }
}

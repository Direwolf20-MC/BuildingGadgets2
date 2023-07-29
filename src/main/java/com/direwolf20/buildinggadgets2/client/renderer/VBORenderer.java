package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.common.items.GadgetBuilding;
import com.direwolf20.buildinggadgets2.common.items.GadgetCopyPaste;
import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
import com.direwolf20.buildinggadgets2.common.items.GadgetExchanger;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketRequestCopyData;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VBORenderer {
    private static ArrayList<StatePos> statePosCache;
    private static int sortCounter = 0;
    public static UUID gadgetUUIDCache = UUID.randomUUID(); //The Unique ID of the gadget who's data we're caching. If this differs, it means the player swapped to another gadget
    public static UUID copyPasteUUIDCache = UUID.randomUUID(); //A unique ID of the copy/paste, which we'll use to determine if we need to request an update from the server Its initialized as random to avoid having to null check it
    public static boolean awaitingUpdate = false;
    public static int updateTimer = 0;

    //Cached SortStates used for re-sorting every so often
    private static final Map<RenderType, BufferBuilder.SortState> sortStates = new HashMap<>();
    //A map of RenderType -> DireBufferBuilder, so we can draw the different render types in proper order later
    private static final Map<RenderType, DireBufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (type) -> new DireBufferBuilder(type.bufferSize())));
    //A map of RenderType -> Vertex Buffer to buffer the different render types.
    private static final Map<RenderType, VertexBuffer> vertexBuffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (type) -> new VertexBuffer(VertexBuffer.Usage.STATIC)));

    //Get the buffer from the map, and ensure its building
    public static DireBufferBuilder getBuffer(RenderType renderType) {
        final DireBufferBuilder buffer = builders.get(renderType);
        if (!buffer.building()) {
            buffer.begin(renderType.mode(), renderType.format());
        }
        return buffer;
    }

    //Start rendering - this is the most expensive part, so we render it, then cache it, and draw it over and over (much cheaper)
    public static void buildRender(RenderLevelStageEvent evt, Player player, ItemStack gadget) {
        ArrayList<StatePos> buildList;
        BaseMode mode = GadgetNBT.getMode(gadget);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        BlockPos anchorPos = GadgetNBT.getAnchorPos(gadget);
        BlockPos renderPos = anchorPos.equals(GadgetNBT.nullPos) ? lookingAt.getBlockPos() : anchorPos;

        if (gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) {
            if (player.level().getBlockState(renderPos).isAir())
                return;
            BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
            if (renderBlockState.isAir()) return;
            buildList = mode.collect(lookingAt.getDirection(), player, renderPos, renderBlockState);

            if (buildList.equals(statePosCache))
                return;

            statePosCache = buildList;
            copyPasteUUIDCache = UUID.randomUUID(); //In case theres an existing copy/Paste render saved, nullify it
        } else if (gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste) {
            renderPos = renderPos.above();
            renderPos.offset(GadgetNBT.getRelativePaste(gadget));
            if (mode.getId().getPath().equals("copy") || mode.getId().getPath().equals("cut")) {
                awaitingUpdate = false;
                drawCopyBox(evt.getPoseStack(), gadget, mode.getId().getPath());
                return;
            } else { //Paste Mode
                UUID gadgetUUID = GadgetNBT.getUUID(gadget);
                if (!gadgetUUIDCache.equals(gadgetUUID)) { //If the player swapped to another gadget, lets refresh the update request.
                    awaitingUpdate = false;
                    gadgetUUIDCache = gadgetUUID;
                }
                if (!GadgetNBT.hasCopyUUID(gadget))
                    return; //If the gadget hasn't copied anything yet, lets just bail out now!
                UUID copyUUID = GadgetNBT.getCopyUUID(gadget);
                if (copyPasteUUIDCache.equals(copyUUID)) //If the Cache'd UUID of the copy/paste matches whats on the item, we don't need to rebuild the render
                    return; //No need to rebuild cache because its up to date!
                UUID dataClientUUID = BG2DataClient.getCopyUUID(gadgetUUID);
                if (dataClientUUID != null && dataClientUUID.equals(copyUUID)) { //If whats stored in BG2DataClient for this gadget matches whats on the tool, its up to date and this class isn't
                    copyPasteUUIDCache = dataClientUUID;
                    statePosCache = BG2DataClient.getLookupFromUUID(gadgetUUID);
                    awaitingUpdate = false;
                    updateTimer = 0;
                    //Don't Return because we want to now draw the Copy/Paste
                } else { //Neither this classes data NOR the BG2Client class's data is up to date - request it from server
                    if (awaitingUpdate && updateTimer < 100) { //If we already requested an update from the server, don't try again for a few seconds
                        updateTimer++;
                        return;
                    }
                    PacketHandler.sendToServer(new PacketRequestCopyData(GadgetNBT.getUUID(gadget)));
                    awaitingUpdate = true;
                    updateTimer = 0;
                    return;
                }
            }
        } else {
            return;
        }

        //Start drawing the Render and cache it, used for both Building and Copy/Paste
        Level level = player.level();
        PoseStack matrix = new PoseStack(); //Create a new matrix stack for use in the buffer building process
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelBlockRenderer = dispatcher.getModelRenderer();
        final RandomSource random = RandomSource.create();
        for (StatePos pos : statePosCache.stream().filter(pos -> isModelRender(pos.state) || !pos.state.getFluidState().isEmpty()).toList()) {
            if (pos.state.isAir()) continue;
            BakedModel ibakedmodel = dispatcher.getBlockModel(pos.state);
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            if (mode.isExchanging) {
                matrix.translate(-0.0005f, -0.0005f, -0.0005f); //For Exchanger
                matrix.scale(1.001f, 1.001f, 1.001f); //For Exchanger
            }
            for (RenderType renderType : ibakedmodel.getRenderTypes(pos.state, random, ModelData.EMPTY)) {
                //Flowers render weirdly so we use a custom renderer to make them look better. Glass and Flowers are both cutouts, so we only want this for non-cube blocks
                if (renderType.equals(RenderType.cutout()) && pos.state.getShape(level, pos.pos.offset(renderPos)).equals(Shapes.block()))
                    renderType = RenderType.translucent();
                DireVertexConsumer direVertexConsumer = new DireVertexConsumer(getBuffer(renderType), 0.5f);
                //Use tesselateBlock to skip the block.isModel check - this helps render Create blocks that are both models AND animated
                if (pos.state.getFluidState().isEmpty())
                    modelBlockRenderer.tesselateBlock(level, ibakedmodel, pos.state, pos.pos.offset(renderPos).above(255), matrix, direVertexConsumer, false, random, pos.state.getSeed(pos.pos.offset(renderPos)), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType);
                else
                    dispatcher.renderLiquid(pos.pos, player.level(), direVertexConsumer, pos.state, pos.state.getFluidState());
                //dispatcher.renderBatched(pos.state, pos.pos.offset(lookingAt.getBlockPos()), level, matrix, direVertexConsumer, true, RandomSource.create(), ModelData.EMPTY, renderType);

            }
            matrix.popPose();
        }
        //Sort all the builder's vertices and then upload them to the vertex buffers
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 subtracted = projectedView.subtract(renderPos.getX(), renderPos.getY(), renderPos.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);
        for (Map.Entry<RenderType, DireBufferBuilder> entry : builders.entrySet()) {
            RenderType renderType = entry.getKey();
            DireBufferBuilder direBufferBuilder = getBuffer(renderType);
            direBufferBuilder.setQuadSorting(VertexSorting.byDistance(sortPos));
            sortStates.put(renderType, direBufferBuilder.getSortState());
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            vertexBuffer.bind();
            vertexBuffer.upload(direBufferBuilder.end());
            VertexBuffer.unbind();
            vertexBuffers.put(renderType, vertexBuffer);
        }
    }

    public static void drawCopyBox(PoseStack matrix, ItemStack gadget, String mode) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        BlockPos start = GadgetNBT.getCopyStartPos(gadget);
        BlockPos end = GadgetNBT.getCopyEndPos(gadget);
        Color color = mode.equals("copy") ? Color.GREEN : Color.RED;
        MyRenderMethods.renderCopy(matrix, start, end, color);
        matrix.popPose();
    }

    public static boolean isModelRender(BlockState state) {
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel ibakedmodel = dispatcher.getBlockModel(state);
        for (Direction direction : Direction.values()) {
            if (!ibakedmodel.getQuads(state, direction, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                return true;
            }
            if (!ibakedmodel.getQuads(state, null, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    //Draw what we've cached
    public static void drawRender(RenderLevelStageEvent evt, Player player, ItemStack gadget) {
        if (vertexBuffers == null || statePosCache == null) {
            return;
        }
        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        BlockPos anchorPos = GadgetNBT.getAnchorPos(gadget);
        BlockPos renderPos = anchorPos.equals(GadgetNBT.nullPos) ? lookingAt.getBlockPos() : anchorPos;
        BlockState lookingAtState = player.level().getBlockState(renderPos);

        if ((lookingAtState.isAir() && anchorPos.equals(GadgetNBT.nullPos)) || lookingAtState.getBlock().equals(Registration.RenderBlock.get()))
            return;
        ArrayList<StatePos> buildList = new ArrayList<>();
        var mode = GadgetNBT.getMode(gadget);
        if (gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) {
            BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
            if (renderBlockState.isAir()) return;
            buildList = mode.collect(lookingAt.getDirection(), player, renderPos, renderBlockState);

            if (buildList.isEmpty()) return;
        } else if (gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste) {
            if (mode.getId().getPath().equals("copy") || mode.getId().getPath().equals("cut")) {
                return; //This is handlded above
            }
            if (!GadgetNBT.hasCopyUUID(gadget) || !copyPasteUUIDCache.equals(GadgetNBT.getCopyUUID(gadget)))
                return;
            renderPos = renderPos.above().offset(GadgetNBT.getRelativePaste(gadget));
        }
        //Sort every <X> Frames to prevent screendoor effect-- TODO Different sort times for different gadgets, scale with number of blocks? Or maybe when view rotation changes enough?
        if (sortCounter > 20) {
            sortAll(renderPos);
            sortCounter = 0;
        } else {
            sortCounter++;
        }

        PoseStack matrix = evt.getPoseStack();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        matrix.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
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
                vertexBuffer.drawWithShader(matrix.last().pose(), new Matrix4f(evt.getProjectionMatrix()), RenderSystem.getShader());
                VertexBuffer.unbind();
                drawRenderType.clearRenderState();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        matrix.popPose();

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        for (StatePos pos : statePosCache.stream().filter(pos -> !isModelRender(pos.state)).toList()) {
            matrix.pushPose();
            matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            matrix.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            MyRenderMethods.renderBETransparent(pos.state, matrix, buffersource, 15728640, 655360, 0.5f);
            matrix.popPose();
        }

        //Fluid Rendering
        /*for (StatePos pos : statePosCache.stream().filter(pos -> (!pos.state.getFluidState().isEmpty())).toList()) {
            matrix.pushPose();
            matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            matrix.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
            FluidState fluidstate = pos.state.getFluidState();
            RenderType rendertype = ItemBlockRenderTypes.getRenderLayer(fluidstate);
            DireVertexConsumer direVertexConsumer = new DireVertexConsumer(buffersource.getBuffer(rendertype), 0.5f);
            dispatcher.renderLiquid(pos.pos, player.level(), direVertexConsumer, pos.state, fluidstate);
            buffersource.endBatch(rendertype);
            matrix.popPose();
        }*/

        //Red Overlay for missing Items
        if ((gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) && !player.isCreative()) {
            BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
            ItemStack findStack = GadgetUtils.getItemForBlock(renderBlockState);
            int availableItems = BuildingUtils.countItemStacks(player.getInventory(), findStack);
            int energyStored = BuildingUtils.getEnergyStored(gadget);
            int energyCost = BuildingUtils.getEnergyCost(gadget);
            for (StatePos statePos : buildList) {
                if (availableItems <= 0 || energyStored < energyCost) {
                    matrix.pushPose();
                    matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
                    matrix.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
                    VertexConsumer builder = buffersource.getBuffer(OurRenderTypes.MissingBlockOverlay);
                    MyRenderMethods.renderBoxSolid(evt.getPoseStack().last().pose(), builder, statePos.pos, 1, 0, 0, 0.35f);
                    matrix.popPose();
                }
                availableItems--;
                energyStored -= energyCost;
            }
        }
    }

    //Sort all the RenderTypes
    public static void sortAll(BlockPos lookingAt) {
        for (Map.Entry<RenderType, BufferBuilder.SortState> entry : sortStates.entrySet()) {
            RenderType renderType = entry.getKey();
            BufferBuilder.RenderedBuffer renderedBuffer = sort(lookingAt, renderType);
            VertexBuffer vertexBuffer = vertexBuffers.get(renderType);
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
        }
    }

    //Sort the render type we pass in - using DireBufferBuilder because we want to sort in the opposite direction from normal
    public static BufferBuilder.RenderedBuffer sort(BlockPos lookingAt, RenderType renderType) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 subtracted = projectedView.subtract(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);
        DireBufferBuilder bufferBuilder = getBuffer(renderType);
        BufferBuilder.SortState sortState = sortStates.get(renderType);
        bufferBuilder.restoreSortState(sortState);
        bufferBuilder.setQuadSorting(VertexSorting.byDistance(sortPos));
        sortStates.put(renderType, bufferBuilder.getSortState());
        return bufferBuilder.end();
    }
}

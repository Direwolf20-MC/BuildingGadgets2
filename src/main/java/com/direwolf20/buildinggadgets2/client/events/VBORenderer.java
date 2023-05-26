package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.client.renderer.DireBufferBuilder;
import com.direwolf20.buildinggadgets2.client.renderer.DireVertexConsumer;
import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VBORenderer {
    private static ArrayList<StatePos> statePosCache;
    private static int sortCounter = 0;

    //Cached SortStates used for re-sorting every so often
    private static final Map<RenderType, BufferBuilder.SortState> sortStates = new HashMap<>();
    //A map of RenderType -> DireBufferBuilder, so we can draw the different render types in proper order later
    private static final Map<RenderType, DireBufferBuilder> builders = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (type) -> new DireBufferBuilder(type.bufferSize())));
    //A map of RenderType -> Vertex Buffer to buffer the different render types.
    private static final Map<RenderType, VertexBuffer> vertexBuffers = RenderType.chunkBufferLayers().stream().collect(Collectors.toMap((renderType) -> renderType, (type) -> new VertexBuffer()));

    //Get the buffer from the map, and ensure its building
    public static DireBufferBuilder getBuffer(RenderType renderType) {
        final DireBufferBuilder buffer = builders.get(renderType);
        if (!buffer.building()) {
            buffer.begin(renderType.mode(), renderType.format());
        }
        return buffer;
    }

    //Start rendering - this is the most expensive part, so we render it, then cache it, and draw it over and over (much cheaper)
    public static void buildRender(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(heldItem);
        if (renderBlockState.isAir()) return;

        BaseMode mode = GadgetNBT.getMode(heldItem);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        ArrayList<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAt.getBlockPos(), renderBlockState);

        //Extra blocks for testing FPS - will remove eventually (Or not cause its me)
        /*for (int k = -15; k < 15; k++) {
            for (int j = 1; j < 25; j++) {
                for (int i = -1; i < 25; i++) {
                    buildList.add(new StatePos(Blocks.GLASS.defaultBlockState(), new BlockPos(k, i, -j)));
                    //buildList.add(new StatePos(Blocks.OAK_LOG.defaultBlockState(), new BlockPos(2, 0, i)));
                    buildList.add(new StatePos(Blocks.COBBLESTONE.defaultBlockState(), new BlockPos(k, i, j)));
                }
            }
        }*/
        //buildList.add(new StatePos(Blocks.GLASS.defaultBlockState(), new BlockPos(1,0,1)));
        //buildList.add(new StatePos(Blocks.COBBLESTONE.defaultBlockState(), new BlockPos(1,0,-1)));

        if (buildList.equals(statePosCache))
            return;

        //System.out.println("I'm Building!");
        //Long drawStart = System.nanoTime();
        Level level = player.level;
        //player.displayClientMessage(Component.literal("Rebuilding Render due to change." + level.getGameTime()), false);
        statePosCache = buildList;
        PoseStack matrix = new PoseStack(); //Create a new matrix stack for use in the buffer building process
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        ModelBlockRenderer modelBlockRenderer = dispatcher.getModelRenderer();
        final RandomSource random = RandomSource.create();
        for (StatePos pos : buildList.stream().filter(pos -> pos.isModelRender).toList()) {
            //matrix.translate(-0.005f, -0.005f, -0.005f); //For Exchanger
            //matrix.scale(1.01f, 1.01f, 1.01f); //For Exchanger
            BakedModel ibakedmodel = dispatcher.getBlockModel(pos.state);
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            for (RenderType renderType : ibakedmodel.getRenderTypes(pos.state, random, ModelData.EMPTY)) {
                //Flowers render weirdly so we use a custom renderer to make them look better. Glass and Flowers are both cutouts, so we only want this for non-cube blocks
                if (renderType.equals(RenderType.cutout()) && pos.state.getShape(level, pos.pos.offset(lookingAt.getBlockPos())).equals(Shapes.block()))
                    renderType = RenderType.translucent();
                DireVertexConsumer direVertexConsumer = new DireVertexConsumer(getBuffer(renderType), 0.5f);
                //Use tesselateBlock to skip the block.isModel check - this helps render Create blocks that are both models AND animated
                modelBlockRenderer.tesselateBlock(level, ibakedmodel, pos.state, pos.pos.offset(lookingAt.getBlockPos()), matrix, direVertexConsumer, true, random, pos.state.getSeed(pos.pos.offset(lookingAt.getBlockPos())), OverlayTexture.NO_OVERLAY, ModelData.EMPTY, renderType, true);
                //dispatcher.renderBatched(pos.state, pos.pos.offset(lookingAt.getBlockPos()), level, matrix, direVertexConsumer, true, RandomSource.create(), ModelData.EMPTY, renderType);

            }
            matrix.popPose();
        }
        //Sort all the builder's vertices and then upload them to the vertex buffers
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector3f sortPos = new Vector3f(projectedView.subtract(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ()));
        for (Map.Entry<RenderType, DireBufferBuilder> entry : builders.entrySet()) {
            RenderType renderType = entry.getKey();
            DireBufferBuilder direBufferBuilder = getBuffer(renderType);
            direBufferBuilder.setQuadSortOrigin(sortPos.x(), sortPos.y(), sortPos.z());
            sortStates.put(renderType, direBufferBuilder.getSortState());
            VertexBuffer vertexBuffer = new VertexBuffer();
            vertexBuffer.bind();
            vertexBuffer.upload(direBufferBuilder.end());
            VertexBuffer.unbind();
            vertexBuffers.put(renderType, vertexBuffer);
        }
    }

    //Draw what we've cached
    public static void drawRender(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {
        if (vertexBuffers == null) {
            return;
        }
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(heldItem);
        if (renderBlockState.isAir()) return;

        // TODO: This might need caching (and invalidating when the mode changes)
        var mode = GadgetNBT.getMode(heldItem);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        List<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAt.getBlockPos(), renderBlockState);
        if (buildList.isEmpty()) return;

        //Sort every <X> Frames to prevent screendoor effect-- TODO Different sort times for different gadgets, scale with number of blocks? Or maybe when view rotation changes enough?
        if (sortCounter > 360) {
            //NumberFormat numberFormat = NumberFormat.getInstance();
            //long sortStart = System.nanoTime();
            sortAll(lookingAt.getBlockPos());
            //long sortEnd = System.nanoTime();
            //System.out.println("Sorting took: " + numberFormat.format(sortEnd - sortStart));
            sortCounter = 0;
        } else {
            sortCounter++;
        }

        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        PoseStack matrix = evt.getPoseStack();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        matrix.translate(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ());
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

        //If any of the blocks in the render didn't have a model (like chests) we draw them here. This renders AND draws them, so more expensive than caching, but I don't think we have a choice
        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (StatePos pos : buildList.stream().filter(pos -> !pos.isModelRender).toList()) {
            matrix.pushPose();
            matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            matrix.translate(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ());
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            MyRenderMethods.renderBETransparent(renderBlockState, matrix, buffersource, 15728640, 655360, 0.5f);
            matrix.popPose();
        }
    }

    //Sort all the RenderTypes
    public static void sortAll(BlockPos lookingAt) {
        for (Map.Entry<RenderType, BufferBuilder.SortState> entry : sortStates.entrySet()) {
            RenderType renderType = entry.getKey();
            BufferBuilder.RenderedBuffer renderedBuffer = sort(lookingAt, renderType);
            VertexBuffer vertexBuffer = new VertexBuffer();
            vertexBuffer.bind();
            vertexBuffer.upload(renderedBuffer);
            VertexBuffer.unbind();
        }
    }

    //Sort the render type we pass in - using DireBufferBuilder because we want to sort in the opposite direction from normal
    public static BufferBuilder.RenderedBuffer sort(BlockPos lookingAt, RenderType renderType) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vector3f sortPos = new Vector3f(projectedView.subtract(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ()));
        DireBufferBuilder bufferBuilder = getBuffer(renderType);
        BufferBuilder.SortState sortState = sortStates.get(renderType);
        bufferBuilder.restoreSortState(sortState);
        bufferBuilder.setQuadSortOrigin(sortPos.x(), sortPos.y(), sortPos.z());
        sortStates.put(renderType, bufferBuilder.getSortState());
        return bufferBuilder.end();
    }
}

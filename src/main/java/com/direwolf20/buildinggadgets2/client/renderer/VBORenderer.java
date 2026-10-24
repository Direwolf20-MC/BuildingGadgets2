package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.common.items.*;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2DataClient;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.*;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * Retained-GPU-mesh renderer for BG2 ghost previews.
 * <p>
 * On statePos change (cache miss): tesselate the entire preview into per-layer {@link BufferBuilder}s,
 * build each one into a {@link MeshData}, upload its vertex bytes into a persistent
 * {@link GpuBuffer} (USAGE_VERTEX | USAGE_COPY_DST), keep the sort state for translucent layers.
 * <p>
 * Every frame: for each layer, run the same body as {@link RenderType#draw(MeshData)} but with our
 * pre-uploaded {@link GpuBuffer} as the vertex source — no re-upload, no re-tesselation.
 * <p>
 * Sort-every-N-frames: rebuild the sorted index buffer from the cached {@link MeshData.SortState}
 * centroids, upload the new indices into a persistent {@link GpuBuffer}. No vertex work.
 */
public class VBORenderer {
    private static ArrayList<StatePos> statePosCache;
    private static int sortCounter = 0;
    public static UUID copyPasteUUIDCache = UUID.randomUUID();

    private static FakeRenderingWorld fakeRenderingWorld;
    private static FakeWorldTintAdapter fakeRenderingWorldTint;
    private static boolean isLargeRender = false;

    // The 3 chunk section layers BG2 cares about.
    private static final ChunkSectionLayer[] LAYERS = ChunkSectionLayer.values();

    // Shared non-AO block renderer, constructed once (Minecraft no longer holds one for us).
    private static ModelBlockRenderer modelBlockRenderer;

    // Per-layer retained GPU vertex buffers. One entry per layer that has something to draw.
    private static final Map<ChunkSectionLayer, LayerCache> layerCaches = new EnumMap<>(ChunkSectionLayer.class);

    // Scratch CPU ring for sorted-index rebuilds. Its Results are consumed by writeToBuffer each time.
    private static final ByteBufferBuilder sortIndexScratch = new ByteBufferBuilder(262144);

    /**
     * One retained GPU mesh for a single {@link ChunkSectionLayer}. Holds the persistent vertex buffer,
     * an optional persistent sorted-index buffer (translucent only), and the sort centroids for re-sorts.
     */
    private static final class LayerCache implements AutoCloseable {
        GpuBuffer vertexBuffer;      // persistent; lives across frames.
        int indexCount;              // drawIndexed(0, 0, indexCount, 1).
        VertexFormat.IndexType autoIndexType;  // for the non-sorted path (from sharedSequentialQuad).

        GpuBuffer sortedIndexBuffer; // persistent; translucent only.
        MeshData.SortState sortState;// centroids for re-sorting.

        @Override
        public void close() {
            if (vertexBuffer != null) {
                vertexBuffer.close();
                vertexBuffer = null;
            }
            if (sortedIndexBuffer != null) {
                sortedIndexBuffer.close();
                sortedIndexBuffer = null;
            }
            sortState = null;
            indexCount = 0;
        }
    }

    public static void clearByteBuffers() {
        for (LayerCache c : layerCaches.values()) {
            c.close();
        }
        layerCaches.clear();
        isLargeRender = false;
    }

    /**
     * Cache-miss driver. Called from the {@link RenderLevelStageEvent.AfterTranslucentBlocks} handler
     * when {@link #shouldUpdateRender(Player, ItemStack)} says we need a fresh bake.
     */
    public static void buildRender(RenderLevelStageEvent evt, Player player, ItemStack gadget) {
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        BlockPos anchorPos = GadgetNBT.getAnchorPos(gadget);
        BlockPos renderPos = anchorPos.equals(GadgetNBT.nullPos) ? lookingAt.getBlockPos() : anchorPos;
        BaseMode mode = GadgetNBT.getMode(gadget);

        GlobalPos boundTo = GadgetNBT.getBoundPos(gadget);
        if (boundTo != null && boundTo.dimension().equals(player.level().dimension()))
            drawBoundBox(evt.getPoseStack(), boundTo.pos());

        if (gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste) {
            renderPos = renderPos.above();
            renderPos.offset(GadgetNBT.getRelativePaste(gadget));
            if (mode.getId().getPath().equals("copy") || mode.getId().getPath().equals("cut")) {
                drawCopyBox(evt.getPoseStack(), gadget, mode.getId().getPath());
                return;
            }
        }

        if (shouldUpdateRender(player, gadget))
            generateRender(player.level(), renderPos, gadget, 0.5f);
    }

    public static boolean shouldUpdateRender(Player player, ItemStack gadget) {
        ArrayList<StatePos> buildList;
        BaseMode mode = GadgetNBT.getMode(gadget);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        BlockPos anchorPos = GadgetNBT.getAnchorPos(gadget);
        BlockPos renderPos = anchorPos.equals(GadgetNBT.nullPos) ? lookingAt.getBlockPos() : anchorPos;
        UUID gadgetUUID = GadgetNBT.getUUID(gadget);

        if (gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) {
            if (player.level().getBlockState(renderPos).isAir())
                return false;
            BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
            if (renderBlockState.isAir()) return false;
            buildList = mode.collect(lookingAt.getDirection(), player, renderPos, renderBlockState);

            FakeRenderingWorld tempWorld = new FakeRenderingWorld(player.level(), buildList, renderPos);
            if (fakeRenderingWorld != null && fakeRenderingWorld.positions.equals(tempWorld.positions))
                return false;

            statePosCache = buildList;
            copyPasteUUIDCache = UUID.randomUUID();
            return true;
        } else if (gadget.getItem() instanceof GadgetCopyPaste || gadget.getItem() instanceof GadgetCutPaste) {
            renderPos = renderPos.above();
            renderPos.offset(GadgetNBT.getRelativePaste(gadget));
            if (mode.getId().getPath().equals("paste")) {
                if (!BG2DataClient.isClientUpToDate(gadget))
                    return false;
                UUID BG2ClientUUID = BG2DataClient.getCopyUUID(gadgetUUID);
                if (BG2ClientUUID != null && copyPasteUUIDCache.equals(BG2ClientUUID))
                    return false;
                copyPasteUUIDCache = BG2ClientUUID;
                statePosCache = BG2DataClient.getLookupFromUUID(gadgetUUID);
                return true;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * Tesselates {@code statePosCache} into per-layer {@link BufferBuilder}s, builds each one,
     * and uploads its vertices into a persistent {@link GpuBuffer}. Called on cache miss only.
     * <p>
     * BE-backed ghost blocks (chests, furnaces, etc.) are silently skipped — re-introduction
     * would require wiring a {@code MultiBufferSource} through a BER submit, which the new
     * BER contract doesn't give us access to from here.
     */
    public static void generateRender(Level level, BlockPos renderPos, ItemStack gadget, float transparency) {
        boolean isExchanging = gadget.getItem() instanceof BaseGadget && GadgetNBT.getMode(gadget).isExchanging;
        if (statePosCache == null || statePosCache.isEmpty()) return;
        fakeRenderingWorld = new FakeRenderingWorld(level, statePosCache, renderPos);
        fakeRenderingWorldTint = new FakeWorldTintAdapter(fakeRenderingWorld, level);

        PoseStack matrix = new PoseStack();
        if (modelBlockRenderer == null) {
            // Free-floating ghost: don't cull against the (fake-world) neighbors, we want all 6 faces.
            modelBlockRenderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
        }
        final RandomSource random = RandomSource.create();

        clearByteBuffers();
        isLargeRender = statePosCache.size() > 50000;

        // One BufferBuilder per layer. Built after the full iteration so builders stay lightweight.
        Map<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);
        Map<ChunkSectionLayer, ByteBufferBuilder> byteBuilders = new EnumMap<>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : LAYERS) {
            ByteBufferBuilder bb = new ByteBufferBuilder(layer.pipeline().getVertexFormat().getVertexSize() * 1024);
            byteBuilders.put(layer, bb);
            builders.put(layer, new BufferBuilder(bb, VertexFormat.Mode.QUADS, layer.pipeline().getVertexFormat()));
        }

        // Pre-compute the byte alpha we want on every vertex. ARGB.color(float, int) stomps the alpha byte
        // onto the RGB bits of an existing color in one op.
        final float alpha = transparency;

        // Exchanger overlay: the 1.21.1 code translated by -0.0005 and scaled by 1.001 so the ghost
        // sits slightly outside the real block on all six faces, preventing Z-fighting. tesselateBlock
        // doesn't take a scale, so we apply the offset here and scale each vertex position in the
        // BlockQuadOutput callback below. Net effect: block renders from -0.0005 to 1.0005 per axis.
        final float exchangerInset = isExchanging ? -0.0005f : 0f;
        final float exchangerScale = isExchanging ? 1.001f : 1f;

        for (StatePos pos : statePosCache) {
            BlockState renderState = fakeRenderingWorld.getBlockStateWithoutReal(pos.pos);
            if (renderState.isAir()) continue;

            float px = pos.pos.getX() + exchangerInset;
            float py = pos.pos.getY() + exchangerInset;
            float pz = pos.pos.getZ() + exchangerInset;

            if (!renderState.getFluidState().isEmpty()) {
                // Fluids take a VertexConsumer and use putBakedQuad internally. Route to the translucent
                // builder — fluid geometry always sorts with the other translucent content.
                matrix.pushPose();
                matrix.translate(px, py, pz);
                RenderFluidBlock.renderFluidBlock(
                        renderState, level, pos.pos.offset(renderPos).above(255), matrix,
                        new AlphaAppliedConsumer(builders.get(ChunkSectionLayer.TRANSLUCENT), alpha),
                        false);
                matrix.popPose();
            } else {
                BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderState);
                long seed = renderState.getSeed(pos.pos.offset(renderPos));

                BlockQuadOutput out = (qx, qy, qz, quad, inst) -> {
                    inst.setColor(0, ARGB.color(alpha, inst.getColor(0)));
                    inst.setColor(1, ARGB.color(alpha, inst.getColor(1)));
                    inst.setColor(2, ARGB.color(alpha, inst.getColor(2)));
                    inst.setColor(3, ARGB.color(alpha, inst.getColor(3)));
                    ChunkSectionLayer layer = quad.materialInfo().layer();
                    BufferBuilder b = builders.get(layer);
                    if (exchangerScale != 1f) {
                        // Scale each vertex position around the block origin so the ghost extends
                        // slightly past the real block on all 6 faces (Z-fighting prevention).
                        org.joml.Vector3fc normal = quad.direction().getUnitVec3f();
                        int lightEmission = quad.materialInfo().lightEmission();
                        for (int v = 0; v < 4; v++) {
                            org.joml.Vector3fc p = quad.position(v);
                            long packedUv = quad.packedUV(v);
                            int vertexColor = ARGB.multiply(inst.getColor(v), quad.bakedColors().color(v));
                            int light = inst.getLightCoordsWithEmission(v, lightEmission);
                            float u = net.minecraft.client.model.geom.builders.UVPair.unpackU(packedUv);
                            float vv = net.minecraft.client.model.geom.builders.UVPair.unpackV(packedUv);
                            b.addVertex(
                                    p.x() * exchangerScale + qx,
                                    p.y() * exchangerScale + qy,
                                    p.z() * exchangerScale + qz,
                                    vertexColor, u, vv,
                                    inst.overlayCoords(), light,
                                    normal.x(), normal.y(), normal.z());
                        }
                    } else {
                        b.putBlockBakedQuad(qx, qy, qz, quad, inst);
                    }
                };

                try {
                    modelBlockRenderer.tesselateBlock(out, px, py, pz,
                            fakeRenderingWorldTint,
                            pos.pos.offset(renderPos).above(255),
                            renderState, model, seed);
                } catch (Exception e) {
                    // Mirrors 1.21.1 behavior — Create blocks and a handful of other outliers occasionally
                    // throw during the tesselation pass. Swallow per-block so the whole preview still renders.
                }
            }
        }

        // Build each non-empty layer's MeshData, upload its vertices to a persistent GpuBuffer,
        // then drop the MeshData. For translucent: also build a sorted index buffer on the GPU.
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 subtracted = projectedView.subtract(renderPos.getX(), renderPos.getY(), renderPos.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);

        GpuDevice device = RenderSystem.getDevice();
        CommandEncoder encoder = device.createCommandEncoder();

        for (ChunkSectionLayer layer : LAYERS) {
            BufferBuilder builder = builders.get(layer);
            MeshData mesh = builder.build();
            if (mesh == null) {
                byteBuilders.get(layer).close();
                continue;
            }
            LayerCache cache = new LayerCache();
            java.nio.ByteBuffer vtx = mesh.vertexBuffer();
            cache.vertexBuffer = device.createBuffer(
                    () -> "BG2 preview " + layer.name() + " VBO",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    vtx);
            cache.indexCount = mesh.drawState().indexCount();
            cache.autoIndexType = mesh.drawState().indexType();

            // Sort every layer — not just TRANSLUCENT. In 1.21.1 every render type bucket was
            // sorted because BG2 draws all layers with translucent blend. Without this, SOLID
            // quads (dirt, stone, etc.) are drawn in arbitrary tesselation order and produce the
            // doubled-face artifact visible in ss1/ss2.
            MeshData.SortState sortState = mesh.sortQuads(sortIndexScratch, VertexSorting.byDistance(v -> -sortPos.distanceSquared(v)));
            cache.sortState = sortState;
            java.nio.ByteBuffer sortedIdx = mesh.indexBuffer();
            if (sortedIdx != null) {
                cache.sortedIndexBuffer = device.createBuffer(
                        () -> "BG2 preview " + layer.name() + " IBO",
                        GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                        sortedIdx);
            }

            mesh.close();
            byteBuilders.get(layer).close();
            layerCaches.put(layer, cache);
        }
    }

    public static void drawCopyBox(PoseStack matrix, ItemStack gadget, String mode) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        BlockPos start = GadgetNBT.getCopyStartPos(gadget);
        BlockPos end = GadgetNBT.getCopyEndPos(gadget);
        Color color = mode.equals("copy") ? Color.GREEN : Color.RED;
        MyRenderMethods.renderCopy(matrix, start, end, color);
        matrix.popPose();
    }

    public static void drawBoundBox(PoseStack matrix, BlockPos blockPos) {
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        Color color = Color.BLUE;
        MyRenderMethods.renderCopy(matrix, blockPos, blockPos, color);
        matrix.popPose();
    }

    /**
     * Probe: does this BlockState have any quads at all? Used upstream to split previews into the
     * "normal block renderer" path vs. a "needs a BER" path (which BG2 no longer supports).
     */
    public static boolean isModelRender(BlockState state) {
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
        java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);
        for (net.minecraft.client.renderer.block.dispatch.BlockStateModelPart part : parts) {
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.values()) {
                if (!part.getQuads(d).isEmpty()) return true;
            }
            if (!part.getQuads(null).isEmpty()) return true;
        }
        return false;
    }

    /**
     * Per-frame draw. Issues one manual {@link RenderPass} per cached layer, reusing the persistent
     * {@link GpuBuffer}s populated by {@link #generateRender}. No CPU→GPU vertex re-upload.
     */
    public static void drawRender(RenderLevelStageEvent evt, Player player, ItemStack gadget) {
        if (layerCaches.isEmpty() || statePosCache == null) {
            return;
        }
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().position();
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
                return;
            }
            if (!GadgetNBT.hasCopyUUID(gadget) || !copyPasteUUIDCache.equals(GadgetNBT.getCopyUUID(gadget)))
                return;
            renderPos = renderPos.above().offset(GadgetNBT.getRelativePaste(gadget));
        }

        // Re-sort translucent every N frames to kill the screendoor effect as the camera moves.
        int sortFrequency = isLargeRender ? 100 : 20;
        if (sortCounter > sortFrequency) {
            sortAll(renderPos);
            sortCounter = 0;
        } else {
            sortCounter++;
        }

        // In 26.1 the event fires inside LevelRenderer's frame graph pass, and LevelRenderer has
        // already pushed the camera view rotation onto RenderSystem.getModelViewStack() at
        // renderLevel entry (LevelRenderer.java:482-483). Re-multiplying evt.getModelViewMatrix()
        // here would stack the rotation twice. Just translate by (renderPos - camera).
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.translate(
                (float) (-projectedView.x() + renderPos.getX()),
                (float) (-projectedView.y() + renderPos.getY()),
                (float) (-projectedView.z() + renderPos.getZ()));

        try {
            // Draw order: solid → cutout → translucent. Matches vanilla chunk draw order.
            drawLayer(ChunkSectionLayer.SOLID, OurRenderTypes.RenderBlock);
            drawLayer(ChunkSectionLayer.CUTOUT, OurRenderTypes.RenderBlock);
            drawLayer(ChunkSectionLayer.TRANSLUCENT, OurRenderTypes.RenderBlock);
        } finally {
            modelViewStack.popMatrix();
        }

        // Red overlay for blocks we don't have the items/energy to build.
        boolean hasBound = GadgetNBT.getBoundPos(gadget) != null;
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadget);
        if ((gadget.getItem() instanceof GadgetBuilding || gadget.getItem() instanceof GadgetExchanger) && !player.isCreative() && !hasBound && renderBlockState.getFluidState().isEmpty()) {
            ItemStack findStack = GadgetUtils.getItemForBlock(renderBlockState, player.level(), BlockPos.ZERO, player);
            int availableItems = BuildingUtils.countItemStacks(player, findStack);
            int energyStored = BuildingUtils.getEnergyStored(gadget);
            int energyCost = BuildingUtils.getEnergyCost(gadget);
            PoseStack matrix = evt.getPoseStack();
            var buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
            for (StatePos statePos : buildList) {
                if (availableItems <= 0 || energyStored < energyCost) {
                    matrix.pushPose();
                    matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
                    matrix.translate(renderPos.getX(), renderPos.getY(), renderPos.getZ());
                    var builder = buffersource.getBuffer(OurRenderTypes.MissingBlockOverlay);
                    MyRenderMethods.renderBoxSolid(evt.getPoseStack().last().pose(), builder, statePos.pos, 1, 0, 0, 0.35f);
                    matrix.popPose();
                }
                availableItems--;
                energyStored -= energyCost;
            }
        }
    }

    /**
     * Run one manual {@link RenderPass} for a single cached layer. Mirrors the body of
     * {@link RenderType#draw(MeshData)} but with a pre-uploaded GPU vertex buffer instead of the
     * per-frame immediate upload.
     */
    private static void drawLayer(ChunkSectionLayer layer, RenderType bg2Type) {
        LayerCache cache = layerCaches.get(layer);
        if (cache == null || cache.vertexBuffer == null || cache.indexCount == 0) return;

        RenderSetup state = bg2Type.state;
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        java.util.function.Consumer<Matrix4fStack> layeringModifier = state.layeringTransform.getModifier();
        if (layeringModifier != null) {
            modelViewStack.pushMatrix();
            layeringModifier.accept(modelViewStack);
        }

        // Same dynamic transforms upload vanilla uses in RenderType.draw(). The pipeline's uniform layout
        // expects a DynamicTransforms UBO slice — without this, the draw ends up with an identity MVP.
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1f, 1f, 1f, 1f),
                        new Vector3f(),
                        state.textureTransform.getMatrix());
        Map<String, RenderSetup.TextureAndSampler> textures = state.getTextures();

        RenderTarget renderTarget = bg2Type.outputTarget().getRenderTarget();
        GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
                ? RenderSystem.outputColorTextureOverride
                : renderTarget.getColorTextureView();
        GpuTextureView depthTexture = renderTarget.useDepth
                ? (RenderSystem.outputDepthTextureOverride != null ? RenderSystem.outputDepthTextureOverride : renderTarget.getDepthTextureView())
                : null;

        // Pick the index buffer: translucent uses our persistent sorted buffer, everyone else leans
        // on RenderSystem's shared sequential-quad index buffer (also a persistent GPU buffer).
        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (cache.sortedIndexBuffer != null) {
            indices = cache.sortedIndexBuffer;
            indexType = cache.autoIndexType;
        } else {
            RenderSystem.AutoStorageIndexBuffer auto = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            indices = auto.getBuffer(cache.indexCount);
            indexType = auto.type();
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (RenderPass pass = encoder.createRenderPass(
                () -> "BG2 preview draw " + layer.name(),
                colorTexture, OptionalInt.empty(),
                depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(state.pipeline);

            ScissorState scissorState = RenderSystem.getScissorStateForRenderTypeDraws();
            if (scissorState.enabled()) {
                pass.enableScissor(scissorState.x(), scissorState.y(), scissorState.width(), scissorState.height());
            }

            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("DynamicTransforms", dynamicTransforms);
            pass.setVertexBuffer(0, cache.vertexBuffer);
            for (Map.Entry<String, RenderSetup.TextureAndSampler> e : textures.entrySet()) {
                pass.bindTexture(e.getKey(), e.getValue().textureView(), e.getValue().sampler());
            }
            pass.setIndexBuffer(indices, indexType);
            pass.drawIndexed(0, 0, cache.indexCount, 1);
        }

        if (layeringModifier != null) {
            modelViewStack.popMatrix();
        }
    }

    /**
     * Re-sort every cached layer's quads by distance from the camera. In 1.21.1 every render type
     * bucket was sorted because BG2 draws all layers with translucent blend state.
     */
    public static void sortAll(BlockPos lookingAt) {
        if (layerCaches.isEmpty()) return;

        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 subtracted = projectedView.subtract(lookingAt.getX(), lookingAt.getY(), lookingAt.getZ());
        Vector3f sortPos = new Vector3f((float) subtracted.x, (float) subtracted.y, (float) subtracted.z);
        VertexSorting sorting = VertexSorting.byDistance(v -> -sortPos.distanceSquared(v));

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        for (Map.Entry<ChunkSectionLayer, LayerCache> entry : layerCaches.entrySet()) {
            LayerCache cache = entry.getValue();
            if (cache.sortState == null) continue;

            ByteBufferBuilder.Result result = cache.sortState.buildSortedIndexBuffer(sortIndexScratch, sorting);
            if (result == null) continue;

            try {
                java.nio.ByteBuffer bytes = result.byteBuffer();
                int needed = bytes.remaining();
                if (cache.sortedIndexBuffer == null || cache.sortedIndexBuffer.size() < needed) {
                    if (cache.sortedIndexBuffer != null) cache.sortedIndexBuffer.close();
                    final ChunkSectionLayer layerName = entry.getKey();
                    cache.sortedIndexBuffer = RenderSystem.getDevice().createBuffer(
                            () -> "BG2 preview " + layerName.name() + " IBO (resorted)",
                            GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                            bytes);
                } else {
                    encoder.writeToBuffer(cache.sortedIndexBuffer.slice(0, needed), bytes);
                }
            } finally {
                result.close();
            }
        }
    }

    /**
     * Adapter that exposes {@link FakeRenderingWorld} (which only implements the server-side
     * {@code LevelAccessor}) as a client-side {@link BlockAndTintGetter}. In 26.1 the client-side
     * tint interface split off from the world-level one, so {@code ModelBlockRenderer.tesselateBlock}
     * now takes a client {@code BlockAndTintGetter} we can't provide directly from {@code LevelAccessor}.
     * <p>
     * Block lookups go to the fake world (so ghost neighbors influence the render); light/tint lookups
     * fall back to the real world (so the preview picks up real-world lighting).
     */
    static final class FakeWorldTintAdapter implements BlockAndTintGetter {
        private final FakeRenderingWorld fake;
        // ClientLevel, not Level: getBlockTint and getBlockColor only live on the client-side
        // BlockAndTintGetter now, and ClientLevel is the only Level subclass that implements it.
        // VBORenderer is client-only so the downcast is safe.
        private final ClientLevel real;

        FakeWorldTintAdapter(FakeRenderingWorld fake, Level real) {
            this.fake = fake;
            this.real = (ClientLevel) real;
        }

        @Override
        public CardinalLighting cardinalLighting() {
            return CardinalLighting.DEFAULT;
        }

        @Override
        public int getBlockTint(BlockPos pos, ColorResolver color) {
            return real.getBlockTint(pos, color);
        }

        @Override
        public BlockEntity getBlockEntity(BlockPos pos) {
            return fake.getBlockEntity(pos);
        }

        @Override
        public BlockState getBlockState(BlockPos pos) {
            return fake.getBlockState(pos);
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return fake.getFluidState(pos);
        }

        @Override
        public int getHeight() {
            return real.getHeight();
        }

        @Override
        public int getMinY() {
            return real.getMinY();
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return real.getLightEngine();
        }
    }

    /**
     * Minimal VertexConsumer wrapper used only to feed {@link RenderFluidBlock}'s fluid quads into the
     * translucent BufferBuilder while stamping BG2's preview alpha on each vertex color.
     * <p>
     * Why this exists: {@code VertexConsumer.putBakedQuad} internally calls {@code addVertex(..., color, ...)}
     * where {@code color} is a packed ARGB int — so the old {@code setColor(int,int,int,int)} interception
     * in {@code DireVertexConsumer} never fires on the fluid path. Intercepting {@code addVertex(...)} with
     * the packed-color argument catches every write regardless of which code path produced it.
     */
    private static final class AlphaAppliedConsumer extends net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper {
        private final float alpha;

        AlphaAppliedConsumer(com.mojang.blaze3d.vertex.VertexConsumer parent, float alpha) {
            super(parent);
            this.alpha = alpha;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            parent.addVertex(x, y, z, ARGB.color(alpha, color), u, v, overlayCoords, lightCoords, nx, ny, nz);
        }
    }
}

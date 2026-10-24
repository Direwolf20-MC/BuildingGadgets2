package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.util.FakeRenderingWorld;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.UUID;

/**
 * 3D template preview rendered into the Template Manager GUI panel.
 * <p>
 * Implemented as a {@link PictureInPictureRenderer}: vanilla allocates a color+depth render target
 * sized to the panel rect times the GUI scale, redirects {@link RenderSystem#outputColorTextureOverride}
 * to point at it, invokes {@link #renderToTexture}, then blits the result into the GUI at the panel's
 * screen position with scissor applied. This is the canonical 26.1 "3D-scene-inside-a-GUI-rect" path
 * (§3.21 bucket (c) of {@code PORTING_1.21.1_TO_26.1.md}).
 * <p>
 * The parent class's {@link #prepare} body hardcodes an orthographic projection for the PiP render
 * target, and also pre-seeds the {@link PoseStack} with a {@code translate(width/2, height, 0)}
 * plus {@code scale(scale, scale, -scale)} that presumes ortho. Both are wrong for a 3D rotatable
 * preview. Fix: this renderer overrides the projection to perspective inside
 * {@link #renderToTexture} (vanilla runs it after the ortho setup and restores projection itself at
 * the GUI blit stage), and resets the PoseStack to identity before applying its own model-view.
 * <p>
 * Inside {@code renderToTexture} the retained-GPU-mesh bake/draw pattern from
 * {@link VBORenderer} is replayed against a private per-instance {@link LayerCache} — so the GUI
 * preview cache is independent of VBORenderer's in-world preview cache and the two never thrash
 * each other when a player holds a loaded template and also aims a gadget at the world.
 */
public class GuiTemplatePreview extends PictureInPictureRenderer<GuiTemplatePreview.State> {
    private static final ChunkSectionLayer[] LAYERS = ChunkSectionLayer.values();

    // Tracks whatever statePos list we last baked. Identity compare against the current state's
    // list is cheap and correct — the caller (TemplateManagerGUI) re-fetches from BG2DataClient
    // only on UUID change, so the reference is stable across non-template-change frames.
    private @Nullable ArrayList<StatePos> cachedList;
    private @Nullable UUID cachedUuid;
    private float cachedCenterX, cachedCenterY, cachedCenterZ;
    private float cachedRadius;

    private final Map<ChunkSectionLayer, LayerCache> layerCaches = new EnumMap<>(ChunkSectionLayer.class);

    // Scratch ring for sorted-index rebuilds. One per renderer instance; fine — pooled across frames.
    private final ByteBufferBuilder sortIndexScratch = new ByteBufferBuilder(131072);

    // Our own projection (perspective) that stomps the parent's ortho before we draw.
    private final ProjectionMatrixBuffer projectionMatrixBuffer = new ProjectionMatrixBuffer("BG2 template preview");
    private final Projection projection = new Projection();

    // Non-AO model renderer. Free-floating ghost blocks: don't cull against neighbors.
    private @Nullable ModelBlockRenderer modelBlockRenderer;

    public GuiTemplatePreview(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<State> getRenderStateClass() {
        return State.class;
    }

    @Override
    protected String getTextureLabel() {
        return "bg2 template preview";
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        // Parent uses this in its presumed-ortho PoseStack pre-seed. We reset the PoseStack to
        // identity inside renderToTexture so this value never actually reaches the draw — override
        // returns center-of-height which at least leaves the ortho ready for vanilla's baseline.
        return height / 2.0f;
    }

    @Override
    public boolean canBeReusedFor(State state, int textureWidth, int textureHeight) {
        // Reuse whenever the texture dimensions match. The actual mesh cache lives on this renderer
        // instance and is keyed by state's template UUID, so if the template swapped we rebuild
        // inside renderToTexture — texture reuse is independent of mesh reuse.
        return super.canBeReusedFor(state, textureWidth, textureHeight);
    }

    @Override
    protected void renderToTexture(State state, PoseStack poseStack) {
        if (state.statePosList != cachedList || !java.util.Objects.equals(state.templateUuid, cachedUuid)) {
            rebuildCache(state);
        }
        if (layerCaches.isEmpty()) {
            return;
        }

        // Step 2: override projection. Parent set an ortho projection for the PiP texture; we
        // want perspective for the rotatable 3D view. The texture is sized to
        // (panelWidth * guiScale, panelHeight * guiScale) — use the texture dimensions so the
        // aspect ratio matches the GUI rect vanilla will blit into.
        int texW = (state.x1() - state.x0()) * state.guiScale;
        int texH = (state.y1() - state.y0()) * state.guiScale;
        RenderSystem.backupProjectionMatrix();
        projection.setupPerspective(0.05f, 1000.0f, 60.0f, texW, texH);
        RenderSystem.setProjectionMatrix(projectionMatrixBuffer.getBuffer(projection), ProjectionType.PERSPECTIVE);

        // Step 3: build model-view. Parent pre-seeded poseStack with an ortho-presuming transform
        // — scrap it. The pose passed to us is fresh (constructed at prepare entry in the parent),
        // so setIdentity() fully resets. Then apply user's rotate/zoom/pan, then a translate that
        // centers the bake-time bounding-box at the origin.
        poseStack.setIdentity();
        // Camera pulled back along -Z by (radius * 2 - zoom). Larger radius → camera further away.
        // Zoom is added directly — positive zoom pushes the camera forward, matching the old code.
        float cameraDistance = Math.max(1.0f, cachedRadius * 2.5f - state.zoom * 0.01f);
        poseStack.translate(state.panX * 0.01f, -state.panY * 0.01f, -cameraDistance);
        poseStack.mulPose(new org.joml.Quaternionf().setAngleAxis(state.rotX * (float) Math.PI / 180.0f, 1, 0, 0));
        poseStack.mulPose(new org.joml.Quaternionf().setAngleAxis(state.rotY * (float) Math.PI / 180.0f, 0, 1, 0));
        // Center the template on the origin so rotation pivots around its center, not its corner.
        poseStack.translate(-cachedCenterX, -cachedCenterY, -cachedCenterZ);

        // Step 4: push the PoseStack's top matrix onto the RenderSystem model-view stack. Our
        // manual draw body (mirrors RenderType.draw()) reads RenderSystem.getModelViewMatrix() —
        // not a PoseStack — for DynamicTransforms, same as VBORenderer does for the in-world path.
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(poseStack.last().pose());

        try {
            drawLayer(ChunkSectionLayer.SOLID, OurRenderTypes.RenderBlock);
            drawLayer(ChunkSectionLayer.CUTOUT, OurRenderTypes.RenderBlock);
            drawLayer(ChunkSectionLayer.TRANSLUCENT, OurRenderTypes.RenderBlock);
        } finally {
            modelViewStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    /**
     * Tesselate {@code state.statePosList} into per-layer {@link GpuBuffer}s. Mirrors
     * {@link VBORenderer#generateRender} but writes into this instance's private cache.
     */
    private void rebuildCache(State state) {
        clearCache();
        ArrayList<StatePos> list = state.statePosList;
        if (list == null || list.isEmpty()) {
            cachedList = list;
            cachedUuid = state.templateUuid;
            return;
        }

        Level level = Minecraft.getInstance().level;
        if (level == null) return;

        if (modelBlockRenderer == null) {
            modelBlockRenderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
        }

        // Compute bounding box so we can center the template on the origin at draw time.
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        for (StatePos sp : list) {
            BlockPos p = sp.pos;
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }
        cachedCenterX = (minX + maxX) * 0.5f + 0.5f;
        cachedCenterY = (minY + maxY) * 0.5f + 0.5f;
        cachedCenterZ = (minZ + maxZ) * 0.5f + 0.5f;
        float dx = maxX - minX + 1, dy = maxY - minY + 1, dz = maxZ - minZ + 1;
        cachedRadius = 0.5f * (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Fake world for neighbor-aware block models. renderOrigin is BlockPos.ZERO because we
        // already bake absolute positions into the vertices and fold the centering into the
        // per-frame model-view.
        BlockPos renderOrigin = BlockPos.ZERO;
        FakeRenderingWorld fake = new FakeRenderingWorld(level, list, renderOrigin);
        VBORenderer.FakeWorldTintAdapter levelAdapter = new VBORenderer.FakeWorldTintAdapter(fake, level);

        Map<ChunkSectionLayer, BufferBuilder> builders = new EnumMap<>(ChunkSectionLayer.class);
        Map<ChunkSectionLayer, ByteBufferBuilder> byteBuilders = new EnumMap<>(ChunkSectionLayer.class);
        for (ChunkSectionLayer layer : LAYERS) {
            ByteBufferBuilder bb = new ByteBufferBuilder(layer.pipeline().getVertexFormat().getVertexSize() * 1024);
            byteBuilders.put(layer, bb);
            builders.put(layer, new BufferBuilder(bb, VertexFormat.Mode.QUADS, layer.pipeline().getVertexFormat()));
        }

        final float alpha = 1.0f;
        final RandomSource random = RandomSource.create();

        for (StatePos pos : list) {
            BlockState renderState = fake.getBlockState(pos.pos);
            if (renderState.isAir()) continue;
            // Fluids: route through the translucent builder via the same alpha-stamping path as
            // VBORenderer. Fluids rarely appear in templates but keep the code symmetric.
            if (!renderState.getFluidState().isEmpty()) {
                PoseStack fluidMatrix = new PoseStack();
                fluidMatrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
                RenderFluidBlock.renderFluidBlock(
                        renderState, level, pos.pos.above(255), fluidMatrix,
                        builders.get(ChunkSectionLayer.TRANSLUCENT), false);
                continue;
            }

            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderState);
            long seed = renderState.getSeed(pos.pos);
            float px = pos.pos.getX();
            float py = pos.pos.getY();
            float pz = pos.pos.getZ();

            BlockQuadOutput out = (qx, qy, qz, quad, inst) -> {
                // Alpha is 1.0 here but we still route through ARGB.color so the call shape matches
                // VBORenderer and any future translucent tweak is one line away.
                inst.setColor(0, ARGB.color(alpha, inst.getColor(0)));
                inst.setColor(1, ARGB.color(alpha, inst.getColor(1)));
                inst.setColor(2, ARGB.color(alpha, inst.getColor(2)));
                inst.setColor(3, ARGB.color(alpha, inst.getColor(3)));
                ChunkSectionLayer layer = quad.materialInfo().layer();
                builders.get(layer).putBlockBakedQuad(qx, qy, qz, quad, inst);
            };

            try {
                modelBlockRenderer.tesselateBlock(out, px, py, pz, levelAdapter, pos.pos.above(255),
                        renderState, model, seed);
            } catch (Exception ignored) {
                // Some blocks (Create, etc.) throw during tesselation with a non-standard level;
                // swallow per-block so the whole preview still shows.
            }
        }

        // Build each layer's MeshData and upload vertices to persistent GpuBuffers.
        GpuDevice device = RenderSystem.getDevice();
        for (ChunkSectionLayer layer : LAYERS) {
            MeshData mesh = builders.get(layer).build();
            if (mesh == null) {
                byteBuilders.get(layer).close();
                continue;
            }
            LayerCache cache = new LayerCache();
            java.nio.ByteBuffer vtx = mesh.vertexBuffer();
            cache.vertexBuffer = device.createBuffer(
                    () -> "BG2 gui preview " + layer.name() + " VBO",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
                    vtx);
            cache.indexCount = mesh.drawState().indexCount();
            cache.autoIndexType = mesh.drawState().indexType();

            if (layer == ChunkSectionLayer.TRANSLUCENT) {
                // Sort once at bake time. Resort-per-frame from the GUI camera is overkill for
                // a 20-block template; a single sort from the template center is good enough.
                Vector3f sortOrigin = new Vector3f(cachedCenterX, cachedCenterY, cachedCenterZ);
                MeshData.SortState sortState = mesh.sortQuads(sortIndexScratch,
                        VertexSorting.byDistance(v -> sortOrigin.distanceSquared(v)));
                cache.sortState = sortState;
                java.nio.ByteBuffer sortedIdx = mesh.indexBuffer();
                if (sortedIdx != null) {
                    cache.sortedIndexBuffer = device.createBuffer(
                            () -> "BG2 gui preview " + layer.name() + " IBO",
                            GpuBuffer.USAGE_INDEX | GpuBuffer.USAGE_COPY_DST,
                            sortedIdx);
                }
            }
            mesh.close();
            byteBuilders.get(layer).close();
            layerCaches.put(layer, cache);
        }

        cachedList = list;
        cachedUuid = state.templateUuid;
    }

    /**
     * Mirrors {@link RenderType#draw(MeshData)}'s body against a pre-uploaded vertex GpuBuffer.
     * Copy of {@link VBORenderer}'s {@code drawLayer}, minus the layering-transform handling
     * (VIEW_OFFSET_Z_LAYERING's effect is a no-op here since there's no real chunk geometry to
     * avoid Z-fighting against in the GUI preview).
     */
    private void drawLayer(ChunkSectionLayer layer, RenderType bg2Type) {
        LayerCache cache = layerCaches.get(layer);
        if (cache == null || cache.vertexBuffer == null || cache.indexCount == 0) return;

        RenderSetup setup = bg2Type.state;
        java.util.function.Consumer<Matrix4fStack> layeringModifier = setup.layeringTransform.getModifier();
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        if (layeringModifier != null) {
            modelViewStack.pushMatrix();
            layeringModifier.accept(modelViewStack);
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(
                        RenderSystem.getModelViewMatrix(),
                        new Vector4f(1f, 1f, 1f, 1f),
                        new Vector3f(),
                        setup.textureTransform.getMatrix());
        Map<String, RenderSetup.TextureAndSampler> textures = setup.getTextures();

        // Output target: use whatever RenderSystem's override currently points at — vanilla's
        // PictureInPictureRenderer set it to our PiP color/depth textures before calling
        // renderToTexture. That's the whole reason PiP works: the manual draw writes to them.
        RenderTarget renderTarget = bg2Type.outputTarget().getRenderTarget();
        GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
                ? RenderSystem.outputColorTextureOverride
                : renderTarget.getColorTextureView();
        GpuTextureView depthTexture = renderTarget.useDepth
                ? (RenderSystem.outputDepthTextureOverride != null
                        ? RenderSystem.outputDepthTextureOverride
                        : renderTarget.getDepthTextureView())
                : null;

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
                () -> "BG2 gui preview draw " + layer.name(),
                colorTexture, OptionalInt.empty(),
                depthTexture, OptionalDouble.empty())) {
            pass.setPipeline(setup.pipeline);
            // No scissor dance — vanilla's PiP render target is our whole drawable area, and the
            // final GUI-rect scissor is applied by the BlitRenderState step *after* renderToTexture
            // returns. Setting a scissor here would clip against main-window coordinates, which
            // are irrelevant to the off-screen PiP target.
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

    private void clearCache() {
        for (LayerCache c : layerCaches.values()) c.close();
        layerCaches.clear();
    }

    @Override
    public void close() {
        clearCache();
        sortIndexScratch.close();
        projectionMatrixBuffer.close();
        super.close();
    }

    /**
     * Per-instance retained GPU mesh for one chunk-section layer. Same shape as
     * {@link VBORenderer}'s inner {@code LayerCache} but kept separate so the two don't share state.
     */
    private static final class LayerCache implements AutoCloseable {
        GpuBuffer vertexBuffer;
        int indexCount;
        VertexFormat.IndexType autoIndexType;
        GpuBuffer sortedIndexBuffer;
        MeshData.SortState sortState;

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

    /**
     * PiP render state carrying the panel rect, the user's current rotate/zoom/pan inputs, and
     * the statePos list to draw. The {@link PictureInPictureRenderState} contract wants x0/y0/x1/y1
     * in GUI coordinates plus a scale factor; we fold guiScale into the state so the renderer can
     * see it without a static dependency on {@link Minecraft#getInstance}.
     */
    public static final class State implements PictureInPictureRenderState {
        public final int x0;
        public final int y0;
        public final int x1;
        public final int y1;
        public final float scale;
        public final Matrix3x2f pose;
        public final @Nullable ScreenRectangle scissorArea;
        public final @Nullable ScreenRectangle bounds;

        public final float rotX, rotY, zoom, panX, panY;
        public final int guiScale;
        public final @Nullable UUID templateUuid;
        public final @Nullable ArrayList<StatePos> statePosList;

        public State(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRectangle scissorArea,
                     int guiScale,
                     float rotX, float rotY, float zoom, float panX, float panY,
                     @Nullable UUID templateUuid, @Nullable ArrayList<StatePos> statePosList) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.scale = 1.0f;
            this.pose = pose;
            this.scissorArea = scissorArea;
            // bounds is stored for internal consumers that want the clipped rect; PictureInPictureRenderState
            // itself doesn't require exposing it through the interface, so we keep it private to State.
            this.bounds = PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea);
            this.guiScale = guiScale;
            this.rotX = rotX;
            this.rotY = rotY;
            this.zoom = zoom;
            this.panX = panX;
            this.panY = panY;
            this.templateUuid = templateUuid;
            this.statePosList = statePosList;
        }

        @Override public int x0() { return x0; }
        @Override public int x1() { return x1; }
        @Override public int y0() { return y0; }
        @Override public int y1() { return y1; }
        @Override public float scale() { return scale; }
        @Override public Matrix3x2f pose() { return pose; }
        @Override public @Nullable ScreenRectangle scissorArea() { return scissorArea; }
        @Override public @Nullable ScreenRectangle bounds() { return bounds; }
    }
}

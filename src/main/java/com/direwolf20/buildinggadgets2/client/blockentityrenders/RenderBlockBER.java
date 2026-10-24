package com.direwolf20.buildinggadgets2.client.blockentityrenders;

import com.direwolf20.buildinggadgets2.client.renderer.DireVertexConsumerSquished;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.client.renderer.RenderFluidBlock;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RenderBlockBER implements BlockEntityRenderer<RenderBlockBE, RenderBlockBERState> {
    public RenderBlockBER(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public RenderBlockBERState createRenderState() {
        return new RenderBlockBERState();
    }

    @Override
    public void extractRenderState(
            RenderBlockBE be,
            RenderBlockBERState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.renderType = be.renderType;
        state.renderBlock = be.renderBlock;
        state.shrinking = be.shrinking;
        state.level = (ClientLevel) be.getLevel();

        float nowScale = (float) be.drawSize / (float) be.getMaxSize();
        float nextScale = (float) be.nextDrawSize() / (float) be.getMaxSize();
        state.scale = Mth.clamp(Mth.lerp(partialTicks, nowScale, nextScale), 0f, 1f);

        // Light sampling: the base extractRenderState samples at our position, but the RenderBlock is
        // light-transparent (propagatesSkylightDown=true, noOcclusion) so the sampled value reflects
        // surrounding ambient light, not what the target block will look like once placed. If the target
        // block emits light, blend that emission into the lightmap so the ghost doesn't appear darker
        // than the real block it's about to become — this softens the brightness pop at transition.
        if (state.level != null) {
            int sampled = LevelRenderer.getLightCoords(state.level, state.blockPos);
            if (state.renderBlock != null) {
                int emission = state.renderBlock.getLightEmission(state.level, state.blockPos);
                if (emission > 0) {
                    sampled = LightCoordsUtil.lightCoordsWithEmission(sampled, emission);
                }
            }
            state.lightCoords = sampled;
        }

        // Adjacency culling for renderFade: suppress shared faces between adjacent RenderBlocks with
        // similar drawSize to avoid overlapping translucent quads (z-fighting / double-alpha artifacts).
        java.util.Arrays.fill(state.cullFaces, false);
        if (state.level != null && state.renderBlock != null && be.renderType == 1) {
            for (Direction direction : Direction.values()) {
                BlockEntity neighbor = state.level.getBlockEntity(be.getBlockPos().relative(direction));
                if (neighbor instanceof RenderBlockBE neighborBE) {
                    if (neighborBE.renderBlock != null
                            && neighborBE.renderBlock.isSolidRender()
                            && Math.abs(be.drawSize - neighborBE.drawSize) < 5
                            && be.drawSize < neighborBE.drawSize) {
                        state.cullFaces[direction.ordinal()] = true;
                    }
                }
            }
        }

        // Resolve biome-aware block tints (grass, leaves, water, etc.). BlockColors.getTintSources
        // returns one BlockTintSource per tint layer the block uses; evaluate each against the
        // real level so the preview picks up biome-correct foliage/grass color.
        if (state.renderBlock != null && state.level != null) {
            java.util.List<BlockTintSource> sources = Minecraft.getInstance().getBlockColors().getTintSources(state.renderBlock);
            if (!sources.isEmpty()) {
                state.tintLayers = new int[sources.size()];
                for (int i = 0; i < sources.size(); i++) {
                    state.tintLayers[i] = sources.get(i).colorInWorld(state.renderBlock, state.level, state.blockPos);
                }
            } else {
                state.tintLayers = new int[]{-1};
            }
        }
    }

    @Override
    public void submit(
            RenderBlockBERState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {
        if (state.renderBlock == null) return;
        if (state.level == null) return;

        BlockState renderBlock = state.renderBlock;
        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderBlock);
        List<BlockStateModelPart> parts = new ArrayList<>();
        model.collectParts(RandomSource.create(), parts);

        byte type = state.renderType;
        if (type == 0) {
            renderGrow(state, poseStack, collector, renderBlock, parts);
        } else if (type == 1) {
            renderFade(state, poseStack, collector, renderBlock, parts);
        } else if (type == 2 || type == 3 || type == 4) {
            boolean adjustUV = type != 2;
            boolean bottomUp = type == 4;
            renderSquished(state, poseStack, collector, renderBlock, parts, adjustUV, bottomUp);
        } else if (type == 5) {
            renderSquishedSnap(state, poseStack, collector, renderBlock, parts);
        } else {
            renderGrow(state, poseStack, collector, renderBlock, parts);
        }
    }

    /**
     * renderType 0: a simple grow-from-center animation. Uses tesselateBlock to write AO-shaded
     * quads directly into the VertexConsumer, matching how the old 1.21.1 BER rendered.
     * tesselateBlock computes per-face AO from the real level, so the ghost's shading closely
     * matches what the chunk mesh will produce after the real block is placed.
     * Fluids go through submitCustomGeometry because FluidModel lives in a separate model set.
     */
    private void renderGrow(
            RenderBlockBERState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            BlockState renderBlock,
            List<BlockStateModelPart> parts) {
        float scale = state.scale;
        poseStack.pushPose();
        poseStack.translate((1 - scale) / 2, (1 - scale) / 2, (1 - scale) / 2);
        poseStack.scale(scale, scale, scale);

        if (renderBlock.getFluidState().isEmpty()) {
            if (!parts.isEmpty()) {
                BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderBlock);
                // tesselateBlock applies CardinalLighting (per-face directional shading) which
                // the chunk mesh also applies — but the entity render pipeline the BER uses
                // seems to double-darken these values. Undo the CardinalLighting scaling after
                // tesselateBlock so we keep the AO but not the extra darkening.
                net.minecraft.world.level.CardinalLighting lighting = state.level.cardinalLighting();
                collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(), (pose, buffer) -> {
                    ModelBlockRenderer renderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
                    renderer.tesselateBlock(
                            (x, y, z, quad, instance) -> {
                                // Undo the CardinalLighting scaling that prepareQuadAmbientOcclusion applied
                                float cardinalShade = quad.materialInfo().shade()
                                        ? lighting.byFace(quad.direction()) : lighting.up();
                                if (cardinalShade > 0.001f && cardinalShade < 0.999f) {
                                    float inverse = 1.0f / cardinalShade;
                                    instance.scaleColor(inverse);
                                }
                                buffer.putBakedQuad(pose, quad, instance);
                            },
                            0, 0, 0,
                            state.level, state.blockPos, renderBlock, model,
                            renderBlock.getSeed(state.blockPos)
                    );
                });
            }
        } else {
            collector.submitCustomGeometry(poseStack, OurRenderTypes.RenderBlockFade, (pose, buffer) ->
                    RenderFluidBlock.renderFluidBlock(renderBlock, state.level, state.blockPos, pose, buffer, true));
        }

        poseStack.popPose();
    }

    /**
     * renderType 1: fade the block in with per-vertex alpha modulation. Uses tesselateBlock for
     * proper AO lighting (same as renderGrow), then applies fade alpha and reverses the
     * CardinalLighting scaling. Adjacency culling is handled by skipping faces where
     * state.cullFaces is set.
     */
    private void renderFade(
            RenderBlockBERState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            BlockState renderBlock,
            List<BlockStateModelPart> parts) {
        BlockPos pos = state.blockPos;
        float scale = state.scale;
        float alpha = Mth.lerp(scale, 0.25f, 1f);
        int alphaInt = Math.round(alpha * 255f);
        boolean isSolid = renderBlock.isSolidRender();
        RenderType renderType = isSolid ? OurRenderTypes.RenderBlockFade : OurRenderTypes.RenderBlockFadeNoCull;

        if (!renderBlock.getFluidState().isEmpty()) {
            collector.submitCustomGeometry(poseStack, OurRenderTypes.RenderBlockFade, (pose, buffer) ->
                    RenderFluidBlock.renderFluidBlock(renderBlock, state.level, pos, pose, buffer, false, alpha));
            return;
        }

        if (parts.isEmpty()) return;

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderBlock);
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            ModelBlockRenderer renderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
            renderer.tesselateBlock(
                    (x, y, z, quad, instance) -> {
                        // Skip culled faces (adjacency optimization)
                        Direction dir = quad.direction();
                        if (dir != null && state.cullFaces[dir.ordinal()]) return;
                        // Apply fade alpha to each vertex color
                        for (int v = 0; v < 4; v++) {
                            int color = instance.getColor(v);
                            instance.setColor(v, ARGB.color(alphaInt,
                                    ARGB.red(color), ARGB.green(color), ARGB.blue(color)));
                        }
                        buffer.putBakedQuad(pose, quad, instance);
                    },
                    0, 0, 0,
                    state.level, state.blockPos, renderBlock, model,
                    renderBlock.getSeed(state.blockPos)
            );
        });
    }

    /**
     * renderType 2/3/4: squish/stretch animation. Uses tesselateBlock for proper AO, then routes
     * each quad through DireVertexConsumerSquished for position/UV rewriting. Block pipeline render
     * types — no CardinalLighting reversal needed.
     */
    private void renderSquished(
            RenderBlockBERState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            BlockState renderBlock,
            List<BlockStateModelPart> parts,
            boolean adjustUV,
            boolean bottomUp) {
        BlockPos pos = state.blockPos;
        float scale = Mth.lerp(state.scale, 0f, 1f);
        boolean isSolid = renderBlock.isSolidRender();
        RenderType renderType = isSolid ? OurRenderTypes.RenderBlockBackface : OurRenderTypes.RenderBlockFadeNoCull;
        boolean effectiveAdjustUV = adjustUV && isSolid;

        if (!renderBlock.getFluidState().isEmpty()) {
            collector.submitCustomGeometry(poseStack, OurRenderTypes.RenderBlockFade, (pose, buffer) -> {
                DireVertexConsumerSquished squished = new DireVertexConsumerSquished(
                        buffer, 0, 0, 0, 1, scale, 1, pose.pose());
                squished.adjustUV = effectiveAdjustUV;
                squished.bottomUp = bottomUp;
                RenderFluidBlock.renderFluidBlock(renderBlock, state.level, pos, pose, squished, true);
            });
            return;
        }

        if (parts.isEmpty()) return;

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderBlock);
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            DireVertexConsumerSquished squished = new DireVertexConsumerSquished(
                    buffer, 0, 0, 0, 1, scale, 1, pose.pose());
            squished.adjustUV = effectiveAdjustUV;
            squished.bottomUp = bottomUp;
            ModelBlockRenderer renderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
            renderer.tesselateBlock(
                    (x, y, z, quad, instance) -> {
                        squished.setSprite(quad.materialInfo().sprite());
                        squished.setDirection(quad.direction());
                        squished.putBakedQuad(pose, quad, instance);
                    },
                    0, 0, 0,
                    state.level, state.blockPos, renderBlock, model,
                    renderBlock.getSeed(state.blockPos)
            );
        });
    }

    /**
     * renderType 5: "snap" variant of the squish animation with a darkness modulation that fades
     * as the block settles. Uses tesselateBlock for proper AO, then applies darkness via per-vertex
     * color scaling and routes through DireVertexConsumerSquished. Uses Sheets.cutoutBlockSheet()
     * (entity pipeline) so CardinalLighting reversal IS needed.
     */
    private void renderSquishedSnap(
            RenderBlockBERState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            BlockState renderBlock,
            List<BlockStateModelPart> parts) {
        if (!state.shrinking && state.scale < 0.1f) return;
        BlockPos pos = state.blockPos;
        float darkness = Mth.lerp(state.scale, 0.25f, 1f);
        float scale = Mth.lerp(state.scale, 0.75f, 1f);
        int darkChannel = Math.round(darkness * 255f);
        boolean isSolid = renderBlock.isSolidRender();
        RenderType renderType = Sheets.cutoutBlockSheet();
        boolean effectiveAdjustUV = isSolid;

        if (!renderBlock.getFluidState().isEmpty()) {
            collector.submitCustomGeometry(poseStack, OurRenderTypes.RenderBlockFade, (pose, buffer) -> {
                DireVertexConsumerSquished squished = new DireVertexConsumerSquished(
                        buffer, 0, 0, 0, 1, scale, 1, pose.pose());
                squished.adjustUV = effectiveAdjustUV;
                squished.bottomUp = false;
                RenderFluidBlock.renderFluidBlock(renderBlock, state.level, pos, pose, squished, true);
            });
            return;
        }

        if (parts.isEmpty()) return;

        BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(renderBlock);
        net.minecraft.world.level.CardinalLighting lighting = state.level.cardinalLighting();
        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
            DireVertexConsumerSquished squished = new DireVertexConsumerSquished(
                    buffer, 0, 0, 0, 1, scale, 1, pose.pose());
            squished.adjustUV = effectiveAdjustUV;
            squished.bottomUp = false;
            ModelBlockRenderer renderer = new ModelBlockRenderer(true, false, Minecraft.getInstance().getBlockColors());
            renderer.tesselateBlock(
                    (x, y, z, quad, instance) -> {
                        // Undo CardinalLighting (entity pipeline render type)
                        float cardinalShade = quad.materialInfo().shade()
                                ? lighting.byFace(quad.direction()) : lighting.up();
                        if (cardinalShade > 0.001f && cardinalShade < 0.999f) {
                            instance.scaleColor(1.0f / cardinalShade);
                        }
                        // Apply darkness modulation
                        for (int v = 0; v < 4; v++) {
                            int color = instance.getColor(v);
                            int r = ARGB.red(color) * darkChannel / 255;
                            int g = ARGB.green(color) * darkChannel / 255;
                            int b = ARGB.blue(color) * darkChannel / 255;
                            instance.setColor(v, ARGB.color(ARGB.alpha(color), r, g, b));
                        }
                        squished.setSprite(quad.materialInfo().sprite());
                        squished.setDirection(quad.direction());
                        squished.putBakedQuad(pose, quad, instance);
                    },
                    0, 0, 0,
                    state.level, state.blockPos, renderBlock, model,
                    renderBlock.getSeed(state.blockPos)
            );
        });
    }

}

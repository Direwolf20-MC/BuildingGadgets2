package com.direwolf20.buildinggadgets2.client.blockentityrenders;

import com.direwolf20.buildinggadgets2.client.renderer.*;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.BitSet;
import java.util.List;

public class RenderBlockBER implements BlockEntityRenderer<RenderBlockBE> {
    public RenderBlockBER(BlockEntityRendererProvider.Context p_173636_) {

    }

    @Override
    public void render(RenderBlockBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        Level level = blockentity.getLevel();
        BlockPos pos = blockentity.getBlockPos();
        int drawSize = blockentity.drawSize;
        float nowScale = (float) (drawSize) / (float) blockentity.getMaxSize();
        float nextScale = (float) (blockentity.nextDrawSize()) / (float) blockentity.getMaxSize();
        float scale = (Mth.lerp(partialTicks, nowScale, nextScale));

        if (scale >= 1.0f)
            scale = 1f;
        if (scale <= 0)
            scale = 0;

        BlockState renderState = blockentity.renderBlock;
        // We're checking here as sometimes the tile can not have a render block as it's yet to be synced
        if (renderState == null)
            return;

        //drawParticle(blockentity, renderState);

        BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel ibakedmodel = blockrendererdispatcher.getBlockModel(renderState);
        boolean isNormalRender = false;
        for (Direction direction : Direction.values()) {
            if (!ibakedmodel.getQuads(renderState, direction, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                isNormalRender = true;
                break;
            }
            if (!ibakedmodel.getQuads(renderState, null, RandomSource.create(), ModelData.EMPTY, null).isEmpty()) {
                isNormalRender = true;
            }
        }
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        ModelBlockRenderer modelBlockRenderer = new ModelBlockRenderer(blockColors);

        if (blockentity.renderType == 0)
            renderGrow(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, blockrendererdispatcher, modelBlockRenderer, isNormalRender);
        else if (blockentity.renderType == 1)
            renderFade(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, modelBlockRenderer, isNormalRender, blockentity);
        else if (blockentity.renderType == 2 || blockentity.renderType == 3 || blockentity.renderType == 4) {
            boolean adjustUV = blockentity.renderType != 2; //3 and 4 get their UV adjusted
            boolean bottomUp = blockentity.renderType == 4; //4 is bottom up, 3 is not, and 2 this doesn't apply
            renderSquished(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, blockrendererdispatcher, modelBlockRenderer, isNormalRender, adjustUV, bottomUp);
        } else if (blockentity.renderType == 5)
            renderSquishedSnap(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, modelBlockRenderer, isNormalRender);
        else
            renderGrow(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, blockrendererdispatcher, modelBlockRenderer, isNormalRender); //Fallback in case something weird happens!

    }

    public void renderGrow(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, BlockRenderDispatcher blockrendererdispatcher, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender) {
        matrixStackIn.pushPose();
        VertexConsumer builder = bufferIn.getBuffer(RenderType.cutout());
        matrixStackIn.translate((1 - scale) / 2, (1 - scale) / 2, (1 - scale) / 2);
        matrixStackIn.scale(scale, scale, scale);

        //TODO Fluids
        if (renderState.getFluidState().isEmpty()) {
            if (isNormalRender)
                modelBlockRenderer.tesselateBlock(level, ibakedmodel, renderState, pos, matrixStackIn, builder, false, RandomSource.create(), renderState.getSeed(pos), combinedOverlayIn, ModelData.EMPTY, null);
            else
                blockrendererdispatcher.renderSingleBlock(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, ModelData.EMPTY, null);
        } else {
            RenderFluidBlock.renderFluidBlock(renderState, level, pos, matrixStackIn, builder, true);
        }

        matrixStackIn.popPose();
    }

    public void renderSquished(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, BlockRenderDispatcher blockrendererdispatcher, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender, boolean adjustUV, boolean bottomUp) {
        matrixStackIn.pushPose();
        VertexConsumer builder = renderState.isSolidRender(level, pos) ? bufferIn.getBuffer(OurRenderTypes.RenderBlockBackface) : bufferIn.getBuffer(OurRenderTypes.RenderBlockFadeNoCull);

        scale = Mth.lerp(scale, 0f, 1f);
        DireVertexConsumerSquished chunksConsumer = new DireVertexConsumerSquished(builder, 0, 0, 0, 1, scale, 1, matrixStackIn.last().pose());
        chunksConsumer.adjustUV = adjustUV;
        chunksConsumer.bottomUp = bottomUp;
        if (!renderState.isSolidRender(level, pos))
            chunksConsumer.adjustUV = false;

        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        RandomSource randomSource = RandomSource.create();
        randomSource.setSeed(renderState.getSeed(pos));
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        if (!renderState.getFluidState().isEmpty()) {
            RenderFluidBlock.renderFluidBlock(renderState, level, pos, matrixStackIn, chunksConsumer, true);
        } else {
            if (isNormalRender) {
                ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> list = ibakedmodel.getQuads(renderState, direction, randomSource, ModelData.EMPTY, null);
                    if (!list.isEmpty()) {
                        TextureAtlasSprite sprite = list.get(0).getSprite();
                        chunksConsumer.setSprite(sprite);
                        chunksConsumer.setDirection(direction);
                        blockpos$mutableblockpos.setWithOffset(pos, direction);
                        modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, chunksConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                    }
                }
                List<BakedQuad> list = ibakedmodel.getQuads(renderState, null, randomSource, ModelData.EMPTY, null);
                if (!list.isEmpty()) {
                    TextureAtlasSprite sprite = list.get(0).getSprite();
                    chunksConsumer.setSprite(sprite);
                    chunksConsumer.setDirection(null);
                    modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, chunksConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                }
            } else {
                MyRenderMethods.renderBESquished(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale);
            }
        }
        matrixStackIn.popPose();
    }

    public void renderFade(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender, RenderBlockBE thisBlockEntity) {
        VertexConsumer builder = renderState.isSolidRender(level, pos) ? bufferIn.getBuffer(OurRenderTypes.RenderBlockFade) : bufferIn.getBuffer(OurRenderTypes.RenderBlockFadeNoCull);
        scale = Mth.lerp(scale, 0.25f, 1f);
        DireVertexConsumer direVertexConsumer = new DireVertexConsumer(builder, scale);
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        RandomSource randomSource = RandomSource.create();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        if (!renderState.getFluidState().isEmpty()) {
            RenderFluidBlock.renderFluidBlock(renderState, level, pos, matrixStackIn, direVertexConsumer, false);
        } else {
            if (isNormalRender) {
                ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
                for (Direction direction : Direction.values()) {
                    randomSource.setSeed(renderState.getSeed(pos));
                    List<BakedQuad> list = ibakedmodel.getQuads(renderState, direction, randomSource, ModelData.EMPTY, null);
                    if (!list.isEmpty()) {
                        blockpos$mutableblockpos.setWithOffset(pos, direction);
                        BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
                        boolean renderAdjacent = true;
                        if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                            if (renderBlockBE.renderBlock != null && renderBlockBE.renderBlock.isSolidRender(level, pos) && Math.abs(thisBlockEntity.drawSize - renderBlockBE.drawSize) < 5 && thisBlockEntity.drawSize < renderBlockBE.drawSize)
                                renderAdjacent = false;
                        }
                        if (renderAdjacent) {
                            modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, direVertexConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                        }
                    }
                }
                randomSource.setSeed(renderState.getSeed(pos));
                List<BakedQuad> list = ibakedmodel.getQuads(renderState, null, randomSource, ModelData.EMPTY, null);
                if (!list.isEmpty()) {
                    modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, direVertexConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                }

            } else {
                MyRenderMethods.renderBETransparent(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale);
            }
        }
    }

    public void renderSquishedSnap(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender) {
        matrixStackIn.pushPose();
        //VertexConsumer builder = renderState.isSolidRender(level, pos) ? bufferIn.getBuffer(OurRenderTypes.RenderBlockBackface) : bufferIn.getBuffer(OurRenderTypes.RenderBlockFadeNoCull);
        VertexConsumer builder = bufferIn.getBuffer(RenderType.cutout());

        float darkness = Mth.lerp(scale, 0.25f, 1f);
        scale = Mth.lerp(scale, 0.75f, 1f);

        DireVertexConsumerSquished chunksConsumer = new DireVertexConsumerSquished(builder, 0, 0, 0, 1, scale, 1, matrixStackIn.last().pose(), darkness, darkness, darkness);
        chunksConsumer.adjustUV = true;
        chunksConsumer.bottomUp = false;
        if (!renderState.isSolidRender(level, pos))
            chunksConsumer.adjustUV = false;

        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        RandomSource randomSource = RandomSource.create();
        randomSource.setSeed(renderState.getSeed(pos));
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        if (!renderState.getFluidState().isEmpty()) {
            RenderFluidBlock.renderFluidBlock(renderState, level, pos, matrixStackIn, chunksConsumer, true);
        } else {
            if (isNormalRender) {
                ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
                for (Direction direction : Direction.values()) {
                    List<BakedQuad> list = ibakedmodel.getQuads(renderState, direction, randomSource, ModelData.EMPTY, null);
                    if (!list.isEmpty()) {
                        TextureAtlasSprite sprite = list.get(0).getSprite();
                        chunksConsumer.setSprite(sprite);
                        chunksConsumer.setDirection(direction);
                        blockpos$mutableblockpos.setWithOffset(pos, direction);
                        modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, chunksConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                    }
                }
                List<BakedQuad> list = ibakedmodel.getQuads(renderState, null, randomSource, ModelData.EMPTY, null);
                if (!list.isEmpty()) {
                    TextureAtlasSprite sprite = list.get(0).getSprite();
                    chunksConsumer.setSprite(sprite);
                    chunksConsumer.setDirection(null);
                    modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, chunksConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                }
            } else {
                MyRenderMethods.renderBESquished(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale);
            }
        }
        matrixStackIn.popPose();
    }

    //Unused
    public void renderSnapFade(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender, RenderBlockBE thisBlockEntity) {
        VertexConsumer builder = renderState.isSolidRender(level, pos) ? bufferIn.getBuffer(OurRenderTypes.RenderBlockFade) : bufferIn.getBuffer(OurRenderTypes.RenderBlockFadeNoCull);
        float rgbScale = Mth.lerp((float) Math.pow(scale, 2), 0.05f, 1);
        float alphaScale = 1f;
        if (scale < 0.5f)
            alphaScale = Mth.lerp((float) Math.pow(scale / 0.5f, 0.25), 0.25f, 1);
        DireVertexConsumer direVertexConsumer = new DireVertexConsumer(builder, alphaScale, rgbScale, rgbScale, rgbScale);
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        RandomSource randomSource = RandomSource.create();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        if (!renderState.getFluidState().isEmpty()) {
            RenderFluidBlock.renderFluidBlock(renderState, level, pos, matrixStackIn, direVertexConsumer, false);
        } else {
            if (isNormalRender) {
                ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
                for (Direction direction : Direction.values()) {
                    randomSource.setSeed(renderState.getSeed(pos));
                    List<BakedQuad> list = ibakedmodel.getQuads(renderState, direction, randomSource, ModelData.EMPTY, null);
                    if (!list.isEmpty()) {
                        blockpos$mutableblockpos.setWithOffset(pos, direction);
                        BlockEntity blockEntity = level.getBlockEntity(pos.relative(direction));
                        boolean renderAdjacent = true;
                        if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                            if (renderBlockBE.renderBlock != null && renderBlockBE.renderBlock.isSolidRender(level, pos) && Math.abs(thisBlockEntity.drawSize - renderBlockBE.drawSize) < 5 && thisBlockEntity.drawSize <= renderBlockBE.drawSize)
                                renderAdjacent = false;
                        }
                        if (renderAdjacent) {
                            modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, direVertexConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                        }
                    }
                }
                randomSource.setSeed(renderState.getSeed(pos));
                List<BakedQuad> list = ibakedmodel.getQuads(renderState, null, randomSource, ModelData.EMPTY, null);
                if (!list.isEmpty()) {
                    modelBlockRenderer.renderModelFaceAO(level, renderState, pos, matrixStackIn, direVertexConsumer, list, afloat, bitset, modelblockrenderer$ambientocclusionface, combinedOverlayIn);
                }
            } else {
                MyRenderMethods.renderBETransparent(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale);
            }
        }
    }
}

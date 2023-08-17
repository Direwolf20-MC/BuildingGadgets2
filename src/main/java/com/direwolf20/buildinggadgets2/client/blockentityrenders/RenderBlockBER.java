package com.direwolf20.buildinggadgets2.client.blockentityrenders;

import com.direwolf20.buildinggadgets2.client.renderer.DireVertexConsumer;
import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
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
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
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
        float nowScale = (float) (drawSize) / (float) RenderBlockBE.getMaxSize();
        ;
        float nextScale = (float) (blockentity.nextDrawSize()) / (float) RenderBlockBE.getMaxSize();
        ;
        float scale = (Mth.lerp(partialTicks, nowScale, nextScale));

        if (scale >= 1.0f)
            scale = 1f;
        if (scale <= 0)
            scale = 0;

        BlockState renderState = blockentity.renderBlock;
        // We're checking here as sometimes the tile can not have a render block as it's yet to be synced
        if (renderState == null)
            return;

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
            renderFade(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, modelBlockRenderer, isNormalRender);
        else
            renderGrow(level, pos, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, scale, renderState, ibakedmodel, blockrendererdispatcher, modelBlockRenderer, isNormalRender); //Fallback in case something weird happens!

    }

    public void renderGrow(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, BlockRenderDispatcher blockrendererdispatcher, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender) {
        matrixStackIn.pushPose();
        VertexConsumer builder = bufferIn.getBuffer(RenderType.cutout());
        matrixStackIn.translate((1 - scale) / 2, (1 - scale) / 2, (1 - scale) / 2);
        matrixStackIn.scale(scale, scale, scale);

        //TODO Fluids
        if (isNormalRender)
            modelBlockRenderer.tesselateBlock(level, ibakedmodel, renderState, pos, matrixStackIn, builder, false, RandomSource.create(), renderState.getSeed(pos), combinedOverlayIn, ModelData.EMPTY, null);
        else
            blockrendererdispatcher.renderSingleBlock(renderState, matrixStackIn, bufferIn, combinedLightsIn, combinedOverlayIn, ModelData.EMPTY, null);


        matrixStackIn.popPose();
    }

    public void renderFade(Level level, BlockPos pos, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn, float scale, BlockState renderState, BakedModel ibakedmodel, ModelBlockRenderer modelBlockRenderer, boolean isNormalRender) {
        VertexConsumer builder = bufferIn.getBuffer(OurRenderTypes.RenderBlock);
        scale = Mth.lerp(scale, 0.0f, 1f);
        DireVertexConsumer direVertexConsumer = new DireVertexConsumer(builder, scale);
        float[] afloat = new float[Direction.values().length * 2];
        BitSet bitset = new BitSet(3);
        RandomSource randomSource = RandomSource.create();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        if (isNormalRender) {
            ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
            for (Direction direction : Direction.values()) {
                randomSource.setSeed(renderState.getSeed(pos));
                List<BakedQuad> list = ibakedmodel.getQuads(renderState, direction, randomSource, ModelData.EMPTY, null);
                if (!list.isEmpty()) {
                    blockpos$mutableblockpos.setWithOffset(pos, direction);
                    if (!(level.getBlockState(pos.relative(direction)).getBlock() instanceof RenderBlock)) {
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

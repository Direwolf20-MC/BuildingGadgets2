package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.BitSet;
import java.util.List;

public class DireModelBlockRenderer extends ModelBlockRenderer {
    public float alpha;
    static final Direction[] DIRECTIONS = Direction.values();

    public DireModelBlockRenderer(BlockColors blockColors) {
        super(blockColors);
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public float getAlpha() {
        return alpha;
    }

    @Override
    public void tesselateWithAO(BlockAndTintGetter level, BakedModel model, BlockState blockState, BlockPos pos, PoseStack poseStack, VertexConsumer builder, boolean checkSides, RandomSource randomSource, long seed, int overlay, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderType) {
        float[] afloat = new float[DIRECTIONS.length * 2];
        BitSet bitset = new BitSet(3);
        ModelBlockRenderer.AmbientOcclusionFace modelblockrenderer$ambientocclusionface = new ModelBlockRenderer.AmbientOcclusionFace();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();

        for (Direction direction : DIRECTIONS) {
            if (level.getBlockState(pos.relative(direction)).getBlock() instanceof RenderBlock) continue;
            randomSource.setSeed(seed);
            List<BakedQuad> list = model.getQuads(blockState, direction, randomSource, modelData, renderType);
            if (!list.isEmpty()) {
                blockpos$mutableblockpos.setWithOffset(pos, direction);
                if (!checkSides || Block.shouldRenderFace(blockState, level, pos, direction, blockpos$mutableblockpos)) {
                    this.renderModelFaceAO(level, blockState, pos, poseStack, builder, list, afloat, bitset, modelblockrenderer$ambientocclusionface, overlay);
                }
            }
        }

        randomSource.setSeed(seed);
        List<BakedQuad> list1 = model.getQuads(blockState, null, randomSource, modelData, renderType);
        if (!list1.isEmpty()) {
            this.renderModelFaceAO(level, blockState, pos, poseStack, builder, list1, afloat, bitset, modelblockrenderer$ambientocclusionface, overlay);
        }

    }

    @Override
    public void putQuadData(BlockAndTintGetter pLevel, BlockState pState, BlockPos pPos, VertexConsumer pConsumer, PoseStack.Pose pPose, BakedQuad pQuad, float pBrightness0, float pBrightness1, float pBrightness2, float pBrightness3, int pLightmap0, int pLightmap1, int pLightmap2, int pLightmap3, int pPackedOverlay) {
        float f;
        float f1;
        float f2;
        if (pQuad.isTinted()) {
            int i = this.blockColors.getColor(pState, pLevel, pPos, pQuad.getTintIndex());
            f = (float) (i >> 16 & 255) / 255.0F;
            f1 = (float) (i >> 8 & 255) / 255.0F;
            f2 = (float) (i & 255) / 255.0F;
        } else {
            f = 1.0F;
            f1 = 1.0F;
            f2 = 1.0F;
        }

        pConsumer.putBulkData(pPose, pQuad, new float[]{pBrightness0, pBrightness1, pBrightness2, pBrightness3}, f, f1, f2, getAlpha(), new int[]{pLightmap0, pLightmap1, pLightmap2, pLightmap3}, pPackedOverlay, true);
    }
}

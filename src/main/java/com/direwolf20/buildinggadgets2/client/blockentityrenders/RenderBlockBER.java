package com.direwolf20.buildinggadgets2.client.blockentityrenders;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class RenderBlockBER implements BlockEntityRenderer<RenderBlockBE> {
    public RenderBlockBER(BlockEntityRendererProvider.Context p_173636_) {

    }

    @Override
    public void render(RenderBlockBE blockentity, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightsIn, int combinedOverlayIn) {
        Level level = blockentity.getLevel();
        BlockPos pos = blockentity.getBlockPos();
        int drawSize = blockentity.drawSize;
        float nowScale = (float) (drawSize) / (float) 40;
        float nextScale = (float) (drawSize + 1) / (float) 40;
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
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();

        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
        matrixStackIn.pushPose();
        //if (breakType == MiningProperties.BreakTypes.SHRINK) {

        ModelBlockRenderer modelBlockRenderer = new ModelBlockRenderer(blockColors);
        matrixStackIn.translate((1 - scale) / 2, (1 - scale) / 2, (1 - scale) / 2);
        matrixStackIn.scale(scale, scale, scale);
        modelBlockRenderer.tesselateBlock(level, ibakedmodel, renderState, pos, matrixStackIn, builder, true, RandomSource.create(), renderState.getSeed(pos), combinedOverlayIn, ModelData.EMPTY, null);

        //} else if (breakType == MiningProperties.BreakTypes.FADE) {

/*
        DireModelBlockRenderer modelBlockRenderer = new DireModelBlockRenderer(blockColors);
        scale = Mth.lerp(scale, 0.0f, 1f);
        modelBlockRenderer.setAlpha(scale);
        modelBlockRenderer.tesselateBlock(level, ibakedmodel, renderState, pos, matrixStackIn, builder, true, RandomSource.create(), renderState.getSeed(pos), combinedOverlayIn, ModelData.EMPTY, null);
*/
        matrixStackIn.popPose();
    }
}

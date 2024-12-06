package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Map;

import static net.minecraft.client.renderer.RenderType.entityTranslucentCull;

public class MyRenderMethods {
    private static float dummyU0 = 0F;
    private static float dummyU1 = 1F;
    private static float dummyV0 = 0F;
    private static float dummyV1 = 1F;
    private static Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();

    public static void renderBETransparent(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, float alpha) {
        MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MultiplyAlphaRenderTypeBuffer(pBufferSource, alpha);
        ItemStack stack = new ItemStack(pState.getBlock());
        IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE, pPoseStack, multiplyAlphaRenderTypeBuffer, pPackedLight, pPackedOverlay);
    }

    public static class MultiplyAlphaRenderTypeBuffer implements MultiBufferSource {
        private final MultiBufferSource inner;
        private final float constantAlpha;

        public MultiplyAlphaRenderTypeBuffer(MultiBufferSource inner, float constantAlpha) {
            this.inner = inner;
            this.constantAlpha = constantAlpha;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            RenderType localType = type;
            if (localType instanceof RenderType.CompositeRenderType) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((RenderStateShard.TextureStateShard) ((RenderType.CompositeRenderType) localType).state.textureState).texture
                        .orElse(InventoryMenu.BLOCK_ATLAS);

                localType = entityTranslucentCull(texture);
            } else if (localType.toString().equals(Sheets.translucentCullBlockSheet().toString())) {
                localType = Sheets.translucentCullBlockSheet();
            }

            return new DireVertexConsumer(this.inner.getBuffer(localType), constantAlpha);
        }
    }

    public static void renderBESquished(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, float alpha) {
        SquishedRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new SquishedRenderTypeBuffer(pBufferSource, alpha, pPoseStack.last().pose());
        ItemStack stack = new ItemStack(pState.getBlock());
        net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE, pPoseStack, multiplyAlphaRenderTypeBuffer, pPackedLight, pPackedOverlay);
    }

    public static class SquishedRenderTypeBuffer implements MultiBufferSource {
        private final MultiBufferSource inner;
        private final float squishAmt;
        private final Matrix4f matrix4f;

        public SquishedRenderTypeBuffer(MultiBufferSource inner, float squishAmt, Matrix4f matrix4f) {
            this.inner = inner;
            this.squishAmt = squishAmt;
            this.matrix4f = matrix4f;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            RenderType localType = type;
            if (localType instanceof RenderType.CompositeRenderType) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((RenderStateShard.TextureStateShard) ((RenderType.CompositeRenderType) localType).state.textureState).texture
                        .orElse(InventoryMenu.BLOCK_ATLAS);

                localType = entityTranslucentCull(texture);
            } else if (localType.toString().equals(Sheets.translucentCullBlockSheet().toString())) {
                localType = Sheets.translucentCullBlockSheet();
            }

            return new DireVertexConsumerSquished(this.inner.getBuffer(localType), 0, 0, 0, 1, squishAmt, 1, matrix4f);
        }
    }

    public static void renderCopy(PoseStack matrix, BlockPos startPos, BlockPos endPos, Color color) {
        if (startPos.equals(GadgetNBT.nullPos) || endPos.equals(GadgetNBT.nullPos))
            return;

        //We want to draw from the starting position to the (ending position)+1
        int x = Math.min(startPos.getX(), endPos.getX()), y = Math.min(startPos.getY(), endPos.getY()), z = Math.min(startPos.getZ(), endPos.getZ());

        int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
        int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
        int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(OurRenderTypes.lines());

        matrix.pushPose();
        Matrix4f matrix4f = matrix.last().pose();
        PoseStack.Pose matrix3f = matrix.last();
        int colorRGB = color.getRGB();

        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, -1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, -1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, -1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, -1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, -1.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, -1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);

        buffer.endBatch(OurRenderTypes.lines()); // @mcp: draw = finish
        matrix.popPose();
    }

    public static void renderBoxSolid(Matrix4f matrix, VertexConsumer builder, BlockPos pos, float r, float g, float b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        renderBoxSolid(matrix, builder, x, y, z, xEnd, yEnd, zEnd, r, g, b, alpha);
    }

    protected static void renderBoxSolid(Matrix4f matrix, VertexConsumer builder, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        float startX = (float) x;
        float startY = (float) y;
        float startZ = (float) z;
        float endX = (float) xEnd;
        float endY = (float) yEnd;
        float endZ = (float) zEnd;

        //down
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);

        //up
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);

        //east
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);

        //west
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);

        //south
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha);

        //north
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha);
    }

    public static void renderLines(PoseStack matrix, BlockPos startPos, BlockPos endPos, Color color, MultiBufferSource buffer) {
        //We want to draw from the starting position to the (ending position)+1
        int x = Math.min(startPos.getX(), endPos.getX()), y = Math.min(startPos.getY(), endPos.getY()), z = Math.min(startPos.getZ(), endPos.getZ());

        int dx = (startPos.getX() > endPos.getX()) ? startPos.getX() + 1 : endPos.getX() + 1;
        int dy = (startPos.getY() > endPos.getY()) ? startPos.getY() + 1 : endPos.getY() + 1;
        int dz = (startPos.getZ() > endPos.getZ()) ? startPos.getZ() + 1 : endPos.getZ() + 1;

        VertexConsumer builder = buffer.getBuffer(OurRenderTypes.lines());

        matrix.pushPose();
        Matrix4f matrix4f = matrix.last().pose();
        PoseStack.Pose matrix3f = matrix.last();
        int colorRGB = color.getRGB();

        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, -1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, -1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, x, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, -1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, -1.0F, 0.0F);
        builder.addVertex(matrix4f, x, y, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, -1.0F);
        builder.addVertex(matrix4f, dx, y, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, -1.0F);
        builder.addVertex(matrix4f, x, dy, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 1.0F, 0.0F, 0.0F);
        builder.addVertex(matrix4f, dx, y, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 1.0F, 0.0F);
        builder.addVertex(matrix4f, dx, dy, z).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);
        builder.addVertex(matrix4f, dx, dy, dz).setColor(colorRGB).setNormal(matrix3f, 0.0F, 0.0F, 1.0F);

        matrix.popPose();
    }

    //This one does not block water
    public static void renderBoxSolid(PoseStack.Pose pose, Matrix4f matrix, MultiBufferSource buffer, double x, double y, double z, double xEnd, double yEnd, double zEnd, float red, float green, float blue, float alpha) {
        VertexConsumer builder = buffer.getBuffer(OurRenderTypes.TRANSPARENT_BOX);

        //careful: mc want's it's vertices to be defined CCW - if you do it the other way around weird cullling issues will arise
        //CCW herby counts as if you were looking at it from the outside
        float startX = (float) x;
        float startY = (float) y;
        float startZ = (float) z;
        float endX = (float) xEnd;
        float endY = (float) yEnd;
        float endZ = (float) zEnd;

        //down
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);

        //up
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);

        //east
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);

        //west
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);

        //south
        builder.addVertex(matrix, endX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, endX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);

        //north
        builder.addVertex(matrix, startX, startY, startZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, startY, endZ).setColor(red, green, blue, alpha).setUv(dummyU0, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, endY, endZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
        builder.addVertex(matrix, startX, endY, startZ).setColor(red, green, blue, alpha).setUv(dummyU1, dummyV0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0F, 0F, 1F);
    }

    public static void renderBoxSolid(PoseStack pose, Matrix4f matrix, MultiBufferSource buffer, BlockPos pos, float r, float g, float b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        renderBoxSolid(pose.last(), matrix, buffer, x, y, z, xEnd, yEnd, zEnd, r, g, b, alpha);
    }

    public static void renderFaceSolid(PoseStack pose, Matrix4f matrix, MultiBufferSource buffer, BlockPos pos, Direction direction, float r, float g, float b, float alpha) {
        double x = pos.getX() - 0.001;
        double y = pos.getY() - 0.001;
        double z = pos.getZ() - 0.001;
        double xEnd = pos.getX() + 1.0015;
        double yEnd = pos.getY() + 1.0015;
        double zEnd = pos.getZ() + 1.0015;

        switch (direction) {
            case DOWN:
                // Draw on the bottom face (y = pos.getY())
                renderBoxSolid(pose.last(), matrix, buffer, x, y - 0.001, z, xEnd, y, zEnd, r, g, b, alpha);
                break;
            case UP:
                // Draw on the top face (y = pos.getY() + 1)
                renderBoxSolid(pose.last(), matrix, buffer, x, yEnd, z, xEnd, yEnd + 0.0015, zEnd, r, g, b, alpha);
                break;
            case NORTH:
                // Draw on the north face (z = pos.getZ())
                renderBoxSolid(pose.last(), matrix, buffer, x, y, z - 0.001, xEnd, yEnd, z, r, g, b, alpha);
                break;
            case SOUTH:
                // Draw on the south face (z = pos.getZ() + 1)
                renderBoxSolid(pose.last(), matrix, buffer, x, y, zEnd, xEnd, yEnd, zEnd + 0.0015, r, g, b, alpha);
                break;
            case WEST:
                // Draw on the west face (x = pos.getX())
                renderBoxSolid(pose.last(), matrix, buffer, x - 0.001, y, z, x, yEnd, zEnd, r, g, b, alpha);
                break;
            case EAST:
                // Draw on the east face (x = pos.getX() + 1)
                renderBoxSolid(pose.last(), matrix, buffer, xEnd, y, z, xEnd + 0.0015, yEnd, zEnd, r, g, b, alpha);
                break;
        }
    }
}

package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.Map;

import static net.minecraft.client.renderer.RenderType.entityTranslucentCull;

public class MyRenderMethods {
    private static Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();

    public static void renderBETransparent(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, float alpha) {
        MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MultiplyAlphaRenderTypeBuffer(pBufferSource, alpha);
        ItemStack stack = new ItemStack(pState.getBlock());
        net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemDisplayContext.NONE, pPoseStack, multiplyAlphaRenderTypeBuffer, pPackedLight, pPackedOverlay);
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
        Matrix3f matrix3f = matrix.last().normal();
        int colorRGB = color.getRGB();

        builder.vertex(matrix4f, x, y, z).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, y, z).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, dy, z).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, y, z).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix4f, x, y, dz).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, z).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, z).color(colorRGB).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, dy, z).color(colorRGB).normal(matrix3f, -1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, dy, z).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix4f, x, dy, dz).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix4f, x, dy, dz).color(colorRGB).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, y, dz).color(colorRGB).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, x, y, dz).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, y, dz).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, y, dz).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        builder.vertex(matrix4f, dx, y, z).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, -1.0F).endVertex();
        builder.vertex(matrix4f, x, dy, dz).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, dz).color(colorRGB).normal(matrix3f, 1.0F, 0.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, y, dz).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, dz).color(colorRGB).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, z).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();
        builder.vertex(matrix4f, dx, dy, dz).color(colorRGB).normal(matrix3f, 0.0F, 0.0F, 1.0F).endVertex();

        buffer.endBatch(OurRenderTypes.lines()); // @mcp: draw = finish
        matrix.popPose();
    }
}

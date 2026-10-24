package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.List;

import static net.minecraft.client.renderer.LevelRenderer.getLightCoords;

public class RenderFluidBlock {
    private static BakedQuad createQuad(List<Vec3> vectors, Material.Baked material, Direction face, float u1, float u2, float v1, float v2) {
        QuadBakingVertexConsumer quadBaker = new QuadBakingVertexConsumer();
        Vec3 normal = Vec3.atLowerCornerOf(face.getUnitVec3i());

        putVertex(quadBaker, normal, vectors.get(0).x, vectors.get(0).y, vectors.get(0).z, u1, v1, material, face);
        putVertex(quadBaker, normal, vectors.get(1).x, vectors.get(1).y, vectors.get(1).z, u1, v2, material, face);
        putVertex(quadBaker, normal, vectors.get(2).x, vectors.get(2).y, vectors.get(2).z, u2, v2, material, face);
        putVertex(quadBaker, normal, vectors.get(3).x, vectors.get(3).y, vectors.get(3).z, u2, v1, material, face);

        return quadBaker.bakeQuad();
    }

    private static void putVertex(QuadBakingVertexConsumer quadBaker, Vec3 normal,
                                  double x, double y, double z, float u, float v, Material.Baked material, Direction face) {
        quadBaker.addVertex((float) x, (float) y, (float) z);
        quadBaker.setNormal((float) normal.x, (float) normal.y, (float) normal.z);
        quadBaker.setColor(-1);
        quadBaker.setUv(u, v);
        quadBaker.setSprite(material);
        quadBaker.setDirection(face);
    }

    public static void renderFluidBlock(BlockState renderState, Level level, BlockPos pos, PoseStack matrixStackIn, VertexConsumer builder, boolean renderAdjacent) {
        renderFluidBlock(renderState, level, pos, matrixStackIn.last(), builder, renderAdjacent, 1f);
    }

    public static void renderFluidBlock(BlockState renderState, Level level, BlockPos pos, PoseStack.Pose pose, VertexConsumer builder, boolean renderAdjacent) {
        renderFluidBlock(renderState, level, pos, pose, builder, renderAdjacent, 1f);
    }

    public static void renderFluidBlock(BlockState renderState, Level level, BlockPos pos, PoseStack.Pose pose, VertexConsumer builder, boolean renderAdjacent, float alpha) {
        if (renderState.getFluidState().isEmpty()) return;
        FluidState fluidState = renderState.getFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        Material.Baked material = model.stillMaterial();
        TextureAtlasSprite sprite = material.sprite();
        // BG2 client renderers are only ever called with a ClientLevel, which implements BlockAndTintGetter.
        FluidTintSource tintSource = model.fluidTintSource();
        int color = tintSource != null ? tintSource.colorInWorld(fluidState, renderState, (BlockAndTintGetter) level, pos) : -1;
        int brightness = getLightCoords(level, pos);

        float minU = sprite.getU0();
        float minV = sprite.getV0();
        float maxU = sprite.getU1();
        float maxV = sprite.getV1();

        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        float x2 = 1.0f;
        float z2 = 1.0f;
        float height = 0.875f; //14/16

        QuadInstance instance = new QuadInstance();
        instance.setColor(alpha < 1f ? ARGB.color(alpha, color) : color);
        instance.setLightCoords(brightness);

        BakedQuad quad;
        //DOWN
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.DOWN)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x, y, z2), new Vec3(x, y, z), new Vec3(x2, y, z), new Vec3(x2, y, z2)), material, Direction.DOWN, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
        //UP
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.UP)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x, height, z), new Vec3(x, height, z2), new Vec3(x2, height, z2), new Vec3(x2, height, z)), material, Direction.UP, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
        //NORTH
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.NORTH)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x2, height, z), new Vec3(x2, y, z), new Vec3(x, y, z), new Vec3(x, height, z)), material, Direction.NORTH, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
        //SOUTH
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.SOUTH)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x, height, z2), new Vec3(x, y, z2), new Vec3(x2, y, z2), new Vec3(x2, height, z2)), material, Direction.SOUTH, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
        //WEST
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.WEST)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x, height, z), new Vec3(x, y, z), new Vec3(x, y, z2), new Vec3(x, height, z2)), material, Direction.WEST, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
        //EAST
        if (renderAdjacent || !(level.getBlockState(pos.relative(Direction.EAST)).getBlock() instanceof RenderBlock)) {
            quad = createQuad(ImmutableList.of(new Vec3(x2, height, z2), new Vec3(x2, y, z2), new Vec3(x2, y, z), new Vec3(x2, height, z)), material, Direction.EAST, minU, maxU, minV, maxV);
            builder.putBakedQuad(pose, quad, instance);
        }
    }
}

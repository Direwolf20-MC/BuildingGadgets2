package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VBORenderer {
    private static VertexBuffer vertexBuffer;
    private static ArrayList<StatePos> statePosCache;

    public static void buildRender(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(heldItem);
        if (renderBlockState.isAir()) return;

        var mode = GadgetNBT.getMode(heldItem);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        ArrayList<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAt.getBlockPos(), renderBlockState);
        /*for (int i = 1; i < 4; i++) {
            buildList.add(new StatePos(Blocks.COBBLESTONE.defaultBlockState(), new BlockPos(1, 0, i)));
            buildList.add(new StatePos(Blocks.OAK_LOG.defaultBlockState(), new BlockPos(2, 0, i)));
        }*/
        if (buildList.equals(statePosCache))
            return;

        System.out.println("I'm Building!");
        Level level = player.level;
        //player.displayClientMessage(Component.literal("Rebuilding Render due to change." + level.getGameTime()), false);
        statePosCache = buildList;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        vertexBuffer = new VertexBuffer();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        PoseStack matrix = new PoseStack(); //Create a new matrix stack for use in the buffer building process
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        for (StatePos pos : buildList.stream().filter(pos -> pos.isModelRender).toList()) {

            //matrix.translate(-0.005f, -0.005f, -0.005f);
            //matrix.scale(1.01f, 1.01f, 1.01f);
            BakedModel ibakedmodel = dispatcher.getBlockModel(pos.state);
            BlockColors blockColors = Minecraft.getInstance().getBlockColors();
            int color = blockColors.getColor(pos.state, level, pos.pos, 0);
            float f = (float) (color >> 16 & 255) / 255.0F;
            float f1 = (float) (color >> 8 & 255) / 255.0F;
            float f2 = (float) (color & 255) / 255.0F;
            matrix.pushPose();
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            for (Direction direction : Direction.values()) {
                Optional<StatePos> statePosOptional = buildList.stream().filter(obj -> obj.pos.equals(pos.pos.relative(direction))).findFirst();
                if (statePosOptional.isEmpty() || !(statePosOptional.get().state.isSolidRender(level, statePosOptional.get().pos)))
                    MyRenderMethods.renderModelBrightnessColorQuads(matrix.last(), bufferbuilder, f, f1, f2, 0.5f, ibakedmodel.getQuads(pos.state, direction, RandomSource.create(), ModelData.EMPTY, RenderType.cutout()), 15728640, 655360);
            }
            MyRenderMethods.renderModelBrightnessColorQuads(matrix.last(), bufferbuilder, f, f1, f2, 0.5f, ibakedmodel.getQuads(pos.state, null, RandomSource.create(), ModelData.EMPTY, RenderType.cutout()), 15728640, 655360);
            matrix.popPose();

        }

        vertexBuffer.bind();
        vertexBuffer.upload(bufferbuilder.end());
        VertexBuffer.unbind();
    }

    public static void drawRender(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {
        if (vertexBuffer == null) {
            return;
        }
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(heldItem);
        if (renderBlockState.isAir()) return;

        // TODO: This might need caching (and invalidating when the mode changes)
        var mode = GadgetNBT.getMode(heldItem);
        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);
        List<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAt.getBlockPos(), renderBlockState);
        if (buildList.isEmpty()) return;
        Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        PoseStack matrix = evt.getPoseStack();
        matrix.pushPose();
        matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
        matrix.translate(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ());
        vertexBuffer.bind();
        vertexBuffer.drawWithShader(matrix.last().pose(), new Matrix4f(evt.getProjectionMatrix()), RenderSystem.getShader());
        VertexBuffer.unbind();
        matrix.popPose();

        MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        for (StatePos pos : buildList.stream().filter(pos -> !pos.isModelRender).toList()) {
            matrix.pushPose();
            matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            matrix.translate(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ());
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            MyRenderMethods.renderBETransparent(renderBlockState, matrix, buffersource, 15728640, 655360, 0.5f);
            matrix.popPose();
        }
        /*final Camera oldActiveRenderInfo = Minecraft.getInstance().getBlockEntityRenderDispatcher().camera;
        for (StatePos pos : buildList) {
            matrix.pushPose();
            matrix.translate(-projectedView.x(), -projectedView.y(), -projectedView.z());
            matrix.translate(lookingAt.getBlockPos().getX(), lookingAt.getBlockPos().getY(), lookingAt.getBlockPos().getZ());
            matrix.translate(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ());
            BlockEntity blockEntity = ((EntityBlock)pos.state.getBlock()).newBlockEntity(pos.pos, pos.state);
            blockEntity.setLevel(player.level);
            blockEntity.setBlockState(pos.state);
            BlockEntityRenderDispatcher blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            blockEntityRenderDispatcher.camera = new Camera();
            blockEntityRenderDispatcher.camera.setPosition(new Vec3(pos.pos.getX(), pos.pos.getY(), pos.pos.getZ()));
            blockEntityRenderDispatcher.render(blockEntity, 0, matrix, buffersource);
            blockEntityRenderDispatcher.camera = oldActiveRenderInfo;
            //MyRenderMethods.renderBETransparent(renderBlockState, matrix, buffersource, 15728640, 655360, 0.5f);
            matrix.popPose();
        }*/
    }
}

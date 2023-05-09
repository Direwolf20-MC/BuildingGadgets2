package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.modes.BuildToMe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

public class Renderer {
    public static void render(RenderLevelStageEvent evt, Player player, ItemStack heldItem) {

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, heldItem);

        BlockState state = Blocks.AIR.defaultBlockState();
        //Optional<List<BlockPos>> anchor = getAnchor(heldItem); //TODO Anchor

        BlockState startBlock = player.level.getBlockState(lookingAt.getBlockPos());
        if ((player.level.isEmptyBlock(lookingAt.getBlockPos()))) // && !anchor.isPresent()) || startBlock == DEFAULT_EFFECT_BLOCK )
            return;

        //BlockData data = getToolBlock(heldItem);
        BlockState renderBlockState = GadgetNBT.getGadgetBlockState(heldItem);
        if (renderBlockState == Blocks.AIR.defaultBlockState())
            return;


        // Sort them on a new line for readability
//        coordinates = SortingHelper.Blocks.byDistance(coordinates, player);

        //Prepare the fake world -- using a fake world lets us render things properly, like fences connecting.
        //getBuilderWorld().setWorldAndState(player.level, renderBlockState, coordinates);

        Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        //Save the current position that is being rendered (I think)
        PoseStack matrix = evt.getPoseStack();
        matrix.pushPose();
        matrix.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        BuildToMe buildToMe = new BuildToMe();
        List<BlockPos> buildList = buildToMe.collect(lookingAt.getDirection(), player, lookingAt.getBlockPos());
        for (BlockPos coordinate : buildList) {
            matrix.pushPose();
            matrix.translate(coordinate.getX(), coordinate.getY(), coordinate.getZ());
            /*if( this.isExchanger ) {
                matrix.translate(-0.0005f, -0.0005f, -0.0005f);
                matrix.scale(1.001f, 1.001f, 1.001f);
            }*/

            // todo: add back from 1.16 port
//            if (getBuilderWorld().getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) { //Get the block state in the fake world
//                try {
            state = renderBlockState;
//                } catch (Exception ignored) {}
//            }


            //OurRenderTypes.MultiplyAlphaRenderTypeBuffer mutatedBuffer = new OurRenderTypes.MultiplyAlphaRenderTypeBuffer(Minecraft.getInstance().renderBuffers().bufferSource(), .55f);
            try {
                dispatcher.renderSingleBlock(
                        state, matrix, buffer, 15728640, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid()
                );
            } catch (Exception ignored) {
            } // I'm sure if this is an issue someone will report it

            //Move the render position back to where it was
            matrix.popPose();
            RenderSystem.disableDepthTest();
            buffer.endBatch(); // @mcp: finish (mcp) = draw (yarn)
        }
    }
}

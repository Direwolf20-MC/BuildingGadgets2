package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.client.renderer.DestructionRenderer;
import com.direwolf20.buildinggadgets2.client.renderer.MyRenderMethods;
import com.direwolf20.buildinggadgets2.client.renderer.OurRenderTypes;
import com.direwolf20.buildinggadgets2.client.renderer.VBORenderer;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.awt.*;

public class RenderLevelLast {
    @SubscribeEvent
    static void renderWorldLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack heldItem = BaseGadget.getGadget(player);
        if (heldItem.isEmpty())
            return;

        if (heldItem.getItem() instanceof GadgetDestruction) {
            DestructionRenderer.render(evt, player, heldItem);
            BlockPos anchorPos = GadgetNBT.getAnchorPos(heldItem);
            if (anchorPos != null && !anchorPos.equals(GadgetNBT.nullPos))
                renderSelectedBlock(evt, anchorPos);
        } else {
            VBORenderer.buildRender(evt, player, heldItem);
            VBORenderer.drawRender(evt, player, heldItem);
            BlockPos anchorPos = GadgetNBT.getAnchorPos(heldItem);
            if (anchorPos != null && !anchorPos.equals(GadgetNBT.nullPos))
                renderSelectedBlock(evt, anchorPos);
        }
    }

    public static void renderSelectedBlock(RenderLevelStageEvent event, BlockPos pos) {
        final Minecraft mc = Minecraft.getInstance();

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x(), -view.y(), -view.z());

        matrix.pushPose();
        matrix.translate(pos.getX(), pos.getY(), pos.getZ());
        matrix.translate(-0.005f, -0.005f, -0.005f);
        matrix.scale(1.01f, 1.01f, 1.01f);
        //matrix.mulPose(Axis.YP.rotationDegrees(-90.0F));

        Matrix4f positionMatrix = matrix.last().pose();
        MyRenderMethods.renderBoxSolid(matrix, positionMatrix, buffer, BlockPos.ZERO, 0, 1, 0, 0.25f);
        //MyRenderMethods.renderFaceSolid(matrix, positionMatrix, buffer, BlockPos.ZERO, direction, 0, 0, 1, 0.25f);
        MyRenderMethods.renderLines(matrix, BlockPos.ZERO, BlockPos.ZERO, Color.WHITE, buffer);
        matrix.popPose();

        matrix.popPose();
        buffer.endBatch(OurRenderTypes.TRANSPARENT_BOX);
    }
}

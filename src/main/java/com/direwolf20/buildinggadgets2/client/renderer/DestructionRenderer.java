package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public class DestructionRenderer {
    public static void render(RenderLevelStageEvent evt, Player player, ItemStack gadget) {
        //if (!GadgetDestruction.getOverlay(gadget)) //TODO
        //    return;

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
        Level level = player.level();
        BlockPos anchor = GadgetNBT.getAnchorPos(gadget);
        Direction anchorSide = GadgetNBT.getAnchorSide(gadget);

        if (level.getBlockState(VectorHelper.getLookingAt(player, gadget).getBlockPos()) == Blocks.AIR.defaultBlockState() && anchor == null)
            return;

        BlockPos startBlock = (anchor == GadgetNBT.nullPos) ? lookingAt.getBlockPos() : anchor;
        Direction facing = (anchorSide == null) ? lookingAt.getDirection() : anchorSide;
        if (level.getBlockState(startBlock) == Registration.RenderBlock.get().defaultBlockState())
            return;

        Vec3 playerPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();

        PoseStack stack = evt.getPoseStack();
        stack.pushPose();
        stack.translate(-playerPos.x(), -playerPos.y(), -playerPos.z());

        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer builder = buffer.getBuffer(OurRenderTypes.MissingBlockOverlay);
        final int[] counter = {BuildingUtils.getEnergyStored(gadget)};
        final int energyCost = BuildingUtils.getEnergyCost(gadget);
        //Todo More Efficient for more FPS, consider a VBO?
        GadgetUtils.getDestructionArea(level, startBlock, facing, player, gadget)
                .forEach(pos -> {
                    if (counter[0] >= energyCost || player.isCreative())
                        MyRenderMethods.renderBoxSolid(stack.last().pose(), builder, pos.pos, 1, 0, 0, 0.35f);
                    counter[0] -= energyCost;
                });

        stack.popPose();
        //RenderSystem.disableDepthTest();
        buffer.endBatch(); // @mcp: draw = finish
    }
}

package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.client.renderer.DestructionRenderer;
import com.direwolf20.buildinggadgets2.client.renderer.VBORenderer;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetDestruction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

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
        } else {
            VBORenderer.buildRender(evt, player, heldItem);
            VBORenderer.drawRender(evt, player, heldItem);
        }
    }
}

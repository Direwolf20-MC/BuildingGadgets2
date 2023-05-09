package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.common.items.Base_Gadget;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderLevelLast {
    @SubscribeEvent
    static void renderWorldLastEvent(RenderLevelStageEvent evt) {
        if (evt.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;

        ItemStack heldItem = Base_Gadget.getGadget(player);
        if (heldItem.isEmpty())
            return;

        VBORenderer.buildRender(evt, player, heldItem);
        VBORenderer.drawRender(evt, player, heldItem);
    }
}

package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.screen.DestructionGUI;
import com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets2.common.network.data.AnchorPayload;
import com.direwolf20.buildinggadgets2.common.network.data.RangeChangePayload;
import com.direwolf20.buildinggadgets2.common.network.data.UndoPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = BuildingGadgets2Api.MOD_ID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        ItemStack tool = BaseGadget.getGadget(mc.player);
        if (tool.isEmpty())
            return;

        KeyMapping mode = KeyBindings.menuSettings;
        if (!(mc.screen instanceof ModeRadialMenu) && mode.consumeClick() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
            if (tool.getItem() instanceof GadgetDestruction)
                mc.setScreen(new DestructionGUI(tool, true));
            else
                mc.setScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.undo.consumeClick()) {
            PacketDistributor.sendToServer(new UndoPayload());
        } else if (KeyBindings.anchor.consumeClick()) {
            PacketDistributor.sendToServer(new AnchorPayload());
        } else if (KeyBindings.range.consumeClick()) {
            int oldRange = GadgetNBT.getToolRange(tool);
            int newRange = oldRange + 1 > 15 ? 1 : oldRange + 1;
            PacketDistributor.sendToServer(new RangeChangePayload(newRange));
        }
    }
}

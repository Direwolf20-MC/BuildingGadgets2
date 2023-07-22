package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketAnchor;
import com.direwolf20.buildinggadgets2.common.network.packets.PacketUndo;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = BuildingGadgets2.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == Phase.START)
            return;

        /*if (KeyBindings.materialList.consumeClick()) {
            GuiMod.MATERIAL_LIST.openScreen(mc.player);
            return;
        }*/

        ItemStack tool = BaseGadget.getGadget(mc.player);
        if (tool.isEmpty())
            return;

        KeyMapping mode = KeyBindings.menuSettings;
        if (!(mc.screen instanceof ModeRadialMenu) && mode.consumeClick() && ((mode.getKeyModifier() == KeyModifier.NONE
                && KeyModifier.getActiveModifier() == KeyModifier.NONE) || mode.getKeyModifier() != KeyModifier.NONE)) {
            mc.setScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.undo.consumeClick()) {
            PacketHandler.sendToServer(new PacketUndo());
        } else if (KeyBindings.anchor.consumeClick()) {
            PacketHandler.sendToServer(new PacketAnchor());
        } /*else if (KeyBindings.rotateMirror.consumeClick()) {
            PacketHandler.sendToServer(new PacketRotateMirror());
        } else if (KeyBindings.undo.consumeClick()) {
            PacketHandler.sendToServer(new PacketUndo());
        } else if (KeyBindings.anchor.consumeClick()) {
            PacketHandler.sendToServer(new PacketAnchor());
        } else if (KeyBindings.fuzzy.consumeClick()) {
            PacketHandler.sendToServer(new PacketToggleFuzzy());
        } else if (KeyBindings.connectedArea.consumeClick()) {
            PacketHandler.sendToServer(new PacketToggleConnectedArea());
        }*/
    }
}

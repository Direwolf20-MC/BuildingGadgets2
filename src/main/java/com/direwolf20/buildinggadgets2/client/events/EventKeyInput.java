package com.direwolf20.buildinggadgets2.client.events;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.screen.DestructionGUI;
import com.direwolf20.buildinggadgets2.client.screen.ModeRadialMenu;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.items.GadgetDestruction;
import com.direwolf20.buildinggadgets2.common.network.data.GadgetActionPayload;
import com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction.ActionGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = BuildingGadgets2.MODID, value = Dist.CLIENT)
public class EventKeyInput {

    @SubscribeEvent
    public static void handleEventInput(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || event.phase == TickEvent.Phase.START)
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
            if (tool.getItem() instanceof GadgetDestruction)
                mc.setScreen(new DestructionGUI(tool, true));
            else
                mc.setScreen(new ModeRadialMenu(tool));
        } else if (KeyBindings.undo.consumeClick()) {
            PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.UNDO));
        } else if (KeyBindings.anchor.consumeClick()) {
            PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.ANCHOR));
        } else if (KeyBindings.range.consumeClick()) {
            int oldRange = GadgetNBT.getToolRange(tool);
            int newRange = oldRange + 1 > 15 ? 1 : oldRange + 1;
            PacketDistributor.SERVER.noArg().send(new GadgetActionPayload(ActionGadget.RANGE_CHANGE, Util.make(new CompoundTag(), c -> c.putInt("range", newRange))));
        }/*else if (KeyBindings.rotateMirror.consumeClick()) {
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

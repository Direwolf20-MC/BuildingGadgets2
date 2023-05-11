package com.direwolf20.buildinggadgets2.client;


import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.network.GadgetModeSwitchPacket;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final KeyMapping MODE_SWITCH = new KeyMapping(BuildingGadgets2.MODID + ".keymapping.mode-switch", InputConstants.KEY_M, BuildingGadgets2.MODID + "_category");

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MODE_SWITCH);
    }

    public static void onClientInput(InputEvent.Key event) {
        if (MODE_SWITCH.consumeClick()) {
            PacketHandler.sendToServer(new GadgetModeSwitchPacket(new ResourceLocation("x:x"), true));
        }
    }
}

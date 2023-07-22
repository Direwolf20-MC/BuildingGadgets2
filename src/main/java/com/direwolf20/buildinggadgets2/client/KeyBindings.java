package com.direwolf20.buildinggadgets2.client;


import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyBindings.class);
    private static final KeyConflictContextGadget CONFLICT_CONTEXT_GADGET = new KeyConflictContextGadget();

    private static final List<KeyMapping> keyMappings = new ArrayList<>();

    public static KeyMapping menuSettings = createBinding("settings_menu", GLFW.GLFW_KEY_G);
    public static KeyMapping undo = createBinding("undo", GLFW.GLFW_KEY_U);
    public static KeyMapping anchor = createBinding("anchor", GLFW.GLFW_KEY_H);

    private static KeyMapping createBinding(String name, int key) {
        KeyMapping keyBinding = new KeyMapping(getKey(name), CONFLICT_CONTEXT_GADGET, InputConstants.Type.KEYSYM.getOrCreate(key), getKey("category"));
        keyMappings.add(keyBinding);
        return keyBinding;
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        LOGGER.debug("Registering {} keybinding for {}", keyMappings.size(), BuildingGadgets2.MODID);
        keyMappings.forEach(event::register);
    }

    private static String getKey(String name) {
        return String.join(".", "key", BuildingGadgets2.MODID, name);
    }

    public static void onClientInput(InputEvent.Key event) {
        /*if (menuSettings.consumeClick()) {
            PacketHandler.sendToServer(new GadgetModeSwitchPacket(new ResourceLocation("x:x"), true));
        }*/
    }

    public static class KeyConflictContextGadget implements IKeyConflictContext {
        @Override
        public boolean isActive() {
            Player player = Minecraft.getInstance().player;
            return !KeyConflictContext.GUI.isActive() && player != null
                    && (!BaseGadget.getGadget(player).isEmpty()
                    /*|| (player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TemplateItem || player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof TemplateItem)*/);
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || other == KeyConflictContext.IN_GAME;
        }
    }
}

package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.client.KeyBindings;
import com.direwolf20.buildinggadgets2.client.blockentityrenders.RenderBlockBER;
import com.direwolf20.buildinggadgets2.client.events.EventKeyInput;
import com.direwolf20.buildinggadgets2.client.events.RenderLevelLast;
import com.direwolf20.buildinggadgets2.client.screen.TemplateManagerGUI;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = BuildingGadgets2Api.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ClientSetup {
    public static void init(final FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(KeyBindings::onClientInput);

        //Register our Render Events Class
        NeoForge.EVENT_BUS.register(RenderLevelLast.class);
        NeoForge.EVENT_BUS.register(EventKeyInput.class);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.TEMPLATE_MANAGER_CONTAINER.get(), TemplateManagerGUI::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        //Register Block Entity Renders
        event.registerBlockEntityRenderer(Registration.RENDER_BLOCK_BLOCK_ENTITY.get(), RenderBlockBER::new);
    }

    @SubscribeEvent
    public static void registerTooltipFactory(RegisterClientTooltipComponentFactoriesEvent event) {
        //LOGGER.debug("Registering custom tooltip component factories for {}", Reference.MODID);
        //event.register(EventTooltip.CopyPasteTooltipComponent.Data.class, EventTooltip.CopyPasteTooltipComponent::new);
    }
}

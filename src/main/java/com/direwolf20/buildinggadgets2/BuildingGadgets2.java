package com.direwolf20.buildinggadgets2;

import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.capabilities.EnergyStorageItemstack;
import com.direwolf20.buildinggadgets2.common.commands.BuildingGadgets2Commands;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.setup.ClientSetup;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.setup.ModSetup;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BuildingGadgets2.MODID)
public class BuildingGadgets2 {
    public static final String MODID = "buildinggadgets2";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BuildingGadgets2(IEventBus eventBus) {
        // Register the deferred registry
        Registration.init(eventBus);
        Config.register();

        eventBus.addListener(ModSetup::init);
        ModSetup.TABS.register(eventBus);
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(PacketHandler::registerNetworking);
        NeoForge.EVENT_BUS.addListener(BuildingGadgets2Commands::registerCommands);

        if (FMLLoader.getDist().isClient()) {
            eventBus.addListener(ClientSetup::init);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.EnergyStorage.ITEM,
                (itemStack, context) -> new EnergyStorageItemstack(((BaseGadget) itemStack.getItem()).getEnergyMax(), itemStack),
                Registration.Building_Gadget.get(),
                Registration.Exchanging_Gadget.get(),
                Registration.CopyPaste_Gadget.get(),
                Registration.CutPaste_Gadget.get(),
                Registration.Destruction_Gadget.get()
        );
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, be, side) -> ((TemplateManagerBE) be).itemHandler,
                // blocks to register for
                Registration.TemplateManager.get());
    }
}

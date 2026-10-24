package com.direwolf20.buildinggadgets2;

import com.direwolf20.buildinggadgets2.common.blockentities.TemplateManagerBE;
import com.direwolf20.buildinggadgets2.common.commands.BuildingGadgets2Commands;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.setup.BG2DataComponents;
import com.direwolf20.buildinggadgets2.setup.ClientSetup;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.setup.ModSetup;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(BuildingGadgets2.MODID)
public class BuildingGadgets2 {
    public static final String MODID = "buildinggadgets2";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BuildingGadgets2(IEventBus eventBus, ModContainer container) {
        // Register the deferred registry
        Registration.init(eventBus);
        Config.register(container);

        eventBus.addListener(ModSetup::init);
        ModSetup.TABS.register(eventBus);
        eventBus.addListener(this::registerCapabilities);
        eventBus.addListener(PacketHandler::registerNetworking);
        NeoForge.EVENT_BUS.addListener(BuildingGadgets2Commands::registerCommands);

        if (FMLEnvironment.getDist().isClient()) {
            eventBus.addListener(ClientSetup::init);
        }
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.Energy.ITEM,
                (itemStack, itemAccess) -> new ItemAccessEnergyHandler(
                        itemAccess != null ? itemAccess : ItemAccess.forStack(itemStack),
                        BG2DataComponents.FORGE_ENERGY.get(),
                        ((BaseGadget) itemStack.getItem()).getEnergyMax()),
                Registration.Building_Gadget.get(),
                Registration.Exchanging_Gadget.get(),
                Registration.CopyPaste_Gadget.get(),
                Registration.CutPaste_Gadget.get(),
                Registration.Destruction_Gadget.get()
        );
        event.registerBlock(Capabilities.Item.BLOCK,
                (level, pos, state, be, side) -> ((TemplateManagerBE) be).itemHandler,
                Registration.TemplateManager.get());
    }
}

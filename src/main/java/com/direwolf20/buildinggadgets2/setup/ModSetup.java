package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.integration.AE2Integration;
import com.direwolf20.buildinggadgets2.integration.AE2Methods;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        NeoForge.EVENT_BUS.register(ServerTickHandler.class);
        if (AE2Integration.isLoaded()) {
            AE2Methods.registerItems();
        }
    }

    public static final String TAB_NAME = "buildinggadgets2";
    public static DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BuildingGadgets2Api.MOD_ID);
    public static DeferredHolder<CreativeModeTab, CreativeModeTab> TAB_BUILDINGGADGETS2 = TABS.register(TAB_NAME, () -> CreativeModeTab.builder()
            .title(Component.literal("Building Gadgets 2"))
            .icon(() -> new ItemStack(Registration.BUILDING_GADGET.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((featureFlags, output) -> {
                Registration.ITEMS.getEntries().forEach(e -> {
                    Item item = e.get();
                    output.accept(item);
                });
            })
            .build());
}

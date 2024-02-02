package com.direwolf20.buildinggadgets2;

import com.direwolf20.buildinggadgets2.setup.ClientSetup;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.setup.ModSetup;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(BuildingGadgets2.MODID)
public class BuildingGadgets2 {
    public static final String MODID = "buildinggadgets2";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BuildingGadgets2() {
        // Register the deferred registry
        Registration.init();
        Config.register();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(ModSetup::init);
        ModSetup.TABS.register(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(ClientSetup::init));

    }
}

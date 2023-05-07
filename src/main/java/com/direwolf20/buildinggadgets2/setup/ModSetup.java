package com.direwolf20.buildinggadgets2.setup;

import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ModSetup {
    public static void init(final FMLCommonSetupEvent event) {
        PacketHandler.register();
        //MinecraftForge.EVENT_BUS.register(ServerTickHandler.class);
    }

    public static final String TAB_NAME = "buildinggadgets2";
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(TAB_NAME) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Registration.Building_Gadget.get());
        }
    };
}

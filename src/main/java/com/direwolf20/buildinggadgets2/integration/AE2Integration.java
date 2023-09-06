package com.direwolf20.buildinggadgets2.integration;

import appeng.api.features.GridLinkables;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraftforge.fml.ModList;

public class AE2Integration {
    private static final String ID = "ae2";

    public AE2Integration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ID);
    }

    public static void registerItems() {
        GridLinkables.register(Registration.Building_Gadget.get(), BaseGadget.LINKABLE_HANDLER);
        GridLinkables.register(Registration.Exchanging_Gadget.get(), BaseGadget.LINKABLE_HANDLER);
        GridLinkables.register(Registration.CopyPaste_Gadget.get(), BaseGadget.LINKABLE_HANDLER);
    }
}

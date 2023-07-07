package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import static com.direwolf20.buildinggadgets2.setup.ModSetup.TAB_NAME;

public class BG2LanguageProvider extends LanguageProvider {
    public BG2LanguageProvider(PackOutput output, String locale) {
        super(output, BuildingGadgets2.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TAB_NAME, "BuildingGadgets2");

        //Blocks
        add(Registration.RenderBlock.get(), "Render Block (DO NOT USE)");

        //Items
        add(Registration.Building_Gadget.get(), "Building Gadget");

        add(BuildingGadgets2.MODID + ".keymapping.mode-switch", "Switch Modes");

        //add("screen.laserio.extractamt", "Transfer Amount");

        //add("message.laserio.wrenchrange", "Connection exceeds maximum range of %d");

        //Tooltips
        //add("laserio.tooltip.item.show_settings", "Hold shift to show settings");

        //add("", "");
    }
}

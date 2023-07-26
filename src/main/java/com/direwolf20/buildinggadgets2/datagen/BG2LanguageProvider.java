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
        add(Registration.Exchanging_Gadget.get(), "Exchanging Gadget");
        add(Registration.CopyPaste_Gadget.get(), "Copy Paste Gadget");
        add(Registration.CutPaste_Gadget.get(), "Cut Paste Gadget");

        add(BuildingGadgets2.MODID + ".keymapping.mode-switch", "Switch Modes");


        //Modes
        add("buildinggadgets2.modes.vertical_wall", "Vertical Wall");
        add("buildinggadgets2.modes.build_to_me", "Build to Me");
        add("buildinggadgets2.modes.vertical_column", "Vertical Column");
        add("buildinggadgets2.modes.surface", "Surface");
        add("buildinggadgets2.modes.copy", "Copy");
        add("buildinggadgets2.modes.cut", "Cut");
        add("buildinggadgets2.modes.paste", "Paste");

        //GUI
        add("buildinggadgets2.gui.range", "Range");
        add("buildinggadgets2.screen.confirm", "Confirm");
        add("buildinggadgets2.screen.close", "Close");
        add("buildinggadgets2.screen.clear", "Clear");
        add("buildinggadgets2.screen.absolutecoords", "Absolute");
        add("buildinggadgets2.screen.relativecoords", "Relative");
        add("buildinggadgets2.screen.start", "Start");
        add("buildinggadgets2.screen.end", "End");
        add("buildinggadgets2.screen.copyheading", "Adjust selection");
        add("buildinggadgets2.screen.pasteheading", "Adjust placement");
        add("buildinggadgets2.screen.copysubheading", "Use absolute mode to switch to block coordinates");


        //Radial Menu
        add("buildinggadgets2.radialmenu.fuzzy", "Fuzzy");
        add("buildinggadgets2.radialmenu.cut", "Cut");
        add("buildinggadgets2.radialmenu.connected_area", "Connected Area");
        add("buildinggadgets2.radialmenu.undo", "Undo");
        add("buildinggadgets2.radialmenu.anchor", "Anchor");
        add("buildinggadgets2.radialmenu.copypastemenu", "Settings Menu");

        //Messages to Player
        add("buildinggadgets2.messages.invalidblock", "Invalid Block");
    }
}

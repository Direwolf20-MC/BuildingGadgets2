package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
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
        add(Registration.TemplateManager.get(), "Template Manager");

        //Items
        add(Registration.Building_Gadget.get(), "Building Gadget");
        add(Registration.Exchanging_Gadget.get(), "Exchanging Gadget");
        add(Registration.CopyPaste_Gadget.get(), "Copy Paste Gadget");
        add(Registration.CutPaste_Gadget.get(), "Cut Paste Gadget");
        add(Registration.Destruction_Gadget.get(), "Destruction Gadget");
        add(Registration.Template.get(), "Template");

        //Misc
        add(BuildingGadgets2.MODID + ".keymapping.mode-switch", "Switch Modes");
        add("key.buildinggadgets2.category", "Building Gadgets 2");
        add("key.buildinggadgets2.anchor", "Anchor");
        add("key.buildinggadgets2.range", "Range");
        add("key.buildinggadgets2.settings_menu", "Settings Menu");
        add("key.buildinggadgets2.undo", "Undo");


        //Tooltips
        add("buildinggadgets2.tooltips.holdshift", "Hold Shift for details");
        add("buildinggadgets2.tooltips.energy", "Energy: %d/%d");
        add("buildinggadgets2.tooltips.mode", "Mode: %s");
        add("buildinggadgets2.tooltips.range", "Range: %d");
        add("buildinggadgets2.tooltips.blockstate", "Block: %s");

        //Modes
        add("buildinggadgets2.modes.vertical_wall", "Vertical Wall");
        add("buildinggadgets2.modes.grid", "Grid");
        add("buildinggadgets2.modes.horizontal_wall", "Horizontal Wall");
        add("buildinggadgets2.modes.stairs", "Stairs");
        add("buildinggadgets2.modes.build_to_me", "Build to Me");
        add("buildinggadgets2.modes.vertical_column", "Vertical Column");
        add("buildinggadgets2.modes.horizontal_row", "Horizontal Row");
        add("buildinggadgets2.modes.surface", "Surface");
        add("buildinggadgets2.modes.copy", "Copy");
        add("buildinggadgets2.modes.cut", "Cut");
        add("buildinggadgets2.modes.paste", "Paste");

        //GUI
        add("buildinggadgets2.gui.range", "Range");
        add("buildinggadgets2.screen.confirm", "Confirm");
        add("buildinggadgets2.screen.close", "Close");
        add("buildinggadgets2.screen.cancel", "Cancel");
        add("buildinggadgets2.screen.revert", "Revert");
        add("buildinggadgets2.screen.clear", "Clear");
        add("buildinggadgets2.screen.affecttiles", "Affect Block Entities");
        add("buildinggadgets2.screen.absolutecoords", "Absolute");
        add("buildinggadgets2.screen.relativecoords", "Relative");
        add("buildinggadgets2.screen.start", "Start");
        add("buildinggadgets2.screen.end", "End");
        add("buildinggadgets2.screen.paste_replace", "Replace Blocks");
        add("buildinggadgets2.screen.copyheading", "Adjust selection");
        add("buildinggadgets2.screen.pasteheading", "Adjust placement");
        add("buildinggadgets2.screen.copysubheading", "Use absolute mode to switch to block coordinates");
        add("buildinggadgets2.screen.destructiontoolarge", "Destruction Area too large");
        add("buildinggadgets2.screen.depth", "Depth");
        add("buildinggadgets2.screen.down", "Down");
        add("buildinggadgets2.screen.up", "Up");
        add("buildinggadgets2.screen.left", "Left");
        add("buildinggadgets2.screen.right", "Right");
        add("buildinggadgets2.screen.sortaz", "Sort A-Z");
        add("buildinggadgets2.screen.sortza", "Sort Z-A");
        add("buildinggadgets2.screen.requiredasc", "Required Ascending");
        add("buildinggadgets2.screen.requireddesc", "Required Descending");
        add("buildinggadgets2.screen.missingasc", "Missing Ascending");
        add("buildinggadgets2.screen.missingdesc", "Missing Descending");
        add("buildinggadgets2.screen.templateplaceholder", "Template name");
        add("buildinggadgets2.screen.namefieldtext", "name?");

        //Buttons
        add("buildinggadgets2.buttons.save", "Save");
        add("buildinggadgets2.buttons.load", "Load");
        add("buildinggadgets2.buttons.copy", "Copy");
        add("buildinggadgets2.buttons.paste", "Paste");

        //Radial Menu
        add("buildinggadgets2.radialmenu.fuzzy", "Fuzzy");
        add("buildinggadgets2.screen.placeatop", "Place On Top");
        add("buildinggadgets2.radialmenu.cut", "Cut");
        add("buildinggadgets2.radialmenu.connected_area", "Connected Area");
        add("buildinggadgets2.radialmenu.undo", "Undo");
        add("buildinggadgets2.radialmenu.anchor", "Anchor");
        add("buildinggadgets2.radialmenu.copypastemenu", "Settings Menu");
        add("buildinggadgets2.radialmenu.raytracefluids", "Raytrace Fluids");
        add("buildinggadgets2.radialmenu.materiallist", "Materials List");

        //Messages to Player
        add("buildinggadgets2.messages.invalidblock", "Invalid Block");
        add("buildinggadgets2.messages.anchorcleared", "Anchor Cleared");
        add("buildinggadgets2.messages.anchorset", "Anchor Set to: ");
        add("buildinggadgets2.messages.overwritecut", "Tool already has cut data stored - click again to OVERWRITE this data");
        add("buildinggadgets2.messages.copyblocks", "Copied %d blocks");
        add("buildinggadgets2.messages.cutblocks", "Cut %d blocks");
        add("buildinggadgets2.messages.range_set", "Range set to: %d");
        add("buildinggadgets2.messages.relativepaste", "Relative Paste set to: [%s]");
        add("buildinggadgets2.messages.areatoolarge", "Area too large! Max size is: %d. Size was: %d");
        add("buildinggadgets2.messages.notenoughenergy", "Not enough energy for cut, need: %d. Have: %d");
        add("buildinggadgets2.messages.undofailedunloaded", "Undo Failed: Chunks are not loaded (Too far away): %s");
    }
}

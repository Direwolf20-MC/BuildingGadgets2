package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

import static com.direwolf20.buildinggadgets2.setup.ModSetup.TAB_NAME;

public class BG2LanguageProvider extends LanguageProvider {
    public BG2LanguageProvider(PackOutput output, String locale) {
        super(output, BuildingGadgets2Api.MOD_ID, locale);
    }

    @Override
    protected void addTranslations() {
        add("itemGroup." + TAB_NAME, "BuildingGadgets2");

        //Blocks
        add(Registration.RENDER_BLOCK.get(), "Render Block (DO NOT USE)");
        add(Registration.TEMPLATE_MANAGER.get(), "Template Manager");

        //Items
        add(Registration.BUILDING_GADGET.get(), "Building Gadget");
        add(Registration.EXCHANGING_GADGET.get(), "Exchanging Gadget");
        add(Registration.COPY_PASTE_GADGET.get(), "Copy Paste Gadget");
        add(Registration.CUT_PASTE_GADGET.get(), "Cut Paste Gadget");
        add(Registration.DESTRUCTION_GADGET.get(), "Destruction Gadget");
        add(Registration.TEMPLATE.get(), "Template");
        add(Registration.REDPRINT.get(), "Redprint");

        //Misc
        add(BuildingGadgets2Api.MOD_ID + ".keymapping.mode-switch", "Switch Modes");
        add("buildinggadgets2.grow", "Grow");
        add("buildinggadgets2.fade", "Fade");
        add("buildinggadgets2.squish", "Squish");
        add("buildinggadgets2.riseup", "Rise Up");
        add("buildinggadgets2.growup", "Grow Up");
        add("buildinggadgets2.snap", "The SNAP!");
        add("buildinggadgets2.voidwarning", "WARNING: Voids Drops of removed blocks!!");
        add("buildinggadgets2.templatename", "Name: %s");

        //Keys
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
        add("buildinggadgets2.tooltips.boundto", "Bound to: %s:%s");


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
        add("buildinggadgets2.screen.paste_replace", "Replace Blocks (WARNING: Voids Drops)");
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
        add("buildinggadgets2.screen.invalidjson", "Invalid Pasted JSON");

        //Buttons
        add("buildinggadgets2.buttons.save", "Save");
        add("buildinggadgets2.buttons.load", "Load");
        add("buildinggadgets2.buttons.copy", "Copy");
        add("buildinggadgets2.buttons.paste", "Paste");
        add("buildinggadgets2.buttons.render", "Render");
        add("buildinggadgets2.buttons.materials", "Materials");

        //Radial Menu
        add("buildinggadgets2.radialmenu.fuzzy", "Fuzzy");
        add("buildinggadgets2.screen.placeatop", "Place On Top");
        add("buildinggadgets2.radialmenu.cut", "Cut");
        add("buildinggadgets2.radialmenu.connected_area", "Connected Area");
        add("buildinggadgets2.radialmenu.undo", "Undo");
        add("buildinggadgets2.radialmenu.bind", "Bind Inventory");
        add("buildinggadgets2.radialmenu.anchor", "Anchor");
        add("buildinggadgets2.radialmenu.copypastemenu", "Settings Menu");
        add("buildinggadgets2.radialmenu.raytracefluids", "Raytrace Fluids");
        add("buildinggadgets2.radialmenu.materiallist", "Materials List");
        add("buildinggadgets2.radialmenu.rotate", "Rotate");

        //Messages to Player
        add("buildinggadgets2.messages.invalidblock", "Invalid Block");
        add("buildinggadgets2.messages.anchorcleared", "Anchor Cleared");
        add("buildinggadgets2.messages.anchorset", "Anchor Set to: ");
        add("buildinggadgets2.messages.overwritecut", "Tool already has cut data stored - click again to OVERWRITE this data");
        add("buildinggadgets2.messages.copyblocks", "Copied %d blocks");
        add("buildinggadgets2.messages.cutblocks", "Cut %d blocks");
        add("buildinggadgets2.messages.range_set", "Range set to: %d");
        add("buildinggadgets2.messages.render_set", "Render Type set to: %s");
        add("buildinggadgets2.messages.relativepaste", "Relative Paste set to: [%s]");
        add("buildinggadgets2.messages.areatoolarge", "Area too large! Max size is: %d. Size was: %d");
        add("buildinggadgets2.messages.axistoolarge", "%s Axis too large! Max size is: %d. Size was: %d");
        add("buildinggadgets2.messages.cutinprogress", "Cut in progress - Please Wait!");
        add("buildinggadgets2.messages.outofpower", "Gadget out of power!");
        add("buildinggadgets2.messages.notenoughenergy", "Not enough energy for cut, need: %d. Have: %d");
        add("buildinggadgets2.messages.undofailedunloaded", "Undo Failed: Chunks are not loaded (Too far away): %s");
        add("buildinggadgets2.messages.bindfailed", "Bind Failed: Invalid block");
        add("buildinggadgets2.messages.bindsuccess", "Bind Succeeded to: %s");
        add("buildinggadgets2.messages.bindremoved", "Bind Removed");
        add("buildinggadgets2.messages.copycoordsfailed", "Copying Coordinates Failed.");
        add("buildinggadgets2.messages.namerequired", "Name Required for Redprints. Please try again.");
        add("buildinggadgets2.messages.namealreadyexists", "This name is already in use, either delete it with commands or give a new name.");
        add("buildinggadgets2.messages.redprintremovesuccess", "Successfully deleted redprint: %s");
        add("buildinggadgets2.messages.redprintremovefail", "Failed to delete redprint: %s");
        add("buildinggadgets2.messages.redprintgivefail", "Failed to give redprint %s to %s");
    }
}

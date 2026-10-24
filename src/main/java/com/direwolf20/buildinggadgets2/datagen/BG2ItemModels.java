package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public final class BG2ItemModels {

    private final ItemModelGenerators itemModels;

    public BG2ItemModels(ItemModelGenerators itemModels) {
        this.itemModels = itemModels;
    }

    public void run() {
        itemModels.generateFlatItem(Registration.Building_Gadget.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Exchanging_Gadget.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.CopyPaste_Gadget.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.CutPaste_Gadget.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Destruction_Gadget.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Template.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(Registration.Redprint.get(), ModelTemplates.FLAT_ITEM);
        // template_manager BlockItem is auto-resolved to the block model via
        // ModelProvider.finalizeAndValidate's BlockItem fallback.
    }
}

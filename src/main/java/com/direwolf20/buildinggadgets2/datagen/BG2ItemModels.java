package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class BG2ItemModels extends ItemModelProvider {
    public BG2ItemModels(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BuildingGadgets2Api.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Block Items
        //withExistingParent(Registration.RenderBlock_ITEM.getId().getPath(), modLoc("block/render_block"));
        //withExistingParent(Registration.LaserNode_ITEM.getId().getPath(), modLoc("block/laser_node"));

        //Item items
        //singleTexture(Registration.Building_Gadget.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/gadget_building"));
    }
}

package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BG2ItemModels extends ItemModelProvider {
    public BG2ItemModels(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BuildingGadgets2.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //Block Items
        //withExistingParent(Registration.LaserConnector_ITEM.getId().getPath(), modLoc("block/laser_connector"));
        //withExistingParent(Registration.LaserNode_ITEM.getId().getPath(), modLoc("block/laser_node"));

        //Item items
        //singleTexture(Registration.Building_Gadget.getId().getPath(), mcLoc("item/generated"), "layer0", modLoc("item/gadget_building"));
    }
}
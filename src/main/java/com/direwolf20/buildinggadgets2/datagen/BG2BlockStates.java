package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BG2BlockStates extends BlockStateProvider {
    public BG2BlockStates(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, BuildingGadgets2.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //simpleBlock(Registration.LaserConnector.get());
        //simpleBlock(Registration.LaserNode.get(), models().getExistingFile(modLoc("block/laser_node")));
    }
}

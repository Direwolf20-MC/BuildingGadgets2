package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BG2BlockTags extends BlockTagsProvider {

    public BG2BlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, BuildingGadgets2.MODID, helper);
    }

    @Override
    protected void addTags() {
        /*tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Registration.LaserNode.get())
                .add(Registration.LaserConnector.get());*/
    }

    @Override
    public String getName() {
        return "BuildingGadgets2 Tags";
    }
}

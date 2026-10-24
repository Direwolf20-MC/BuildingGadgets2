package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

public class BG2ModelProvider extends ModelProvider {
    public BG2ModelProvider(PackOutput output) {
        super(output, BuildingGadgets2.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        new BG2BlockStates(blockModels).run();
        new BG2ItemModels(itemModels).run();
    }
}

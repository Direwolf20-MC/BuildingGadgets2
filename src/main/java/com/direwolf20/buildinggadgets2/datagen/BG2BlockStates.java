package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;


public class BG2BlockStates extends BlockStateProvider {
    public BG2BlockStates(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingGadgets2Api.MOD_ID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //models().cubeAll(ForgeRegistries.BLOCKS.getKey(Registration.RenderBlock.get()).getPath(), blockTexture(Registration.RenderBlock.get())).renderType("cutout");
        simpleBlock(Registration.RENDER_BLOCK.get(), models().cubeAll(Registration.RENDER_BLOCK.getId().getPath(), blockTexture(Registration.RENDER_BLOCK.get())).renderType("cutout"));
        //simpleBlock(Registration.LaserNode.get(), models().getExistingFile(modLoc("block/laser_node")));
    }
}

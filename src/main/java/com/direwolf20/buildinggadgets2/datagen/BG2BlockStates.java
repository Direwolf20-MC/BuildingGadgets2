package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BG2BlockStates extends BlockStateProvider {
    public BG2BlockStates(PackOutput output, ExistingFileHelper helper) {
        super(output, BuildingGadgets2.MODID, helper);
    }

    @Override
    protected void registerStatesAndModels() {
        //models().cubeAll(ForgeRegistries.BLOCKS.getKey(Registration.RenderBlock.get()).getPath(), blockTexture(Registration.RenderBlock.get())).renderType("cutout");
        simpleBlock(Registration.RenderBlock.get(), models().cubeAll(Registration.RenderBlock.getId().getPath(), blockTexture(Registration.RenderBlock.get())).renderType("cutout"));
        //simpleBlock(Registration.LaserNode.get(), models().getExistingFile(modLoc("block/laser_node")));
    }
}

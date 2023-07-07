package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BG2BlockTags extends BlockTagsProvider {

    public BG2BlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, BuildingGadgets2.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        /*tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(Registration.LaserNode.get())
                .add(Registration.LaserConnector.get());*/
    }

    @Override
    public String getName() {
        return "BuildingGadgets2 Tags";
    }
}

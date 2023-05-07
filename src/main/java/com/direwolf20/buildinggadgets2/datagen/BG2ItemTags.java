package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;

public class BG2ItemTags extends ItemTagsProvider {
    //public static final TagKey<Item> WRENCHES = forgeTag("wrenches");
    //public static final TagKey<Item> TOOLS_WRENCH = forgeTag("tools/wrench");

    private static TagKey<Item> forgeTag(String name) {
        return ItemTags.create(new ResourceLocation("forge", name));
    }

    public BG2ItemTags(DataGenerator generator, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(generator, blockTags, BuildingGadgets2.MODID, helper);
    }

    @Override
    protected void addTags() {
        /*tag(WRENCHES)
                .add(Registration.Laser_Wrench.get());
        tag(TOOLS_WRENCH)
                .add(Registration.Laser_Wrench.get());*/
    }

    @Override
    public String getName() {
        return "BuildingGadgets2 Tags";
    }
}
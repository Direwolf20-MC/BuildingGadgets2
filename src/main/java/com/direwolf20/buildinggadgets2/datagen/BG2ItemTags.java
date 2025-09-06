package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BG2ItemTags extends ItemTagsProvider {
    private static TagKey<Item> forgeTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("forge", name));
    }

    public BG2ItemTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider, BlockTagsProvider blockTags, ExistingFileHelper helper) {
        super(packOutput, lookupProvider, blockTags.contentsGetter(), BuildingGadgets2Api.MOD_ID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ItemTags.MINING_LOOT_ENCHANTABLE)
                .add(Registration.EXCHANGING_GADGET.get());
    }

    @Override
    public String getName() {
        return "BuildingGadgets2 Item Tags";
    }
}

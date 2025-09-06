package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class BG2Recipes extends RecipeProvider {


    public BG2Recipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput consumer) {
        //Gadgets
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.BUILDING_GADGET.get())
                .pattern("iri")
                .pattern("drd")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.EXCHANGING_GADGET.get())
                .pattern("iri")
                .pattern("dld")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.COPY_PASTE_GADGET.get())
                .pattern("iri")
                .pattern("ere")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.GEMS_EMERALD)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_emerald", InventoryChangeTrigger.TriggerInstance.hasItems(Items.EMERALD))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.DESTRUCTION_GADGET.get())
                .pattern("iri")
                .pattern("ere")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_ender_pearl", InventoryChangeTrigger.TriggerInstance.hasItems(Items.ENDER_PEARL))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CUT_PASTE_GADGET.get())
                .pattern("iri")
                .pattern("srs")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Items.SHEARS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_shear", InventoryChangeTrigger.TriggerInstance.hasItems(Items.SHEARS))
                .save(consumer);

        //Blocks
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.TEMPLATE_MANAGER.get())
                .pattern("iri")
                .pattern("prp")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('p', Items.PAPER)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_paper", InventoryChangeTrigger.TriggerInstance.hasItems(Items.PAPER))
                .save(consumer);
    }
}

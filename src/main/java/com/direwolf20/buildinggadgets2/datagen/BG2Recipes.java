package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class BG2Recipes extends RecipeProvider {

    protected BG2Recipes(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // Gadgets
        shaped(RecipeCategory.MISC, Registration.Building_Gadget.get())
                .pattern("iri")
                .pattern("drd")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, Registration.Exchanging_Gadget.get())
                .pattern("iri")
                .pattern("dld")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('d', Tags.Items.GEMS_DIAMOND)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.MISC, Registration.CopyPaste_Gadget.get())
                .pattern("iri")
                .pattern("ere")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.GEMS_EMERALD)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_emerald", has(Items.EMERALD))
                .save(output);

        shaped(RecipeCategory.MISC, Registration.Destruction_Gadget.get())
                .pattern("iri")
                .pattern("ere")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                .save(output);

        shaped(RecipeCategory.MISC, Registration.CutPaste_Gadget.get())
                .pattern("iri")
                .pattern("srs")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Items.SHEARS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_shear", has(Items.SHEARS))
                .save(output);

        // Blocks
        shaped(RecipeCategory.MISC, Registration.TemplateManager.get())
                .pattern("iri")
                .pattern("prp")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('p', Items.PAPER)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new BG2Recipes(registries, output);
        }

        @Override
        public String getName() {
            return "BuildingGadgets2 Recipes";
        }
    }
}

package com.direwolf20.buildinggadgets2.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.function.Consumer;

public class BG2Recipes extends RecipeProvider {

    public BG2Recipes(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        //Crafting Components
        /*ShapedRecipeBuilder.shaped(Registration.Logic_Chip_Raw.get(), 4)
                .pattern("rgr")
                .pattern("cqc")
                .pattern("rgr")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('q', Tags.Items.STORAGE_BLOCKS_QUARTZ)
                .define('g', Tags.Items.NUGGETS_GOLD)
                .define('c', Items.CLAY_BALL)
                .group("laserio")
                .unlockedBy("has_quartz", InventoryChangeTrigger.TriggerInstance.hasItems(Items.QUARTZ_BLOCK))
                .save(consumer);
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(Registration.Logic_Chip_Raw.get()),
                        Registration.Logic_Chip.get(), 1.0f, 100)
                .unlockedBy("has_raw_chip", inventoryTrigger(ItemPredicate.Builder.item().of(Registration.Logic_Chip_Raw.get()).build()))
                .save(consumer);*/
    }
}
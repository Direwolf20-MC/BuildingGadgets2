package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import java.util.function.Consumer;

public class BG2Recipes extends RecipeProvider {

    public BG2Recipes(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        //Gadgets
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Building_Gadget.get())
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Exchanging_Gadget.get())
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CopyPaste_Gadget.get())
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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.Destruction_Gadget.get())
                .pattern("iri")
                .pattern("ere")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('e', Tags.Items.ENDER_PEARLS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Registration.CutPaste_Gadget.get())
                .pattern("iri")
                .pattern("srs")
                .pattern("ili")
                .define('r', Tags.Items.DUSTS_REDSTONE)
                .define('i', Tags.Items.INGOTS_IRON)
                .define('s', Tags.Items.SHEARS)
                .define('l', Tags.Items.GEMS_LAPIS)
                .group("buildinggadgets2")
                .unlockedBy("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(consumer);
    }
}
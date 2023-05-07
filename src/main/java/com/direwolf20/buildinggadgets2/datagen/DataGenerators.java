package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BuildingGadgets2.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        //if (event.includeServer()) {
        generator.addProvider(event.includeServer(), new BG2Recipes(generator));
        generator.addProvider(event.includeServer(), new BG2LootTables(generator));
        BG2BlockTags blockTags = new BG2BlockTags(generator, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), blockTags);
        BG2ItemTags itemTags = new BG2ItemTags(generator, blockTags, event.getExistingFileHelper());
        generator.addProvider(event.includeServer(), itemTags);
        //generator.addProvider(new BG2ItemTags(generator, blockTags, event.getExistingFileHelper()));
        //}
        //if (event.includeClient()) {
        generator.addProvider(event.includeClient(), new BG2BlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new BG2ItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new BG2LanguageProvider(generator, "en_us"));
        //}
    }
}

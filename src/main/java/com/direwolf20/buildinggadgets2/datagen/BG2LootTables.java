package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.stream.Collectors;

public class BG2LootTables extends VanillaBlockLoot {

    @Override
    protected void generate() {
        add(Registration.RenderBlock.get(), noDrop());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ForgeRegistries.BLOCKS.getEntries().stream()
                .filter(e -> e.getKey().location().getNamespace().equals(BuildingGadgets2.MODID))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
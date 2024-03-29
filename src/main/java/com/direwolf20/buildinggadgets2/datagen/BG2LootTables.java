package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.stream.Collectors;

public class BG2LootTables extends VanillaBlockLoot {

    @Override
    protected void generate() {
        add(Registration.RenderBlock.get(), noDrop());
        dropSelf(Registration.TemplateManager.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Registration.BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
    }
}

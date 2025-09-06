package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.stream.Collectors;

public class BG2LootTables extends VanillaBlockLoot {

    public BG2LootTables(HolderLookup.Provider p_344962_) {
        super(p_344962_);
    }

    @Override
    protected void generate() {
        add(Registration.RENDER_BLOCK.get(), noDrop());
        dropSelf(Registration.TEMPLATE_MANAGER.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Registration.BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
    }
}

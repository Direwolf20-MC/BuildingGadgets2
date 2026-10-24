package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class BG2Tags {
    public static final TagKey<Block> BG2DENY = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "deny"));

    private BG2Tags() {}
}

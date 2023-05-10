package com.direwolf20.buildinggadgets2.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class RenderBlock extends Block {
    private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.NONE).nonSolid().build();

    public RenderBlock() {
        super(Block.Properties.of(EFFECT_BLOCK_MATERIAL)
                .strength(20f)
                .noOcclusion());
    }

    //These 2 methods required to not cast a shadow under the block - since its transparent we don't want that...
    @Override
    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }
}
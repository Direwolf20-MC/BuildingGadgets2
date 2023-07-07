package com.direwolf20.buildinggadgets2.common.blocks;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class RenderBlock extends Block implements EntityBlock {
    //private static final Material EFFECT_BLOCK_MATERIAL = new Material.Builder(MaterialColor.NONE).nonSolid().build();

    public RenderBlock() {
        super(Block.Properties.of()
                .strength(20f)
                .dynamicShape()
                .noOcclusion());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (lvl, pos, blockState, t) -> {
                if (t instanceof RenderBlockBE tile) {
                    tile.tickClient();
                }
            };
        }
        return (lvl, pos, blockState, t) -> {
            if (t instanceof RenderBlockBE tile) {
                tile.tickServer();
            }
        };
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RenderBlockBE(pos, state);
    }


    //These 2 methods after the shadows under the block
    @Override
    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        if (p_48741_.getBlockEntity(p_48742_) instanceof RenderBlockBE renderBlockBE && renderBlockBE.renderBlock != null) {
            return renderBlockBE.renderBlock.propagatesSkylightDown(p_48741_, p_48742_);
        }
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        if (p_48732_.getBlockEntity(p_48733_) instanceof RenderBlockBE renderBlockBE && renderBlockBE.renderBlock != null) {
            float blockShade = renderBlockBE.renderBlock.getShadeBrightness(p_48732_, p_48733_);
            return blockShade;
        }
        return 1.0F;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof RenderBlockBE renderBlockBE && renderBlockBE.renderBlock != null) {
            return renderBlockBE.renderBlock.getOcclusionShape(pLevel, pPos);
        }
        return super.getOcclusionShape(pState, pLevel, pPos);
    }

    @Override
    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }
}
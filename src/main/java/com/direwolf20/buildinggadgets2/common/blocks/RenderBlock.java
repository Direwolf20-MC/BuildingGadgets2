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
    public RenderBlock(Properties properties) {
        super(properties);
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


    //These 2 methods affect the shadows under the block
    @Override
    public boolean propagatesSkylightDown(BlockState pState) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState pState) {
        return super.getOcclusionShape(pState);
    }

    @Override
    @Deprecated
    public boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }
}
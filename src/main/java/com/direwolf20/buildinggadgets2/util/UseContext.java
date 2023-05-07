package com.direwolf20.buildinggadgets2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class UseContext {
    private final Level world;
    private final BlockState setState;
    private final BlockPos startPos;
    private final Direction hitSide;
    private final Player player;

    private final boolean isFuzzy;
    private final boolean placeOnTop;
    private final int range;
    private final boolean rayTraceFluid;
    private final boolean isConnected;

    public UseContext(Level world, Player player, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean placeOnTop, boolean isConnected) {
        this.world = world;
        this.setState = setState;
        this.startPos = startPos;
        this.player = player;

        this.range = GadgetNBT.getToolRange(gadget);
        this.isFuzzy = GadgetNBT.getFuzzy(gadget);
        this.rayTraceFluid = GadgetNBT.shouldRayTraceFluid(gadget);
        this.hitSide = hitSide;

        this.isConnected = isConnected;
        this.placeOnTop = placeOnTop;
    }

    public UseContext(Level world, Player player, BlockState setState, BlockPos startPos, ItemStack gadget, Direction hitSide, boolean isConnected) {
        this(world, player, setState, startPos, gadget, hitSide, false, isConnected);
    }

    public BlockPlaceContext createBlockUseContext() {
        return new BlockPlaceContext(
                new UseOnContext(
                        player,
                        InteractionHand.MAIN_HAND,
                        VectorHelper.getLookingAt(player, this.rayTraceFluid)
                )
        );
    }

    public boolean isConnected() {
        return isConnected;
    }

    public BlockState getWorldState(BlockPos pos) {
        return world.getBlockState(pos);
    }

    public Level getWorld() {
        return world;
    }

    public BlockState getSetState() {
        return setState;
    }

    public boolean isFuzzy() {
        return isFuzzy;
    }

    public boolean isRayTraceFluid() {
        return rayTraceFluid;
    }

    public boolean isPlaceOnTop() {
        return placeOnTop;
    }

    public int getRange() {
        return range;
    }

    public BlockPos getStartPos() {
        return startPos;
    }

    public Direction getHitSide() {
        return this.hitSide;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "UseContext{" +
                "world=" + world +
                ", setState=" + setState +
                ", startPos=" + startPos +
                ", hitSide=" + hitSide +
                ", isFuzzy=" + isFuzzy +
                ", placeOnTop=" + placeOnTop +
                ", range=" + range +
                ", rayTraceFluid=" + rayTraceFluid +
                '}';
    }
}

package com.direwolf20.buildinggadgets2.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class EventHelpers {
    public static boolean mayExchange(ServerLevel level, Player player, BlockPos pos) { 
        return canBreak(level, player, pos) && mayPlace(level, player, pos);
    }

    public static boolean mayPlace(ServerLevel level, Player player, @NotNull BlockPos pos) { 
        BlockSnapshot blockSnapshot = BlockSnapshot.create(level.dimension(), level, pos);
        BlockState placedAgainst = Objects.requireNonNull(blockSnapshot.getLevel()).getBlockState(blockSnapshot.getPos());
        BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot, placedAgainst, player);
        boolean placeAllowed = !MinecraftForge.EVENT_BUS.post(event);
        return level.mayInteract(player,pos) && placeAllowed;
    }

    public static boolean canBreak(Level level, Player player, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
        boolean breakAllowed = !MinecraftForge.EVENT_BUS.post(event);
        return level.mayInteract(player,pos) && breakAllowed;
    }
}

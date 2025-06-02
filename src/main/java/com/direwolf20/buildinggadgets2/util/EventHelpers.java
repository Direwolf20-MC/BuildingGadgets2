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
    
	 public static boolean mayExchange(Level level, Player player, BlockPos pos) {
		 return level.mayInteract(player,pos) && _canBreak(level, player, pos) && _mayPlace(level, player, pos);
	 }

	 public static boolean mayPlace(Level level, Player player, @NotNull BlockPos pos){
		 return level.mayInteract(player,pos) && _mayPlace(level, player, pos);
	 }
	 public static boolean canBreak(Level level, Player player, @NotNull BlockPos pos){
		 return level.mayInteract(player,pos) && _canBreak(level, player, pos);
	 }

	 // I lie to forge because as far as im concerned WHAT im placing off of
	 // does not matter we are just checking for permission
	 private static boolean _mayPlace(Level level, Player player, @NotNull BlockPos pos) {
		 BlockSnapshot blockSnapshot = BlockSnapshot.create(level.dimension(), level, pos);
		 BlockState placedAgainst = Objects.requireNonNull(blockSnapshot.getLevel()).getBlockState(blockSnapshot.getPos());
		 BlockEvent.EntityPlaceEvent event = new BlockEvent.EntityPlaceEvent(blockSnapshot, placedAgainst, player);
         return !MinecraftForge.EVENT_BUS.post(event);
	 }
	 
	 private static boolean _canBreak(Level level, Player player, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
         return !MinecraftForge.EVENT_BUS.post(event);
	}
}

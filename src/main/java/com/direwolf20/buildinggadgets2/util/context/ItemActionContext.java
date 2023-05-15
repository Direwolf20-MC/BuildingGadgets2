package com.direwolf20.buildinggadgets2.util.context;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Simple context to share common required fields for a item action.
 */
public record ItemActionContext(
        BlockPos pos,
        BlockHitResult hitResult,
        Player player,
        Level level,
        InteractionHand hand,
        ItemStack stack
) {
}

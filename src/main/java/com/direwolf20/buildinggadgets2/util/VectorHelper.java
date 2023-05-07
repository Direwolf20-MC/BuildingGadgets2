package com.direwolf20.buildinggadgets2.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * @implNote The main reason behind this is so we have control over the RayTraceContext,
 * this means that we can use COLLIDER so it traces through non-collidable objects
 */
public class VectorHelper {
    public static BlockHitResult getLookingAt(Player player, ItemStack tool) {
        return getLookingAt(player, GadgetNBT.shouldRayTraceFluid(tool) ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
    }

    public static BlockHitResult getLookingAt(Player player, boolean shouldRayTrace) {
        return getLookingAt(player, shouldRayTrace ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE);
    }

    public static BlockHitResult getLookingAt(Player player, ClipContext.Fluid rayTraceFluid) {
        double rayTraceRange = 32d; //TODO Config
        HitResult result = player.pick(rayTraceRange, 0f, rayTraceFluid != ClipContext.Fluid.NONE);

        return (BlockHitResult) result;
    }
}

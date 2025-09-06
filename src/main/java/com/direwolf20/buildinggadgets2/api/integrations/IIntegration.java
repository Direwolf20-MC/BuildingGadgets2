package com.direwolf20.buildinggadgets2.api.integrations;

import io.netty.util.internal.UnstableApi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;

/**
 * Unstable API: Interface for mod integrations to handle item and fluid management
 */
@UnstableApi
public interface IIntegration {

    /**
     * Removes a specified amount of fluid from the player's inventory or containers.
     *
     * @param player     the player whose inventory should be modified
     * @param fluidStack the fluid to remove (type and amount)
     * @param simulate   if {@code true}, perform a dry run without changing the inventory
     */
    void removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate);

    /**
     * Removes multiple item stacks from the player's inventory.
     *
     * @param player    the player whose inventory should be modified
     * @param requestedItems the list of item stacks to remove (stack size is respected)
     * @param simulate  if {@code true}, perform a dry run without changing the inventory
     */
    void removeStacksFromInventory(Player player, ArrayList<ItemStack> requestedItems, boolean simulate);

    /**
     * Counts how many of the given item exist in the player's inventory.
     * <p>
     * The result should be stored in the {@code counter} array,
     * which is expected to have at least one element. Implementations
     * may use {@code counter[0]} to accumulate the total count.
     *
     * @param player   the player whose inventory is being queried
     * @param itemStack the item type to count
     */
    int countItemStacks(Player player, ItemStack itemStack);

    /**
     * Gives a fluid stack back to the player, e.g. after undoing an operation.
     *
     * @param player        the player receiving the fluid
     * @param returnedFluid the fluid to give
     */
    void giveFluidToPlayer(Player player, FluidStack returnedFluid);

    /**
     * Gives an item stack back to the player, e.g. after undoing an operation.
     *
     * @param player           the player receiving the item
     * @param realReturnedItem the item stack to give
     */
    void giveItemToPlayer(Player player, ItemStack realReturnedItem);
}

package com.direwolf20.buildinggadgets2.api.integrations;

import com.direwolf20.buildinggadgets2.integration.CuriosIntegration;
import com.direwolf20.buildinggadgets2.integration.ProjectEIntegration;
import com.google.common.collect.Maps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Map;

public class IntegrationRegistry {
    private static Map<String, IIntegration> integrations;
    public static void setupIntegrations(){
        integrations = Maps.newHashMap();
        register("curios",new CuriosIntegration());
        register("projecte", new ProjectEIntegration());
    }

    /**
     * Registers a new mod integration.
     * <p>
     * Example usage:
     * <pre>{@code
     * register("curios", new CuriosIntegration());
     * register("projecte", new ProjectEIntegration());
     * }</pre>
     *
     * @param modid       the unique mod ID to associate with the integration
     * @param integration the integration instance for the mod (see {@link IIntegration})
     */
    public static void register(String modid, IIntegration integration) {
        integrations.put(modid, integration);
    }

    public static void removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            entry.getValue().removeFluidStacksFromInventory(player, fluidStack, simulate);
            if (fluidStack.isEmpty()) return;
        }
    }

    public static void removeStacksFromInventory(Player player, ArrayList<ItemStack> testArray, boolean simulate) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            entry.getValue().removeStacksFromInventory(player, testArray, simulate);
            if (testArray.isEmpty()) return;
        }
    }

    public static void countItemStacks(Player player, ItemStack itemStack, int[] counter) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            entry.getValue().countItemStacks(player, itemStack, counter);
        }
    }

    public static void giveFluidToPlayer(Player player, FluidStack returnedFluid) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            entry.getValue().giveFluidToPlayer(player, returnedFluid);
            if (returnedFluid.isEmpty()) return;
        }
    }

    public static void giveItemToPlayer(Player player, ItemStack realReturnedItem) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            entry.getValue().giveItemToPlayer(player, realReturnedItem);
            if (realReturnedItem.isEmpty()) return;
        }
    }
}

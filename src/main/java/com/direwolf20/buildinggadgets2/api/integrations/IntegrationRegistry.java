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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

    private static void runForIntegrations(Function<IIntegration, Boolean> runner) {
        for (Map.Entry<String, IIntegration> entry : integrations.entrySet()) {
            if (!ModList.get().isLoaded(entry.getKey())) continue;
            var shouldContinue = runner.apply(entry.getValue());
            if (!shouldContinue) {
                return;
            }
        }
    }

    public static boolean removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate) {
        runForIntegrations(integration -> {
            integration.removeFluidStacksFromInventory(player, fluidStack, simulate);
            return !fluidStack.isEmpty();
        });
        return fluidStack.isEmpty();
    }

    public static boolean removeStacksFromInventory(Player player, ArrayList<ItemStack> requestedItems, boolean simulate) {
        runForIntegrations(integration -> {
            integration.removeStacksFromInventory(player, requestedItems, simulate);
            return !requestedItems.isEmpty();
        });
        return requestedItems.isEmpty();
    }

    public static int countItemStacks(Player player, ItemStack itemStack) {
        AtomicInteger count = new AtomicInteger();
        runForIntegrations(integration -> {
            count.addAndGet(integration.countItemStacks(player, itemStack));
            return true;
        });
        return count.get();
    }

    public static boolean giveFluidToPlayer(Player player, FluidStack returnedFluid) {
        runForIntegrations(integration -> {
            integration.giveFluidToPlayer(player, returnedFluid);
            return returnedFluid.isEmpty();
        });
        return returnedFluid.isEmpty();
    }

    public static boolean giveItemToPlayer(Player player, ItemStack realReturnedItem) {
        runForIntegrations(integration -> {
            integration.giveItemToPlayer(player, realReturnedItem);
            return realReturnedItem.isEmpty();
        });
        return realReturnedItem.isEmpty();
    }
}

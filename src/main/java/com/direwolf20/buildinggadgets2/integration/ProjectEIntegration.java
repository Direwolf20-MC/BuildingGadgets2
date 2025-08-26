package com.direwolf20.buildinggadgets2.integration;

import com.direwolf20.buildinggadgets2.api.integrations.IIntegration;
import moze_intel.projecte.api.capabilities.IKnowledgeProvider;
import moze_intel.projecte.api.proxy.IEMCProxy;
import moze_intel.projecte.api.proxy.ITransmutationProxy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.fluids.FluidStack;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class ProjectEIntegration implements IIntegration {
    public void removeFluidStacksFromInventory(Player player, FluidStack fluidStack, boolean simulate) {
        IKnowledgeProvider knowledgeProvider = getProvider(player);
        ItemStack bucket = fluidStack.getFluid().getBucket().getDefaultInstance();
        if (!knowledgeProvider.hasKnowledge(bucket)) return;
        IEMCProxy emcProxy = getEMCProxy();
        BigInteger currentEMC = knowledgeProvider.getEmc();
        var cost = emcProxy.getValue(bucket) - emcProxy.getValue(Items.BUCKET);
        BigInteger newEMC = currentEMC.subtract(BigInteger.valueOf(cost));
        if (newEMC.signum() == -1) return;
        if (!simulate) {
            knowledgeProvider.setEmc(newEMC);
            knowledgeProvider.syncEmc((ServerPlayer) player);
        }
        fluidStack.setAmount(0);
    }

    public void removeStacksFromInventory(Player player, ArrayList<ItemStack> requestedItems, boolean simulate) {
        IKnowledgeProvider knowledgeProvider = getProvider(player);
        IEMCProxy emcProxy = getEMCProxy();
        BigInteger currentEMC = knowledgeProvider.getEmc();
        long totalCost = 0;
        Iterator<ItemStack> it = requestedItems.iterator();
        while (it.hasNext()) {
            ItemStack s = it.next();
            if (!knowledgeProvider.hasKnowledge(s)) continue;
            long cost = emcProxy.getValue(s);
            if (currentEMC.subtract(BigInteger.valueOf(totalCost + cost)).signum() >= 0) {
                totalCost += cost;
                it.remove();
            }
        }
        if (!simulate) {
            BigInteger newEMC = currentEMC.subtract(BigInteger.valueOf(totalCost));
            knowledgeProvider.setEmc(newEMC);
            knowledgeProvider.syncEmc((ServerPlayer) player);
        }
    }

    public int countItemStacks(Player player, ItemStack itemStack) {
        IKnowledgeProvider knowledgeProvider = getProvider(player);
        IEMCProxy emcProxy = getEMCProxy();
        if (!knowledgeProvider.hasKnowledge(itemStack)) return 0;

        long cost = emcProxy.getValue(itemStack);
        BigInteger count = knowledgeProvider.getEmc().divide(BigInteger.valueOf(cost));
        return count.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0
                ? Integer.MAX_VALUE
                : count.intValue();
    }

    public void giveFluidToPlayer(Player player, FluidStack returnedFluid) {
        IKnowledgeProvider knowledgeProvider = getProvider(player);
        IEMCProxy emcProxy = getEMCProxy();
        long cost = emcProxy.getValue(returnedFluid.getFluid().getBucket()) - emcProxy.getValue(Items.BUCKET);
        BigInteger currentEMC = knowledgeProvider.getEmc();
        knowledgeProvider.setEmc(currentEMC.add(BigInteger.valueOf(cost)));
        knowledgeProvider.syncEmc((ServerPlayer) player);
        returnedFluid.shrink(returnedFluid.getAmount());
    }

    public void giveItemToPlayer(Player player, ItemStack realReturnedItem) {
        IKnowledgeProvider knowledgeProvider = getProvider(player);
        IEMCProxy emcProxy = getEMCProxy();
        long cost = emcProxy.getValue(realReturnedItem) - emcProxy.getValue(Items.BUCKET);
        BigInteger currentEMC = knowledgeProvider.getEmc();
        knowledgeProvider.setEmc(currentEMC.add(BigInteger.valueOf(cost)));
        knowledgeProvider.syncEmc((ServerPlayer) player);
        realReturnedItem.shrink(realReturnedItem.getCount());
    }

    private IKnowledgeProvider getProvider(Player player) {
        return ITransmutationProxy.INSTANCE.getKnowledgeProviderFor(player.getUUID());
    }
    
    private IEMCProxy getEMCProxy() {
        return IEMCProxy.INSTANCE;
    }

}

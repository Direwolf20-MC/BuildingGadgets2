package com.direwolf20.buildinggadgets2.common.capabilities;

import com.direwolf20.buildinggadgets2.setup.BG2DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.EnergyStorage;

public class EnergyStorageItemStack extends EnergyStorage {
    protected final ItemStack itemStack;

    public EnergyStorageItemStack(int capacity, ItemStack itemStack) {
        super(capacity, capacity, capacity, 0);
        this.itemStack = itemStack;
        this.energy = itemStack.getOrDefault(BG2DataComponents.FORGE_ENERGY, 0);
    }

    public void setEnergy(int energy) {
        this.energy = energy;
        itemStack.set(BG2DataComponents.FORGE_ENERGY, energy);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            itemStack.set(BG2DataComponents.FORGE_ENERGY, energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            itemStack.set(BG2DataComponents.FORGE_ENERGY, energy);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return itemStack.getOrDefault(BG2DataComponents.FORGE_ENERGY, 0);
    }
}

package com.direwolf20.buildinggadgets2.datagen;

import net.minecraft.data.DataGenerator;

public class BG2LootTables extends BaseLootTableProvider {

    public BG2LootTables(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        //lootTables.put(Registration.LaserNode.get(), createSimpleTable("lasernode", Registration.LaserNode.get()));
        //lootTables.put(Registration.LaserConnector.get(), createSimpleTable("laserconnector", Registration.LaserConnector.get()));
    }
}
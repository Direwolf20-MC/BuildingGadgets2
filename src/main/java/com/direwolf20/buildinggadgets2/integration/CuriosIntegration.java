package com.direwolf20.buildinggadgets2.integration;

import net.neoforged.fml.ModList;

public class CuriosIntegration {
    private static final String ID = "curios";

    public CuriosIntegration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ID);
    }
}

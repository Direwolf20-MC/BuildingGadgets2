package com.direwolf20.buildinggadgets2.integration;

import net.minecraftforge.fml.ModList;

public class AE2Integration {
    private static final String ID = "ae2";

    public AE2Integration() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(ID);
    }
}

package com.direwolf20.buildinggadgets2.api.utils;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import net.minecraft.resources.ResourceLocation;

public class Helpers {
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, path);
    }
}

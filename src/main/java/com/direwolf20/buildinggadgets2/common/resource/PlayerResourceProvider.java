package com.direwolf20.buildinggadgets2.common.resource;

import com.direwolf20.buildinggadgets2.api.resource.IResourceProvider;
import com.direwolf20.buildinggadgets2.api.utils.Helpers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerResourceProvider implements IResourceProvider {
    public static ResourceLocation ID = Helpers.id("player");

    @Override
    public ResourceLocation id() {
        return null;
    }

    @Override
    public String displayName() {
        return "";
    }

    @Override
    public boolean isAvailable(Player player, ItemStack gadget) {
        return false;
    }
}

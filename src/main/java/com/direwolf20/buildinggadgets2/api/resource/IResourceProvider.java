package com.direwolf20.buildinggadgets2.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IResourceProvider {
    /**
     * A unique name for this resource provider.
     * @return A unique name.
     */
    ResourceLocation id();

    /**
     * Passed through the i18n system, ideally, use a translation key here, but not required.
     * @return A translation key or a display name.
     */
    String displayName();

    // Some efficient way to return a list of "resources" and the amount of each resource.
    // Some way to consume a resource.
    // Some way to refund a resource.
    // Some way to identify the provider / source.
    // A way to identify if the provider is available for use, aka: a linked chest, a backpack, etc.

    boolean isAvailable(Player player, ItemStack gadget);
}

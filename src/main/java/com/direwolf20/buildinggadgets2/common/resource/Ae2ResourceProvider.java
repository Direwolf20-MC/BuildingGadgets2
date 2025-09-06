package com.direwolf20.buildinggadgets2.common.resource;

import com.direwolf20.buildinggadgets2.api.resource.IResourceProvider;
import com.direwolf20.buildinggadgets2.api.utils.Helpers;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Ae2ResourceProvider implements IResourceProvider {
    public static ResourceLocation ID = Helpers.id("ae2");

    @Override
    public ResourceLocation id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "";
    }

    @Override
    public boolean isAvailable(Player player, ItemStack gadgetStack) {
        if (!(gadgetStack.getItem() instanceof BaseGadget gadget)) {
            return false;
        }

        /**
         *         Level level = getLevel(player.getServer(), boundInventory);
         *         if (level == null) return;
         *         BlockEntity blockEntity = level.getBlockEntity(boundInventory.pos());
         *         if (blockEntity == null) return;
         *         if (blockEntity instanceof IWirelessAccessPoint accessPoint) {
         */

        return false;
    }
}

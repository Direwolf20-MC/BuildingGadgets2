package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.setup.ModSetup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BaseGadget extends Item {
    public BaseGadget(Properties builder) {
        super(builder.tab(ModSetup.ITEM_GROUP)
                .stacksTo(1)
                .setNoRepair());
    }

    public static ItemStack getGadget(Player player) {
        ItemStack heldItem = player.getMainHandItem();
        if (!(heldItem.getItem() instanceof BaseGadget)) {
            heldItem = player.getOffhandItem();
            if (!(heldItem.getItem() instanceof BaseGadget)) {
                return ItemStack.EMPTY;
            }
        }
        return heldItem;
    }
}

package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.setup.ModSetup;
import net.minecraft.world.item.Item;

public class Gadget_Building extends Item {
    public Gadget_Building() {
        super(new Item.Properties().tab(ModSetup.ITEM_GROUP)
                .stacksTo(1));
    }

}

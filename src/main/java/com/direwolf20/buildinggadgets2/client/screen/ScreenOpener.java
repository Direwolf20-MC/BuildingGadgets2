package com.direwolf20.buildinggadgets2.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ScreenOpener {
    public static void openDestructionScreen(ItemStack itemstack) {
        Minecraft.getInstance().setScreen(new DestructionGUI(itemstack, false));
    }

    public static void openMaterialList(ItemStack itemstack) {
        Minecraft.getInstance().setScreen(new MaterialListGUI(itemstack));
    }
}

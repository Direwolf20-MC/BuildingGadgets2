package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.client.screen.ScreenOpener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TemplateItem extends Item {

    public TemplateItem() {
        super(new Properties()
                .stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!playerIn.isShiftKeyDown())
            return super.use(worldIn, playerIn, handIn);

        if (worldIn.isClientSide) {
            ScreenOpener.openMaterialList(playerIn.getItemInHand(handIn));
        }

        return super.use(worldIn, playerIn, handIn);
    }
}

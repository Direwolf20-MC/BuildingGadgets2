package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.Styles;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GadgetBuilding extends BaseGadget {
    public GadgetBuilding() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.BUILDINGGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.BUILDINGGADGET_COST.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Minecraft mc = Minecraft.getInstance();

        if (level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Screen.hasShiftDown();

        if (sneakPressed) {
            BaseMode mode = GadgetNBT.getMode(stack);
            tooltip.add(Component.translatable("buildinggadgets2.tooltips.mode", Component.translatable(mode.i18n())).setStyle(Styles.AQUA));
            tooltip.add(Component.translatable("buildinggadgets2.tooltips.range", GadgetNBT.getToolRange(stack)).setStyle(Styles.LT_PURPLE));
            tooltip.add(Component.translatable("buildinggadgets2.tooltips.blockstate", GadgetNBT.getGadgetBlockState(stack).getBlock().getName()).setStyle(Styles.DK_GREEN));
        }
    }

    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        BlockState setState = GadgetNBT.getGadgetBlockState(gadget);
        if (setState.isAir()) return InteractionResultHolder.pass(gadget);

        var mode = GadgetNBT.getMode(gadget);
        ArrayList<StatePos> buildList = mode.collect(context.hitResult().getDirection(), context.player(), getHitPos(context), setState);

        UUID buildUUID = BuildingUtils.build(context.level(), context.player(), buildList, getHitPos(context), gadget, true);
        GadgetUtils.addToUndoList(context.level(), gadget, new ArrayList<>(), buildUUID);
        GadgetNBT.clearAnchorPos(gadget);
        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        BlockState blockState = context.level().getBlockState(context.pos());
        if (!GadgetUtils.isValidBlockState(blockState, context.level(), context.pos()) || blockState.getBlock() instanceof RenderBlock) {
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.invalidblock"), true);
            return super.onShiftAction(context);
        }
        if (GadgetUtils.setBlockState(context.stack(), blockState))
            return InteractionResultHolder.success(context.stack());

        return super.onShiftAction(context);
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.BUILDING;
    }
}

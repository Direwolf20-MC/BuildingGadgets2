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
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.state.BlockState;


import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

public class GadgetBuilding extends BaseGadget {
    public GadgetBuilding(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnergyMax() {
        return Config.BUILDINGGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.BUILDINGGADGET_COST.get();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, display, tooltip, flagIn);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        boolean sneakPressed = Minecraft.getInstance().hasShiftDown();

        if (sneakPressed) {
            BaseMode mode = GadgetNBT.getMode(stack);
            tooltip.accept(Component.translatable("buildinggadgets2.tooltips.mode", Component.translatable(mode.i18n())).setStyle(Styles.AQUA));
            tooltip.accept(Component.translatable("buildinggadgets2.tooltips.range", GadgetNBT.getToolRange(stack)).setStyle(Styles.LT_PURPLE));
            tooltip.accept(Component.translatable("buildinggadgets2.tooltips.blockstate", GadgetNBT.getGadgetBlockState(stack).getBlock().getName()).setStyle(Styles.DK_GREEN));
        }
    }

    @Override
    InteractionResult onAction(ItemActionContext context) {
        var gadget = context.stack();

        BlockState setState = GadgetNBT.getGadgetBlockState(gadget);
        if (setState.isAir()) return InteractionResult.PASS;

        var mode = GadgetNBT.getMode(gadget);
        ArrayList<StatePos> buildList = mode.collect(context.hitResult().getDirection(), context.player(), getHitPos(context), setState);

        UUID buildUUID = BuildingUtils.build(context.level(), context.player(), buildList, getHitPos(context), gadget, true);
        GadgetUtils.addToUndoList(context.level(), gadget, new ArrayList<>(), buildUUID);
        GadgetNBT.clearAnchorPos(gadget);
        return InteractionResult.SUCCESS.heldItemTransformedTo(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResult onShiftAction(ItemActionContext context) {
        BlockState blockState = context.level().getBlockState(context.pos());
        if (!GadgetUtils.isValidBlockState(blockState, context.level(), context.pos()) || blockState.getBlock() instanceof RenderBlock) {
            context.player().sendOverlayMessage(Component.translatable("buildinggadgets2.messages.invalidblock"));
            return super.onShiftAction(context);
        }
        if (GadgetUtils.setBlockState(context.stack(), blockState))
            return InteractionResult.SUCCESS.heldItemTransformedTo(context.stack());

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

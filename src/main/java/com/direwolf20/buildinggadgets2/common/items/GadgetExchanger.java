package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

import static com.direwolf20.buildinggadgets2.util.BuildingUtils.hasEnoughEnergy;
import static com.direwolf20.buildinggadgets2.util.BuildingUtils.useEnergy;

public class GadgetExchanger extends BaseGadget {
    public GadgetExchanger() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.EXCHANGINGGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.EXCHANGINGGADGET_COST.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
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

        UUID buildUUID = BuildingUtils.exchange(context.level(), context.player(), buildList, getHitPos(context), gadget, true, true);
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
     * Undo is handled differently for exchanger - we want to look at what the blocks that were replaced were previously, and grab them from the players inventory to undo it.
     */
    @Override
    public void undo(Level level, Player player, ItemStack gadget) {
        if (!canUndo(level, player, gadget)) return;
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        UUID buildUUID = GadgetNBT.popUndoList(gadget);
        ServerTickHandler.stopBuilding(buildUUID);
        ArrayList<StatePos> undoList = bg2Data.popUndoList(buildUUID);
        if (undoList.isEmpty()) return;
        Collections.reverse(undoList);
        UUID newBuildUUID = UUID.randomUUID();

        for (StatePos pos : undoList) {
            if (pos.state.isAir()) continue; //Since we store air now
            if (!pos.state.canSurvive(level, pos.pos)) continue;
            if (!player.isCreative() && !hasEnoughEnergy(gadget)) {
                player.displayClientMessage(Component.translatable("buildinggadgets2.messages.outofpower"), true);
                break; //Break out if we're out of power
            }
            if (!player.isCreative())
                useEnergy(gadget);
            ServerTickHandler.addToMap(newBuildUUID, new StatePos(pos.state, pos.pos), level, GadgetNBT.getRenderTypeByte(gadget), player, true, true, gadget, ServerBuildList.BuildType.EXCHANGE, true, GadgetNBT.nullPos);
        }
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.EXCHANGING;
    }

    /**
     * For Silk Touch - The tag that allows silk touch ALSO allows fortune, so I have to deny fortune after adding the tag....SUPER FUN!
     */
    @Override
    public boolean isEnchantable(ItemStack p_41456_) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 3;
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return super.isPrimaryItemFor(stack, enchantment) && canAcceptEnchantments(enchantment);
    }

    private boolean canAcceptEnchantments(Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.FORTUNE);
    }
}

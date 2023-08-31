package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.modes.Copy;
import com.direwolf20.buildinggadgets2.util.modes.Paste;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GadgetCopyPaste extends BaseGadget {
    public GadgetCopyPaste() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.COPYPASTEGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.COPYPASTEGADGET_COST.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Minecraft mc = Minecraft.getInstance();

        if (level == null || mc.player == null) {
            return;
        }

        if (GadgetNBT.getPasteReplace(stack))
            tooltip.add(Component.translatable("buildinggadgets2.voidwarning").withStyle(ChatFormatting.RED));

        String templateName = GadgetNBT.getTemplateName(stack);

        if (!templateName.isEmpty())
            tooltip.add(Component.translatable("buildinggadgets2.templatename", templateName).withStyle(ChatFormatting.AQUA));

        boolean sneakPressed = Screen.hasShiftDown();

        if (sneakPressed) {

        }
    }

    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("copy")) {
            GadgetNBT.setCopyStartPos(gadget, context.pos());
            buildAndStore(context, gadget);
        } else if (mode.getId().getPath().equals("paste")) {
            UUID uuid = GadgetNBT.getUUID(gadget);
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());
            ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(uuid, false);
            UUID buildUUID;
            // This should go through some translation based process
            // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
            boolean replace = GadgetNBT.getPasteReplace(gadget);
            ArrayList<StatePos> actuallyBuiltList;
            if (!replace)
                buildUUID = BuildingUtils.build(context.level(), context.player(), buildList, getHitPos(context).above().offset(GadgetNBT.getRelativePaste(gadget)), gadget, true);
            else
                buildUUID = BuildingUtils.exchange(context.level(), context.player(), buildList, getHitPos(context).above().offset(GadgetNBT.getRelativePaste(gadget)), gadget, true, false);

            GadgetUtils.addToUndoList(context.level(), gadget, new ArrayList<>(), buildUUID);
            GadgetNBT.clearAnchorPos(gadget);

            //if (!actuallyBuiltList.isEmpty()) {
            //GadgetNBT.clearAnchorPos(gadget);
            //    GadgetUtils.addToUndoList(context.level(), gadget, actuallyBuiltList); //If we placed anything at all, add to the undoList
            //}
            return InteractionResultHolder.success(gadget);
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("copy")) {
            GadgetNBT.setCopyEndPos(gadget, context.pos());
            buildAndStore(context, gadget);
        } else if (mode.equals(new Paste())) {
            //Paste
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    public void buildAndStore(ItemActionContext context, ItemStack gadget) {
        ArrayList<StatePos> buildList = new Copy().collect(context.hitResult().getDirection(), context.player(), context.pos(), Blocks.AIR.defaultBlockState());
        UUID uuid = GadgetNBT.getUUID(gadget);
        GadgetNBT.setCopyUUID(gadget); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());
        bg2Data.addToCopyPaste(uuid, buildList);
        context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.copyblocks", buildList.size()), true);
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.COPYPASTE;
    }
}

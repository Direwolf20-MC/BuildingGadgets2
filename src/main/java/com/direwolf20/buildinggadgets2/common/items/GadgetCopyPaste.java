package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.modes.Copy;
import com.direwolf20.buildinggadgets2.util.modes.Paste;
import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.UUID;

public class GadgetCopyPaste extends BaseGadget {
    public GadgetCopyPaste() {
        super();
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
            BG2Data bg2Data = BG2Data.get(context.player().level().getServer().overworld()); //TODO NPE?
            ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(uuid);

            // This should go through some translation based process
            // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
            ArrayList<StatePos> actuallyBuiltList = BuildingUtils.build(context.level(), buildList, context.pos().above());
            if (!actuallyBuiltList.isEmpty()) {
                GadgetUtils.addToUndoList(context.level(), gadget, actuallyBuiltList); //If we placed anything at all, add to the undoList
            }
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
        if (buildList.isEmpty()) return;
        UUID uuid = GadgetNBT.getUUID(gadget);
        GadgetNBT.setCopyUUID(gadget); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        BG2Data bg2Data = BG2Data.get(context.player().level().getServer().overworld()); //TODO NPE?
        bg2Data.addToCopyPaste(uuid, buildList);
        context.player().displayClientMessage(Component.literal("Copied " + buildList.size() + " blocks."), true); //Todo temp
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.COPYPASTE;
    }
}

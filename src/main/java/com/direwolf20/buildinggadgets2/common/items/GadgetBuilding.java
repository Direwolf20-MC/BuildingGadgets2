package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

public class GadgetBuilding extends BaseGadget {
    public GadgetBuilding() {
        super();
    }

    /**
     * TODO: it's very possible this method could be common on all gadgets
     */
    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        BlockState setState = GadgetNBT.getGadgetBlockState(gadget);
        if (setState.isAir()) return InteractionResultHolder.pass(gadget);

        var mode = GadgetNBT.getMode(gadget);
        ArrayList<StatePos> buildList = mode.collect(context.hitResult().getDirection(), context.player(), context.pos(), setState);

        // This should go through some translation based process
        // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
        ArrayList<StatePos> actuallyBuiltList = BuildingUtils.build(context.level(), buildList, context.pos());
        if (!actuallyBuiltList.isEmpty()) {
            GadgetUtils.addToUndoList(context.level(), gadget, actuallyBuiltList); //If we placed anything at all, add to the undoList
        }
        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        BlockState blockState = context.level().getBlockState(context.pos());

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

package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public abstract class BaseMode implements Comparable<BaseMode> {
    /**
     * @deprecated all the modes should be possible without knowing the type of gadget... I'd hope
     */
    @Deprecated
    public boolean isExchanging;

    public BaseMode(boolean isExchanging) {
        this.isExchanging = isExchanging;
    }

    /**
     * Collects a list of blocks that should be used when building and rendering the mode - Checks for an Anchor first
     */
    public final ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state) {
        ItemStack gadget = BaseGadget.getGadget(player);
        final ArrayList<StatePos> buildList = new ArrayList<>();
        if (!player.mayBuild())
            return buildList;
        ArrayList<BlockPos> anchorList = GadgetNBT.getAnchorList(gadget);
        if (anchorList.isEmpty()) {
            if (!isExchanging || !player.level().getBlockState(start).equals(state))
                buildList.addAll(collectWorld(hitSide, player, start, state));
        } else {
            ArrayList<BlockPos> posList = GadgetNBT.getAnchorList(gadget);
            posList.forEach(e -> buildList.add(new StatePos(state, e)));
        }
        return buildList;
    }

    public abstract ArrayList<StatePos> collectWorld(Direction hitSide, Player player, BlockPos start, BlockState state);

    public abstract ResourceLocation getId();

    /**
     * Used for translations
     */
    public String i18n() {
        return BuildingGadgets2.MODID + ".modes." + this.getId().getPath();
    }

    /**
     * Used when displaying the mode selection wheel
     */
    public ResourceLocation icon() {
        return new ResourceLocation(BuildingGadgets2.MODID, "textures/gui/mode/" + getId().getPath() + ".png");
    }

    public boolean isPosValid(Level level, BlockPos blockPos) {
        if ((blockPos.getY() >= level.getMaxBuildHeight() || blockPos.getY() < level.getMinBuildHeight()))
            return false;
        if (isExchanging) {
            if (!GadgetUtils.isValidBlockState(level.getBlockState(blockPos), level, blockPos))
                return false;
            return true;
        } else {
            //Todo More validations like location, etc
            if (!level.getBlockState(blockPos).canBeReplaced())
                return false;
            return true;
        }
    }

    // TODO: implement the correct comparator
    @Override
    public int compareTo(@NotNull BaseMode o) {
        return this.getId().compareTo(o.getId());
    }
}

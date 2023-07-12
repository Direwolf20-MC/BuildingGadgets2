package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
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
     * Collects a list of blocks that should be used when building and rendering the mode
     */
    public abstract ArrayList<StatePos> collect(Direction hitSide, Player player, BlockPos start, BlockState state);

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
        if (isExchanging) {
            if (level.getBlockState(blockPos).getDestroySpeed(level, blockPos) < 0)
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

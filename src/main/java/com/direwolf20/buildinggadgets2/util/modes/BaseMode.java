package com.direwolf20.buildinggadgets2.util.modes;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseMode implements Comparable<BaseMode> {
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
        List<BlockPos> anchorList = GadgetNBT.getAnchorList(gadget);
        if (anchorList.isEmpty()) {
            //if (!isExchanging || !player.level().getBlockState(start).equals(state))
            buildList.addAll(collectWorld(hitSide, player, start, state));
        } else {
            List<BlockPos> posList = GadgetNBT.getAnchorList(gadget);
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
        return BuildingGadgets2Api.MOD_ID + ".modes." + this.getId().getPath();
    }

    /**
     * Used when displaying the mode selection wheel
     */
    public ResourceLocation icon() {
        return ResourceLocation.fromNamespaceAndPath(BuildingGadgets2Api.MOD_ID, "textures/gui/mode/" + getId().getPath() + ".png");
    }

    public boolean isPosValid(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        ItemStack gadget = BaseGadget.getGadget(player);
        if (!isExchangingValid(level, player, blockPos, gadget)) return false;
        if ((blockPos.getY() >= level.getMaxBuildHeight() || blockPos.getY() < level.getMinBuildHeight()))
            return false;
        if (!blockState.canSurvive(level, blockPos)) return false; //Seeds on tilled earth, cactus, sugarcane, etc
        if (!level.mayInteract(player, blockPos)) return false; //Chunk Protection like spawn and FTB Utils
        if (!isExchanging) {
            if (!level.getBlockState(blockPos).canBeReplaced())
                return false;
        }
        return true;
    }

    public ArrayList<StatePos> removeUnConnected(Level level, Player player, BlockPos startAt, ArrayList<StatePos> coordinates, Direction hitSide) {
        if (coordinates.isEmpty()) return coordinates;
        Map<BlockPos, BlockState> coordinatesPositions = coordinates.stream().collect(Collectors.toMap(e -> e.pos, e -> e.state));
        Set<StatePos> visitedBlocks = new HashSet<>(); //Blocks we've checked
        Queue<BlockPos> blocksToVisit = new LinkedList<>(); //Blocks we need to check
        blocksToVisit.offer(startAt); //Add the starting block to 'need to check'

        while (!blocksToVisit.isEmpty()) {
            BlockPos currentPos = blocksToVisit.poll(); //Get the current block to check

            if (!visitedBlocks.contains(new StatePos(coordinatesPositions.get(currentPos), currentPos)) && coordinatesPositions.containsKey(currentPos)) { //If we haven't added it already its in our list of coords we might place in
                visitedBlocks.add(new StatePos(coordinatesPositions.get(currentPos), currentPos)); //This is a list of blocks that we considered valid

                for (Direction direction : Direction.stream().filter(e -> !e.getAxis().equals(hitSide.getAxis())).toList()) { //Grab all the blocks around this one based on hitSide and add to the list to check out
                    BlockPos nextPos = currentPos.relative(direction);
                    if (coordinatesPositions.containsKey(nextPos)) { //Only if its inside our list of coords to check.
                        blocksToVisit.offer(nextPos);
                    }
                }
            }
        }
        return new ArrayList<>(visitedBlocks);
    }

    public boolean isExchangingValid(Level level, Player player, BlockPos pos, ItemStack gadget) {
        if (!isExchanging) return true; //Don't do these checks if we're not exchanging
        if (level.getBlockState(pos).isAir())
            return false;
        if (!GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos))
            return false;
        boolean fuzzy = GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.FUZZY.getName());
        BlockState oldState = level.getBlockState(pos);
        if (oldState.hasBlockEntity() && !GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.AFFECT_TILES.getName()))
            return false;
        if (fuzzy) {
            if (oldState.isAir()) return false;
            if (oldState.equals(GadgetNBT.getGadgetBlockState(gadget))) return false;
            if (oldState.getBlock() instanceof RenderBlock) return false;
        } else {
            BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadget);
            BlockState compareState = level.getBlockState(lookingAt.getBlockPos());
            if (!oldState.equals(compareState)) return false;
        }
        return true;
    }

    // TODO: implement the correct comparator
    @Override
    public int compareTo(@NotNull BaseMode o) {
        return this.getId().compareTo(o.getId());
    }
}

package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.datagen.BG2BlockTags;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GadgetUtils {
    /*private static final ImmutableList<Property<?>> ALLOWED_PROPERTIES = ImmutableList.of(
            BlockStateProperties.FACING, BlockStateProperties.AXIS, BlockStateProperties.HORIZONTAL_FACING,
            BlockStateProperties.CHEST_TYPE, BlockStateProperties.SLAB_TYPE
    );*/

    private static final ImmutableList<Property<?>> DENY_PROPERTIES = ImmutableList.of(
            BlockStateProperties.AGE_1, BlockStateProperties.AGE_2, BlockStateProperties.AGE_3, BlockStateProperties.AGE_4,
            BlockStateProperties.AGE_5, BlockStateProperties.AGE_7, BlockStateProperties.AGE_15, BlockStateProperties.AGE_25,
            DoublePlantBlock.HALF, BlockStateProperties.WATERLOGGED, BlockStateProperties.LIT, BlockStateProperties.HAS_RECORD,
            BlockStateProperties.HAS_BOOK, BlockStateProperties.OPEN, BlockStateProperties.STAGE
    );

    public static boolean isValidBlockState(BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.is(BG2BlockTags.BG2DENY)) return false;
        if (blockState.getDestroySpeed(level, blockPos) < 0) return false;
        if (!blockState.getFluidState().isEmpty() && !blockState.getFluidState().isSource()) return false;
        return true;
    }

    public static boolean isValidDestroyBlockState(BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.isAir()) return false;
        if (blockState.getDestroySpeed(level, blockPos) < 0) return false;
        if (blockState.getBlock() instanceof RenderBlock) return false;
        return true;
    }

    public static boolean setBlockState(ItemStack gadget, BlockState blockState) {
        BlockState placeState = cleanBlockState(blockState);
        GadgetNBT.setGadgetBlockState(gadget, placeState);
        return true;
    }

    public static ItemStack getSimpleItemForBlock(BlockState blockState) {
        return new ItemStack(blockState.getBlock());
    }

    public static ItemStack getItemForBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        return blockState.getCloneItemStack(new BlockHitResult(Vec3.ZERO, Direction.UP, blockPos, false), level, blockPos, player);
    }

    public static List<ItemStack> getDropsForBlockState(ServerLevel level, BlockPos blockPos, BlockState blockState, Player player) {
        ItemStack tempStack = new ItemStack(Registration.Exchanging_Gadget.get());
        HolderLookup.RegistryLookup<Enchantment> registrylookup = level.getServer().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        tempStack.enchant(registrylookup.getOrThrow(Enchantments.SILK_TOUCH), 1);
        List<ItemStack> drops = new ArrayList<>();
        try {
            drops = new ArrayList<>(getDropsForBlockStateGadget(level, blockPos, blockState, tempStack));
        } catch (Exception e) {
            //No-Op
        }
        ItemStack baseItem;
        if (player != null)
            baseItem = getItemForBlock(blockState, level, blockPos, player); //Sometimes we have the player, sometimes not!
        else
            baseItem = getSimpleItemForBlock(blockState);
        if (drops.stream().filter(e -> ItemStack.isSameItem(e, baseItem)).toList().isEmpty()) { //If the item we expect to find isn't in the drops list, something weird happened, like wheat seeds from tall grass
            drops = new ArrayList<>();
            if (!baseItem.isEmpty())
                drops.add(baseItem);
            return drops;
        }
        return drops;
    }

    public static List<ItemStack> getDropsForBlockStateGadget(ServerLevel level, BlockPos blockPos, BlockState blockState, ItemStack gadget) {
        LootParams.Builder builder = (new LootParams.Builder(level))
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                .withParameter(LootContextParams.TOOL, gadget);

        return blockState.getDrops(builder);
    }

    public static BlockState cleanBlockState(BlockState sourceState) {
        BlockState placeState = sourceState.getBlock().defaultBlockState();
        for (Property<?> prop : sourceState.getProperties()) {
            if (!DENY_PROPERTIES.contains(prop)) {
                placeState = applyProperty(placeState, sourceState, prop);
            }
        }
        return placeState;
    }

    public static void addToUndoList(Level level, ItemStack gadget, ArrayList<StatePos> buildList, UUID uuid) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        bg2Data.addToUndoList(uuid, buildList, level);
        GadgetNBT.addToUndoList(gadget, uuid, bg2Data);
    }

    public static void addToUndoList(Level level, ItemStack gadget, ArrayList<StatePos> buildList) {
        addToUndoList(level, gadget, buildList, UUID.randomUUID());
    }

    private static <T extends Comparable<T>> BlockState applyProperty(BlockState state, BlockState from, Property<T> prop) {
        return state.setValue(prop, from.getValue(prop));
    }

    public static AABB getSquareArea(BlockPos pos, Direction face, int range) {
        switch (face) {
            case UP:
            case DOWN:
                // If you're looking up or down, the area will extend east-west and north-south
                return new AABB(pos.getX() - range, pos.getY(), pos.getZ() - range, pos.getX() + range, pos.getY(), pos.getZ() + range);
            case NORTH:
            case SOUTH:
                // If you're looking north or south, the area will extend up-down and east-west
                return new AABB(pos.getX() - range, pos.getY() - range, pos.getZ(), pos.getX() + range, pos.getY() + range, pos.getZ());
            case EAST:
            case WEST:
                // If you're looking east or west, the area will extend up-down and north-south
                return new AABB(pos.getX(), pos.getY() - range, pos.getZ() - range, pos.getX(), pos.getY() + range, pos.getZ() + range);
            default:
                throw new IllegalStateException("Unexpected value: " + face);
        }
    }

    public static ArrayList<StatePos> getDestructionArea(Level level, BlockPos pos, Direction face, Player player, ItemStack gadget) {
        int depth = GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.DEPTH.getName());

        if (gadget.isEmpty() || depth == 0 || !player.mayBuild())
            return new ArrayList<>();

        boolean vertical = face.getAxis().isVertical();
        Direction up = vertical ? player.getDirection() : Direction.UP;
        Direction down = up.getOpposite();
        Direction right = vertical ? up.getClockWise() : face.getCounterClockWise();
        Direction left = right.getOpposite();

        BlockPos first = pos.relative(left, GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.LEFT.getName())).relative(up, GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.UP.getName()));
        BlockPos second = pos.relative(right, GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.RIGHT.getName()))
                .relative(down, GadgetNBT.getToolValue(gadget, GadgetNBT.IntSettings.DOWN.getName()))
                .relative(face.getOpposite(), depth - 1);

        //boolean isFluidOnly = getIsFluidOnly(gadget); //Todo
        AABB box = VecHelpers.aabbFromBlockPos(first, second);
        ArrayList<StatePos> returnList = new ArrayList<>();
        BlockPos.betweenClosedStream(box).map(BlockPos::immutable).forEach(blockPos -> {
            BlockState blockState = level.getBlockState(blockPos);
            if (blockState.hasBlockEntity() && !GadgetNBT.getSetting(gadget, GadgetNBT.ToggleableSettings.AFFECT_TILES.getName()))
                return;
            if (isValidDestroyBlockState(blockState, level, blockPos))
                returnList.add(new StatePos(blockState, blockPos));
        });
        return returnList;
    }

    //Because contains doesn't use <= just <
    public static boolean direContains(AABB aabb, double x, double y, double z) {
        return x >= aabb.minX && x <= aabb.maxX && y >= aabb.minY && y <= aabb.maxY && z >= aabb.minZ && z <= aabb.maxZ;
    }

    public static boolean direContains(AABB aabb, BlockPos pos) {
        return direContains(aabb, pos.getX(), pos.getY(), pos.getZ());
    }
}

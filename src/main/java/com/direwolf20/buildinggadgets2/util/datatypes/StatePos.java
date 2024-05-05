package com.direwolf20.buildinggadgets2.util.datatypes;

import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.ItemStackKey;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StatePos {
    public BlockState state;
    public BlockPos pos;

    public StatePos(BlockState state, BlockPos pos) {
        this.state = state;
        this.pos = pos;
    }

    public StatePos(CompoundTag compoundTag) {
        if (!compoundTag.contains("blockstate") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compoundTag.getCompound("blockstate"));
        this.pos = NbtUtils.readBlockPos(compoundTag, "blockpos").orElse(BlockPos.ZERO);
    }

    public StatePos(CompoundTag compoundTag, ArrayList<BlockState> blockStates) {
        if (!compoundTag.contains("blockstateshort") || !compoundTag.contains("blockpos")) {
            this.state = null;
            this.pos = null;
        }
        this.state = blockStates.get(compoundTag.getShort("blockstateshort"));
        this.pos = BlockPos.of(compoundTag.getLong("blockpos"));
    }

    public CompoundTag getTag() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put("blockstate", NbtUtils.writeBlockState(state));
        compoundTag.put("blockpos", NbtUtils.writeBlockPos(pos));
        return compoundTag;
    }

    public CompoundTag getTag(ArrayList<BlockState> blockStates) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putShort("blockstateshort", (short) blockStates.indexOf(state));
        compoundTag.putLong("blockpos", pos.asLong());
        return compoundTag;
    }

    public static ArrayList<BlockState> getBlockStateMap(ArrayList<StatePos> list) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (StatePos statePos : list) {
            if (!blockStateMap.contains(statePos.state))
                blockStateMap.add(statePos.state);
        }
        return blockStateMap;
    }

    public static ArrayList<StatePos> rotate90Degrees(ArrayList<StatePos> list, ArrayList<TagPos> tagListMutable) {
        ArrayList<StatePos> rotatedList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return rotatedList;
        }
        boolean tags = !(tagListMutable == null || tagListMutable.isEmpty()); //If not empty or null, it has tags!

        Map<BlockPos, CompoundTag> tagMap = new HashMap<>();
        if (tags)
            tagMap = tagListMutable.stream().collect(Collectors.toMap(e -> e.pos, e -> e.tag));

        for (StatePos statePos : list) {
            BlockPos oldPos = statePos.pos;
            BlockState oldState = statePos.state;
            BlockState newState = oldState.rotate(Rotation.CLOCKWISE_90);
            BlockPos newPos = new BlockPos(-oldPos.getZ(), oldPos.getY(), oldPos.getX());

            if (tags && tagMap.get(statePos.pos) != null) {
                CompoundTag tempTag = tagMap.get(statePos.pos);
                tagMap.remove(statePos.pos);
                tagMap.put(newPos, tempTag);
            }

            rotatedList.add(new StatePos(newState, newPos));
        }

        if (tags) {
            tagListMutable.clear();
            for (Map.Entry<BlockPos, CompoundTag> entry : tagMap.entrySet())
                tagListMutable.add(new TagPos(entry.getValue(), entry.getKey()));
        }

        return rotatedList;
    }

    @OnlyIn(Dist.CLIENT)
    public static Map<ItemStackKey, Integer> getItemList(ArrayList<StatePos> list) {
        Map<ItemStackKey, Integer> itemList = new Object2IntOpenHashMap<>();
        if (list == null || list.isEmpty())
            return itemList;
        for (StatePos statePos : list) {
            ItemStackKey itemStackKey = new ItemStackKey(GadgetUtils.getItemForBlock(statePos.state, Minecraft.getInstance().level, BlockPos.ZERO, Minecraft.getInstance().player), true);
            if (!itemList.containsKey(itemStackKey)) //Todo Slabs, etc
                itemList.put(itemStackKey, 1);
            else
                itemList.put(itemStackKey, itemList.get(itemStackKey) + 1);
        }
        return itemList;
    }

    public static ListTag getBlockStateNBT(ArrayList<BlockState> blockStateMap) {
        ListTag listTag = new ListTag();
        for (BlockState blockState : blockStateMap) {
            listTag.add(NbtUtils.writeBlockState(blockState));
        }
        return listTag;
    }

    public static ArrayList<BlockState> getBlockStateMapFromNBT(ListTag listTag) {
        ArrayList<BlockState> blockStateMap = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            BlockState blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), listTag.getCompound(i));
            blockStateMap.add(blockState);
        }
        return blockStateMap;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StatePos) {
            return ((StatePos) obj).state.equals(this.state) && ((StatePos) obj).pos.equals(this.pos);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, pos);
    }
}

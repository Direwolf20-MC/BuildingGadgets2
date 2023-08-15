package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BG2Data extends SavedData {
    private static final String NAME = "buildinggadgets2";
    private final HashMap<UUID, ArrayList<StatePos>> undoList;
    private final HashMap<UUID, ArrayList<StatePos>> copyPasteLookup;
    private final HashMap<UUID, ArrayList<TagPos>> teMap;

    public BG2Data(HashMap<UUID, ArrayList<StatePos>> undoList, HashMap<UUID, ArrayList<StatePos>> copyPasteLookup, HashMap<UUID, ArrayList<TagPos>> teMap) {
        this.undoList = undoList;
        this.copyPasteLookup = copyPasteLookup;
        this.teMap = teMap;
    }

    public void addToUndoList(UUID uuid, ArrayList<StatePos> list, Level level) {
        undoList.put(uuid, list);
        this.setDirty();
    }

    public void removeFromUndoList(UUID uuid) {
        undoList.remove(uuid);
        this.setDirty();
    }

    public void addToCopyPaste(UUID uuid, ArrayList<StatePos> list) {
        copyPasteLookup.put(uuid, list);
        this.setDirty();
    }

    public void addToTEMap(UUID uuid, ArrayList<TagPos> list) {
        teMap.put(uuid, list);
        this.setDirty();
    }

    public ArrayList<StatePos> getCopyPasteList(UUID uuid, boolean remove) {
        ArrayList<StatePos> returnList = copyPasteLookup.get(uuid);
        if (remove) {
            returnList = copyPasteLookup.remove(uuid);
            this.setDirty();
        }
        return returnList;
    }

    public CompoundTag getCopyPasteListAsNBTMap(UUID uuid, boolean remove) {
        return statePosListToNBTMapArray(getCopyPasteList(uuid, remove));
    }

    public ArrayList<StatePos> peekUndoList(UUID uuid) {
        ArrayList<StatePos> posList = undoList.get(uuid);
        this.setDirty();
        return posList;
    }

    public ArrayList<StatePos> popUndoList(UUID uuid) {
        ArrayList<StatePos> posList = undoList.remove(uuid);
        this.setDirty();
        return posList;
    }

    public ArrayList<TagPos> getTEMap(UUID uuid) {
        ArrayList<TagPos> tagList = teMap.remove(uuid);
        this.setDirty();
        return tagList;
    }

    public static CompoundTag statePosListToNBTMapArray(ArrayList<StatePos> list) {
        CompoundTag tag = new CompoundTag();
        if (list == null || list.isEmpty()) return tag;
        ArrayList<BlockState> blockStateMap = StatePos.getBlockStateMap(list);
        ListTag blockStateMapList = StatePos.getBlockStateNBT(blockStateMap);
        int[] blocklist = new int[list.size()];
        int k = 0;
        for (StatePos statePos : list) {
            blocklist[k] = blockStateMap.indexOf(statePos.state);
            k++;
        }
        tag.put("startpos", NbtUtils.writeBlockPos(list.get(0).pos));
        tag.put("endpos", NbtUtils.writeBlockPos(list.get(list.size() - 1).pos));
        tag.put("blockstatemap", blockStateMapList);
        tag.putIntArray("statelist", blocklist); //Todo - Short Array?
        return tag;
    }

    public static ArrayList<StatePos> statePosListFromNBTMapArray(CompoundTag tag) {
        ArrayList<StatePos> statePosList = new ArrayList<>();
        if (!tag.contains("blockstatemap") || !tag.contains("statelist")) return statePosList;
        ArrayList<BlockState> blockStateMap = StatePos.getBlockStateMapFromNBT(tag.getList("blockstatemap", Tag.TAG_COMPOUND));
        BlockPos start = NbtUtils.readBlockPos(tag.getCompound("startpos"));
        BlockPos end = NbtUtils.readBlockPos(tag.getCompound("endpos"));
        AABB aabb = new AABB(start, end);
        int[] blocklist = tag.getIntArray("statelist");
        final int[] counter = {0};
        BlockPos.betweenClosedStream(aabb).map(BlockPos::immutable).forEach(pos -> {
            int blockStateLookup = blocklist[counter[0]++];
            BlockState blockState = blockStateMap.get(blockStateLookup);
            statePosList.add(new StatePos(blockState, pos));
        });
        return statePosList;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag undoTagList = new ListTag();
        for (Map.Entry<UUID, ArrayList<StatePos>> entry : undoList.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            ListTag tempList = new ListTag();
            for (StatePos statePos : entry.getValue()) {
                tempList.add(statePos.getTag());
            }
            tempTag.put("stateposlist", tempList);
            undoTagList.add(tempTag);
        }
        nbt.put("undolist", undoTagList);

        ListTag copyPasteTag = new ListTag();
        for (Map.Entry<UUID, ArrayList<StatePos>> entry : copyPasteLookup.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            tempTag.put("stateposlist", statePosListToNBTMapArray(entry.getValue()));
            copyPasteTag.add(tempTag);
        }
        nbt.put("copypaste", copyPasteTag);

        ListTag teMapTag = new ListTag();
        for (Map.Entry<UUID, ArrayList<TagPos>> entry : teMap.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            ListTag tempList = new ListTag();
            for (TagPos tagPos : entry.getValue()) {
                tempList.add(tagPos.getTag());
            }
            tempTag.put("temaplist", tempList);
            teMapTag.add(tempTag);
        }
        nbt.put("temaptag", teMapTag);
        return nbt;
    }

    public static BG2Data readNbt(CompoundTag nbt) {
        HashMap<UUID, ArrayList<StatePos>> undoList = new HashMap<>();
        ListTag undoTagList = nbt.getList("undolist", Tag.TAG_COMPOUND);
        for (int i = 0; i < undoTagList.size(); i++) {
            UUID uuid = undoTagList.getCompound(i).getUUID("uuid");
            ListTag statePosList = undoTagList.getCompound(i).getList("stateposlist", CompoundTag.TAG_COMPOUND);
            ArrayList<StatePos> tempList = new ArrayList<>();
            for (int j = 0; j < statePosList.size(); j++) {
                tempList.add(new StatePos(statePosList.getCompound(j)));
            }
            undoList.put(uuid, tempList);
        }

        HashMap<UUID, ArrayList<StatePos>> copyPaste = new HashMap<>();
        ListTag copyPasteList = nbt.getList("copypaste", Tag.TAG_COMPOUND);
        for (int i = 0; i < copyPasteList.size(); i++) {
            UUID uuid = copyPasteList.getCompound(i).getUUID("uuid");
            CompoundTag statePosList = copyPasteList.getCompound(i).getCompound("stateposlist");
            copyPaste.put(uuid, statePosListFromNBTMapArray(statePosList));
        }

        HashMap<UUID, ArrayList<TagPos>> teMap = new HashMap<>();
        ListTag teMapListTag = nbt.getList("temaptag", Tag.TAG_COMPOUND);
        for (int i = 0; i < teMapListTag.size(); i++) {
            UUID uuid = teMapListTag.getCompound(i).getUUID("uuid");
            ListTag temaplistTag = teMapListTag.getCompound(i).getList("temaplist", Tag.TAG_COMPOUND);
            ArrayList<TagPos> tagPosList = new ArrayList<>();
            for (int j = 0; j < temaplistTag.size(); j++) {
                TagPos tagPos = new TagPos(temaplistTag.getCompound(j));
                tagPosList.add(tagPos);
            }
            teMap.put(uuid, tagPosList);
        }
        return new BG2Data(undoList, copyPaste, teMap);
    }

    public static BG2Data get(ServerLevel world) {
        BG2Data bg2Data = world.getDataStorage().computeIfAbsent(BG2Data::readNbt, () -> new BG2Data(new HashMap<>(), new HashMap<>(), new HashMap<>()), NAME);
        bg2Data.setDirty();
        return bg2Data;
    }
}

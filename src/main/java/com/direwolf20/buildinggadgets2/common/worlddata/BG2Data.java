package com.direwolf20.buildinggadgets2.common.worlddata;

import com.direwolf20.buildinggadgets2.util.modes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BG2Data extends SavedData {
    private static final String NAME = "buildinggadgets2";
    private HashMap<UUID, ArrayList<StatePos>> undoList;
    private HashMap<UUID, Long> undoListTimers;
    private HashMap<UUID, ArrayList<StatePos>> copyPasteLookup;

    public BG2Data(HashMap<UUID, ArrayList<StatePos>> undoList, HashMap<UUID, Long> undoListTimers, HashMap<UUID, ArrayList<StatePos>> copyPasteLookup) {
        this.undoList = undoList;
        this.undoListTimers = undoListTimers;
        this.copyPasteLookup = copyPasteLookup;
    }

    public void addToUndoList(UUID uuid, ArrayList<StatePos> list, Level level) {
        undoList.put(uuid, list);
        undoListTimers.put(uuid, level.getGameTime());
        cleanupList(level);
        this.setDirty();
    }

    public void addToCopyPaste(UUID uuid, ArrayList<StatePos> list) {
        copyPasteLookup.put(uuid, list);
        this.setDirty();
    }

    public ArrayList<StatePos> getCopyPasteList(UUID uuid) {
        return copyPasteLookup.get(uuid);
    }

    public void cleanupList(Level level) {
        long currentTime = level.getGameTime();
        ArrayList<UUID> uuidsToRemove = new ArrayList<>();
        for (Map.Entry<UUID, Long> entry : undoListTimers.entrySet()) {
            if (currentTime - entry.getValue() > 100000) {
                uuidsToRemove.add(entry.getKey());
            }
        }
        for (UUID uuid : uuidsToRemove) {
            undoList.remove(uuid);
            undoListTimers.remove(uuid);
        }
    }

    public ArrayList<StatePos> getUndoList(UUID uuid) {
        ArrayList<StatePos> posList = undoList.remove(uuid);
        undoListTimers.remove(uuid);
        this.setDirty();
        return posList;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        ListTag undoTagList = new ListTag();
        for (Map.Entry<UUID, ArrayList<StatePos>> entry : undoList.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            ListTag statePosList = new ListTag();
            for (StatePos statePos : entry.getValue()) {
                statePosList.add(statePos.getTag());
            }
            tempTag.put("stateposlist", statePosList);
            undoTagList.add(tempTag);
        }
        nbt.put("undolist", undoTagList);

        ListTag undoTagListTimer = new ListTag();
        for (Map.Entry<UUID, Long> entry : undoListTimers.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            tempTag.putLong("time", entry.getValue());
            undoTagListTimer.add(tempTag);
        }
        nbt.put("undolisttimer", undoTagListTimer);

        ListTag copyPasteTag = new ListTag();
        for (Map.Entry<UUID, ArrayList<StatePos>> entry : copyPasteLookup.entrySet()) {
            CompoundTag tempTag = new CompoundTag();
            tempTag.putUUID("uuid", entry.getKey());
            ListTag statePosList = new ListTag();
            for (StatePos statePos : entry.getValue()) {
                statePosList.add(statePos.getTag());
            }
            tempTag.put("stateposlist", statePosList);
            copyPasteTag.add(tempTag);
        }
        nbt.put("copypaste", copyPasteTag);
        return nbt;
    }

    public static BG2Data readNbt(CompoundTag nbt) {
        HashMap<UUID, ArrayList<StatePos>> undoList = new HashMap<>();
        ListTag undoTagList = nbt.getList("undolist", Tag.TAG_COMPOUND);
        for (int i = 0; i < undoTagList.size(); i++) {
            UUID uuid = undoTagList.getCompound(i).getUUID("uuid");
            ArrayList<StatePos> statePosArrayList = new ArrayList<>();
            ListTag statePosList = undoTagList.getCompound(i).getList("stateposlist", Tag.TAG_COMPOUND);
            for (int j = 0; j < statePosList.size(); j++) {
                statePosArrayList.add(new StatePos(statePosList.getCompound(j)));
            }
            undoList.put(uuid, statePosArrayList);
        }

        HashMap<UUID, Long> undoListTimer = new HashMap<>();
        ListTag undoTagListTimer = nbt.getList("undolisttimer", Tag.TAG_COMPOUND);
        for (int i = 0; i < undoTagListTimer.size(); i++) {
            UUID uuid = undoTagListTimer.getCompound(i).getUUID("uuid");
            Long time = undoTagListTimer.getCompound(i).getLong("time");
            undoListTimer.put(uuid, time);
        }

        HashMap<UUID, ArrayList<StatePos>> copyPaste = new HashMap<>();
        ListTag copyPasteList = nbt.getList("copypaste", Tag.TAG_COMPOUND);
        for (int i = 0; i < copyPasteList.size(); i++) {
            UUID uuid = copyPasteList.getCompound(i).getUUID("uuid");
            ArrayList<StatePos> statePosArrayList = new ArrayList<>();
            ListTag statePosList = copyPasteList.getCompound(i).getList("stateposlist", Tag.TAG_COMPOUND);
            for (int j = 0; j < statePosList.size(); j++) {
                statePosArrayList.add(new StatePos(statePosList.getCompound(j)));
            }
            copyPaste.put(uuid, statePosArrayList);
        }

        return new BG2Data(undoList, undoListTimer, copyPaste);
    }

    public static BG2Data get(ServerLevel world) {
        BG2Data bg2Data = world.getDataStorage().computeIfAbsent(BG2Data::readNbt, () -> new BG2Data(new HashMap<>(), new HashMap<>(), new HashMap<>()), NAME);
        bg2Data.setDirty();
        return bg2Data;
    }
}

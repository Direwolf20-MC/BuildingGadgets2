package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.UUID;

public class ServerBuildList {
    public Level level;
    public ArrayList<StatePos> statePosList;
    public byte buildType;
    public UUID playerUUID;
    public int originalSize;
    public ArrayList<StatePos> actuallyBuildList = new ArrayList<>();
    public boolean needItems;
    public UUID buildUUID;

    public ServerBuildList(Level level, ArrayList<StatePos> statePosList, byte buildType, UUID playerUUID, boolean needItems, UUID buildUUID) {
        this.level = level;
        this.statePosList = statePosList;
        this.buildType = buildType;
        this.playerUUID = playerUUID;
        this.originalSize = statePosList.size();
        this.needItems = needItems;
        this.buildUUID = buildUUID;
    }

    public void addToBuiltList(StatePos statePos) {
        this.actuallyBuildList.add(statePos);
    }
}

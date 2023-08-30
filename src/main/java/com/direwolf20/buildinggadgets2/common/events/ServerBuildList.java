package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.UUID;

public class ServerBuildList {
    public enum BuildType {
        BUILD,
        EXCHANGE,
        DESTROY,
        UNDO_DESTROY
    }

    public Level level;
    public ArrayList<StatePos> statePosList;
    public byte renderType;
    public UUID playerUUID;
    public int originalSize;
    public ArrayList<StatePos> actuallyBuildList = new ArrayList<>();
    public boolean needItems;
    public boolean returnItems;
    public ItemStack gadget;
    public UUID buildUUID;
    public BuildType buildType;
    public boolean dropContents;
    public ArrayList<BlockPos> retryList = new ArrayList<>();

    public ServerBuildList(Level level, ArrayList<StatePos> statePosList, byte renderType, UUID playerUUID, boolean needItems, boolean returnItems, UUID buildUUID, ItemStack gadget, BuildType buildType, boolean dropContents) {
        this.level = level;
        this.statePosList = statePosList;
        this.renderType = renderType;
        this.playerUUID = playerUUID;
        this.originalSize = statePosList.size();
        this.needItems = needItems;
        this.buildUUID = buildUUID;
        this.returnItems = returnItems;
        this.gadget = gadget.copy();
        this.buildType = buildType;
        this.dropContents = dropContents;
    }

    public void addToBuiltList(StatePos statePos) {
        this.actuallyBuildList.add(statePos);
    }
}

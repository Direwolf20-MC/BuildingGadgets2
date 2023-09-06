package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.util.DimBlockPos;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class ServerBuildList {
    public enum BuildType {
        BUILD,
        EXCHANGE,
        DESTROY,
        UNDO_DESTROY,
        CUT
    }

    public Level level;
    public ArrayList<StatePos> statePosList;
    public ArrayList<TagPos> teData;
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
    public BlockPos cutStart = BlockPos.ZERO;
    public BlockPos lookingAt = BlockPos.ZERO;
    public DimBlockPos boundPos;
    public int direction;

    public ServerBuildList(Level level, ArrayList<StatePos> statePosList, byte renderType, UUID playerUUID, boolean needItems, boolean returnItems, UUID buildUUID, ItemStack gadget, BuildType buildType, boolean dropContents, BlockPos lookingAt, DimBlockPos boundPos, int direction) {
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
        this.lookingAt = lookingAt;
        this.boundPos = boundPos;
        this.direction = direction;
    }

    public void addToBuiltList(StatePos statePos) {
        this.actuallyBuildList.add(statePos);
    }

    public void updateActuallyBuiltList(StatePos statePos) {
        for (StatePos entry : actuallyBuildList) {
            if (entry.pos.equals(statePos.pos)) {
                entry.state = statePos.state;
                break;
            }
        }
    }

    public CompoundTag getTagForPos(BlockPos pos) {
        CompoundTag compoundTag = new CompoundTag();
        if (teData == null || teData.isEmpty()) return compoundTag;
        BlockPos blockPos = pos.subtract(lookingAt);
        Iterator<TagPos> iterator = teData.iterator();
        while (iterator.hasNext()) {
            TagPos data = iterator.next();
            if (data.pos.equals(blockPos)) {
                compoundTag = data.tag;
                iterator.remove();
                break;
            }
        }
        return compoundTag;
    }

    public Direction getDirection() {
        if (direction == -1) return null;
        return Direction.values()[direction];
    }
}

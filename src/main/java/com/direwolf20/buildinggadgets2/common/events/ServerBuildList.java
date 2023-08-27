package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public class ServerBuildList {
    public Level level;
    public ArrayList<StatePos> statePosList;
    public byte buildType;
    public Player player;
    public int originalSize;

    public ServerBuildList(Level level, ArrayList<StatePos> statePosList, byte buildType, Player player) {
        this.level = level;
        this.statePosList = statePosList;
        this.buildType = buildType;
        this.player = player;
        this.originalSize = statePosList.size();
    }
}

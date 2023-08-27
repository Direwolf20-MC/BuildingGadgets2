package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ServerTickHandler {

    public static final HashMap<UUID, ServerBuildList> buildMap = new HashMap<>();

    @SubscribeEvent
    public static void handleTickEndEvent(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || buildMap.isEmpty()) return;

        for (UUID uuid : buildMap.keySet()) {
            int min = buildMap.get(uuid).originalSize < 25 ? 1 : 5;
            int amountPerTick = Math.max((int) Math.floor(buildMap.get(uuid).originalSize / 300), min);
            for (int i = 0; i < amountPerTick; i++)
                build(uuid);
        }

        removeEmptyLists();
    }

    public static void addToMap(UUID buildUUID, StatePos statePos, Level level, byte buildType, Player player) {
        ServerBuildList serverBuildList = buildMap.computeIfAbsent(buildUUID, k -> new ServerBuildList(level, new ArrayList<>(), buildType, player));
        serverBuildList.statePosList.add(statePos);
        serverBuildList.originalSize = serverBuildList.statePosList.size();
    }

    public static void removeEmptyLists() {
        buildMap.entrySet().removeIf(entry -> entry.getValue().statePosList.isEmpty());
    }

    public static void build(UUID buildUUID) {
        ServerBuildList serverBuildList = buildMap.get(buildUUID);
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;
        BlockState blockState = statePos.state;

        if (!blockState.canSurvive(level, blockPos)) {
            statePosList.add(statePos);
            return;
        }

        boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

        if (!placed || be == null) {
            // this can happen when another mod rejects the set block state (fixes #120)
            return;
        }
        be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.buildType);
    }
}
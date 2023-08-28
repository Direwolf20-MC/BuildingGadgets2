package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

import static com.direwolf20.buildinggadgets2.util.BuildingUtils.giveItemToPlayer;
import static com.direwolf20.buildinggadgets2.util.BuildingUtils.removeStacksFromInventory;

public class ServerTickHandler {

    public static final HashMap<UUID, ServerBuildList> buildMap = new HashMap<>();

    @SubscribeEvent
    public static void handleTickEndEvent(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || buildMap.isEmpty()) return;

        for (UUID uuid : buildMap.keySet()) {
            ServerBuildList serverBuildList = buildMap.get(uuid);
            Player player = event.getServer().getPlayerList().getPlayer(serverBuildList.playerUUID);
            if (player == null) {
                stopBuilding(uuid); //Clear the remaining list of things to build, removing it after this loop in removeEmptyLists
                continue;
            }
            int min = serverBuildList.originalSize < 25 ? 1 : 5;
            int amountPerTick = Math.max((int) Math.floor(serverBuildList.originalSize / 300), min);
            for (int i = 0; i < amountPerTick; i++) {
                if (serverBuildList.isExchange)
                    exchange(serverBuildList, player);
                else
                    build(serverBuildList, player);
            }
        }

        removeEmptyLists();
    }

    public static void addToMap(UUID buildUUID, StatePos statePos, Level level, byte buildType, Player player, boolean neededItems, boolean returnItems, ItemStack gadget, boolean isExchange) {
        ServerBuildList serverBuildList = buildMap.computeIfAbsent(buildUUID, k -> new ServerBuildList(level, new ArrayList<>(), buildType, player.getUUID(), neededItems, returnItems, buildUUID, gadget, isExchange));
        serverBuildList.statePosList.add(statePos);
        serverBuildList.originalSize = serverBuildList.statePosList.size();
    }

    public static void stopBuilding(UUID buildUUID) {
        if (!buildMap.containsKey(buildUUID)) return;
        ServerBuildList serverBuildList = buildMap.get(buildUUID);
        serverBuildList.statePosList.clear();
    }

    public static void removeEmptyLists() {
        buildMap.entrySet().removeIf(entry -> entry.getValue().statePosList.isEmpty());
    }

    public static void build(ServerBuildList serverBuildList, Player player) {
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

        if (!level.getBlockState(blockPos).canBeReplaced()) return; //Return without placing the block

        boolean foundStacks = false;
        List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, blockState, player);
        if (!player.isCreative() && serverBuildList.needItems) {
            foundStacks = removeStacksFromInventory(player, neededItems, true);
            if (!foundStacks) return; //Return without placing the block
        }

        boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

        if (!placed || be == null) {
            // this can happen when another mod rejects the set block state (fixes #120)
            return;
        }

        if (!player.isCreative() && serverBuildList.needItems) {
            removeStacksFromInventory(player, neededItems, false);
        }
        serverBuildList.addToBuiltList(new StatePos(blockState, blockPos));
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
        be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.buildType);
    }

    public static void exchange(ServerBuildList serverBuildList, Player player) {
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

        boolean foundStacks = false;
        List<ItemStack> neededItems = new ArrayList<>();
        if (!player.isCreative() && serverBuildList.needItems) {
            if (!blockState.isAir()) {
                neededItems.addAll(GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, blockState, player));
                foundStacks = removeStacksFromInventory(player, neededItems, true);
                if (!foundStacks) return; //Return without placing the block
            }
        }

        BlockState oldState = level.getBlockState(blockPos);
        boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

        if (!placed || be == null) {
            // this can happen when another mod rejects the set block state (fixes #120)
            return;
        }

        if (!player.isCreative() && serverBuildList.needItems) {
            if (!blockState.isAir()) {
                removeStacksFromInventory(player, neededItems, false);
            }
        }

        if (!player.isCreative() && serverBuildList.returnItems && !oldState.isAir()) {
            List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockStateGadget((ServerLevel) level, blockPos, oldState, serverBuildList.gadget);
            for (ItemStack returnedItem : returnedItems)
                giveItemToPlayer(player, returnedItem);
        }

        serverBuildList.addToBuiltList(new StatePos(oldState, blockPos));
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
        be.setRenderData(oldState, blockState, serverBuildList.buildType);
    }
}
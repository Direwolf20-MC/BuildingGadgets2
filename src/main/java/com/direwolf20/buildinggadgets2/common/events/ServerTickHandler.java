package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.GadgetUtils;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

import static com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste.customCutValidation;
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
                if (serverBuildList.buildType.equals(ServerBuildList.BuildType.BUILD))
                    build(serverBuildList, player);
                else if (serverBuildList.buildType.equals(ServerBuildList.BuildType.EXCHANGE))
                    exchange(serverBuildList, player);
                else if (serverBuildList.buildType.equals(ServerBuildList.BuildType.DESTROY))
                    remove(serverBuildList, player);
                else if (serverBuildList.buildType.equals(ServerBuildList.BuildType.UNDO_DESTROY))
                    undoDestroy(serverBuildList, player);
                else if (serverBuildList.buildType.equals(ServerBuildList.BuildType.CUT))
                    cut(serverBuildList, player);
            }
        }

        removeEmptyLists();
    }

    public static void addToMap(UUID buildUUID, StatePos statePos, Level level, byte renderType, Player player, boolean neededItems, boolean returnItems, ItemStack gadget, ServerBuildList.BuildType buildType, boolean dropContents, BlockPos lookingAt) {
        ServerBuildList serverBuildList = buildMap.computeIfAbsent(buildUUID, k -> new ServerBuildList(level, new ArrayList<>(), renderType, player.getUUID(), neededItems, returnItems, buildUUID, gadget, buildType, dropContents, lookingAt));
        serverBuildList.statePosList.add(statePos);
        serverBuildList.originalSize = serverBuildList.statePosList.size();
    }

    public static void addTEData(UUID buildUUID, ArrayList<TagPos> teData) {
        ServerBuildList serverBuildList = buildMap.get(buildUUID);
        if (serverBuildList == null) return;
        serverBuildList.teData = teData;
    }

    public static boolean gadgetWorking(UUID gadgetUUID) {
        return buildMap.values().stream().anyMatch(e -> GadgetNBT.getUUID(e.gadget).equals(gadgetUUID));
    }

    public static void setCutStart(UUID buildUUID, BlockPos cutStart) {
        ServerBuildList serverBuildList = buildMap.get(buildUUID);
        serverBuildList.cutStart = cutStart;
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
            if (serverBuildList.retryList.contains(blockPos))
                return; //Don't retry if this is already retried
            statePosList.add(statePos);
            serverBuildList.retryList.add(blockPos); //Only retry once!
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
        be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.renderType);
        CompoundTag compoundTag = serverBuildList.getTagForPos(blockPos);
        if (!compoundTag.isEmpty())
            be.setBlockEntityData(compoundTag);
    }

    public static void exchange(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;
        BlockState blockState = statePos.state;
        BlockState oldState = level.getBlockState(blockPos);
        byte drawSize = RenderBlockBE.getMaxSize();

        if (oldState.equals(blockState)) return; //No need to replace blocks if they match!

        if (!blockState.canSurvive(level, blockPos)) {
            if (serverBuildList.retryList.contains(blockPos))
                return; //Don't retry if this is already retried
            statePosList.add(statePos);
            serverBuildList.retryList.add(blockPos); //Only retry once!
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


        boolean placed = false;

        //Handles situations where we are undoing an exchange
        BlockState oldRenderState = level.getBlockState(blockPos);
        if (oldState.getBlock() instanceof RenderBlock) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                oldState = renderBlockBE.targetBlock;
                oldRenderState = renderBlockBE.renderBlock;
                drawSize = renderBlockBE.drawSize;
                placed = true;
            }
        } else {
            placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
        }

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

        if (oldRenderState.equals(blockState))
            be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.renderType);
        else
            be.setRenderData(oldState, blockState, serverBuildList.renderType);
        be.drawSize = drawSize;

        serverBuildList.addToBuiltList(new StatePos(oldState, blockPos));
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        if (bg2Data.containsUndoList(serverBuildList.buildUUID))
            bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
        //be.setRenderData(oldState, blockState, serverBuildList.renderType);
    }

    public static void remove(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;

        byte drawSize = RenderBlockBE.getMaxSize();

        BlockState oldState = level.getBlockState(blockPos);
        if (oldState.isAir() || !GadgetUtils.isValidBlockState(oldState, level, blockPos))
            return; //Return without processing
        if (oldState.getBlock() instanceof RenderBlock) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                oldState = renderBlockBE.renderBlock;
                drawSize = renderBlockBE.drawSize;
            }
        }

        if (!serverBuildList.dropContents)
            level.removeBlockEntity(blockPos); //Calling this prevents chests from dropping their contents, so only do it if we don't care about the drops (Like cut)
        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 48);
        StatePos affectedBlock = new StatePos(oldState, blockPos);

        if (serverBuildList.returnItems) {
            List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, oldState, player);
            for (ItemStack returnedItem : returnedItems)
                giveItemToPlayer(player, returnedItem);
        }

        boolean placed = level.setBlock(affectedBlock.pos, Registration.RenderBlock.get().defaultBlockState(), 3);
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(affectedBlock.pos);
        if (placed && be != null) {
            be.setRenderData(affectedBlock.state, Blocks.AIR.defaultBlockState(), serverBuildList.renderType);
            be.drawSize = drawSize;
        }

        serverBuildList.addToBuiltList(affectedBlock);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        if (bg2Data.containsUndoList(serverBuildList.buildUUID))
            bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
    }

    public static void undoDestroy(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;
        BlockState blockState = statePos.state;
        byte drawSize = RenderBlockBE.getMaxSize();

        if (blockState.isAir()) return; //Do nothing if the old state was Air

        BlockState oldState = level.getBlockState(blockPos);
        if (!oldState.canBeReplaced() && !(oldState.getBlock() instanceof RenderBlock))
            return; //Don't overwrite any blocks that have been placed since destroying - only air or replacables like grass/water.

        if ((oldState.getBlock() instanceof RenderBlock)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                drawSize = renderBlockBE.drawSize;
                renderBlockBE.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.renderType);
                renderBlockBE.drawSize = drawSize;
            }
        } else {
            boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

            if (!placed || be == null) {
                // this can happen when another mod rejects the set block state (fixes #120)
                return;
            }
            be.setRenderData(oldState, blockState, serverBuildList.renderType);
        }
    }

    public static void cut(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;
        BlockState blockState = level.getBlockState(blockPos);
        boolean doRemove = false;

        if (GadgetUtils.isValidBlockState(blockState, level, blockPos) && customCutValidation(blockState, level, player, blockPos)) {
            serverBuildList.actuallyBuildList.add(new StatePos(blockState, blockPos.subtract(serverBuildList.cutStart)));
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!blockState.isAir()) //Don't remove air - also used to detect how many blocks are actually removed
                doRemove = true;
            if (blockEntity != null) {
                CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                TagPos tagPos = new TagPos(blockTag, blockPos.subtract(serverBuildList.cutStart));
                serverBuildList.teData.add(tagPos);
            }
        } else {
            serverBuildList.actuallyBuildList.add(new StatePos(Blocks.AIR.defaultBlockState(), blockPos.subtract(serverBuildList.cutStart))); //We need to have a block in EVERY position, so write air if invalid
        }

        //Update world data
        UUID uuid = GadgetNBT.getUUID(serverBuildList.gadget);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
        bg2Data.addToCopyPaste(uuid, serverBuildList.actuallyBuildList);
        bg2Data.addToTEMap(uuid, serverBuildList.teData);

        //Remove blocks from world if appropriate (Not air!)
        if (doRemove) {
            level.removeBlockEntity(blockPos); //Calling this prevents chests from dropping their contents, so only do it if we don't care about the drops (Like cut)
            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 48);
            StatePos affectedBlock = new StatePos(blockState, blockPos);

            boolean placed = level.setBlock(affectedBlock.pos, Registration.RenderBlock.get().defaultBlockState(), 3);
            RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(affectedBlock.pos);
            if (placed && be != null) {
                be.setRenderData(affectedBlock.state, Blocks.AIR.defaultBlockState(), serverBuildList.renderType);
            }
        }
    }
}
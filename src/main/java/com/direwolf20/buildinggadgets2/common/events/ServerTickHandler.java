package com.direwolf20.buildinggadgets2.common.events;

import com.direwolf20.buildinggadgets2.common.blockentities.RenderBlockBE;
import com.direwolf20.buildinggadgets2.common.blocks.RenderBlock;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.DimBlockPos;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

import static com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste.customCutValidation;
import static com.direwolf20.buildinggadgets2.util.BuildingUtils.*;

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
            int min = serverBuildList.originalSize < 60 ? 1 : 5;
            int max = 50;
            int amountPerTick = Math.min(Math.max((int) Math.floor(serverBuildList.originalSize / 300), min), max);
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

        removeEmptyLists(event);
    }

    public static void addToMap(UUID buildUUID, StatePos statePos, Level level, byte renderType, Player player, boolean neededItems, boolean returnItems, ItemStack gadget, ServerBuildList.BuildType buildType, boolean dropContents, BlockPos lookingAt) {
        DimBlockPos boundPos = GadgetNBT.getBoundPos(gadget);
        int direction = boundPos == null ? -1 : GadgetNBT.getToolValue(gadget, "binddirection");
        ServerBuildList serverBuildList = buildMap.computeIfAbsent(buildUUID, k -> new ServerBuildList(level, new ArrayList<>(), renderType, player.getUUID(), neededItems, returnItems, buildUUID, gadget, buildType, dropContents, lookingAt, boundPos, direction));
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
        if (serverBuildList.buildType.equals(ServerBuildList.BuildType.CUT)) { // should always be the case!
            for (StatePos statePos : serverBuildList.statePosList)
                serverBuildList.actuallyBuildList.add(new StatePos(Blocks.VOID_AIR.defaultBlockState(), statePos.pos.subtract(serverBuildList.cutStart))); //Fill the actually built list with void air, in case the cut gets interrupted by player logoff
        }
    }

    public static void stopBuilding(UUID buildUUID) {
        if (!buildMap.containsKey(buildUUID)) return;
        ServerBuildList serverBuildList = buildMap.get(buildUUID);
        serverBuildList.statePosList.clear();
    }

    public static void removeEmptyLists(TickEvent.ServerTickEvent event) {
        Iterator<Map.Entry<UUID, ServerBuildList>> iterator = buildMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ServerBuildList> entry = iterator.next();
            ServerBuildList serverBuildList = entry.getValue();
            if (entry.getValue().statePosList.isEmpty()) {
                Player player = event.getServer().getPlayerList().getPlayer(serverBuildList.playerUUID); //We check for the player - if they exist, they finished building - if not they logged off. Remove data from map only if finished building
                if (serverBuildList.teData != null && !serverBuildList.buildType.equals(ServerBuildList.BuildType.CUT) && player != null) { //If we had teData this was from a cut-Paste, so remove the data from world data if we're not cutting
                    BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(serverBuildList.level.getServer()).overworld());
                    bg2Data.getCopyPasteList(GadgetNBT.getUUID(serverBuildList.gadget), true); //Remove the data
                    bg2Data.getTEMap(GadgetNBT.getUUID(serverBuildList.gadget)); //Remove the TE data
                    bg2Data.popUndoList(GadgetNBT.getUUID(serverBuildList.gadget)); //Remove the undo list, which tracks partial placements
                }
                iterator.remove();
            }
        }
    }

    public static void build(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        StatePos statePos = statePosList.remove(0);
        if (statePos.state.equals(Blocks.VOID_AIR.defaultBlockState()))
            return; //Void_AIR is used for blocks we want to skip
        ArrayList<StatePos> undoList = bg2Data.peekUndoList(GadgetNBT.getUUID(serverBuildList.gadget));
        if (undoList != null && undoList.contains(statePos))
            return; //This really only happens if a cut/paste got interrupted mid-build by a server stop or player logoff

        BlockPos blockPos = statePos.pos.offset(serverBuildList.lookingAt);
        BlockState blockState = statePos.state;

        if (!blockState.getFluidState().isEmpty()) {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000);
                boolean canDestContainFluid = !fluid.getFluidType().isVaporizedOnPlacement(level, blockPos, fluidStack);
                if (!canDestContainFluid) return; //Skip -- This is for like, water in the nether and stuff
            }
        }

        if (!blockState.canSurvive(level, blockPos)) {
            if (serverBuildList.retryList.contains(blockPos))
                return; //Don't retry if this is already retried
            statePosList.add(statePos); //Retry placing this after all other blocks are placed - in case torches are placed before their supporting block for example
            serverBuildList.retryList.add(blockPos); //Only retry once!
            return;
        }

        if (!level.getBlockState(blockPos).canBeReplaced()) return; //Return without placing the block

        List<ItemStack> neededItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, blockState, player);
        if (blockState.getFluidState().isEmpty()) { //Check for items
            if (!player.isCreative() && serverBuildList.needItems) {
                if (!removeStacksFromInventory(player, neededItems, true, serverBuildList.boundPos, serverBuildList.getDirection()))
                    return; //Return without placing the block
            }
        } else {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                if (!player.isCreative() && serverBuildList.needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    if (!removeFluidStacksFromInventory(player, fluidStack, true, serverBuildList.boundPos, serverBuildList.getDirection()))
                        return; //Return without placing the block
                }
            }
        }

        boolean placed = level.setBlockAndUpdate(blockPos, Registration.RenderBlock.get().defaultBlockState());
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(blockPos);

        if (!placed || be == null) {
            // this can happen when another mod rejects the set block state (fixes #120)
            return;
        }
        if (blockState.getFluidState().isEmpty()) { //Check for items
            if (!player.isCreative() && serverBuildList.needItems) {
                removeStacksFromInventory(player, neededItems, false, serverBuildList.boundPos, serverBuildList.getDirection());
            }
        } else {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                if (!player.isCreative() && serverBuildList.needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    removeFluidStacksFromInventory(player, fluidStack, false, serverBuildList.boundPos, serverBuildList.getDirection());
                }
            }
        }

        be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.renderType);

        if (serverBuildList.teData == null && bg2Data.containsUndoList(serverBuildList.buildUUID)) { //Only track 'real undos' for non cut-pasted data
            serverBuildList.addToBuiltList(new StatePos(blockState, blockPos));
            bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
        }

        if (serverBuildList.teData != null) { //If theres ANY TE data (even an empty list), we are doing a cut paste
            serverBuildList.addToBuiltList(new StatePos(blockState, statePos.pos)); //Add the non-adjust blockpos to the list for reference later
            bg2Data.addToUndoList(GadgetNBT.getUUID(serverBuildList.gadget), serverBuildList.actuallyBuildList, level);

            CompoundTag compoundTag = serverBuildList.getTagForPos(blockPos); //First check if theres TE data for this block
            if (!compoundTag.isEmpty()) {
                be.setBlockEntityData(compoundTag);
                bg2Data.addToTEMap(GadgetNBT.getUUID(serverBuildList.gadget), serverBuildList.teData); //If the server crashes mid-build you'll maybe dupe blocks but at least not dupe TE data? TODO Improve
            }
        }
    }

    public static void exchange(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);
        if (statePos.state.equals(Blocks.VOID_AIR.defaultBlockState()))
            return; //Void_AIR is used for blocks we wanna skip

        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(level.getServer()).overworld());
        ArrayList<StatePos> undoList = bg2Data.peekUndoList(GadgetNBT.getUUID(serverBuildList.gadget));
        if (undoList != null && undoList.contains(statePos))
            return; //This really only happens if a cut/paste got interrupted mid-build by a server stop or player logoff


        BlockPos blockPos = statePos.pos;
        if (!serverBuildList.lookingAt.equals(GadgetNBT.nullPos)) //This only happens when undoing an exchange - we don't wanna offset in this case
            blockPos = statePos.pos.offset(serverBuildList.lookingAt);
        BlockState blockState = statePos.state;
        BlockState oldState = level.getBlockState(blockPos);
        byte drawSize = -1;

        if (oldState.equals(blockState)) return; //No need to replace blocks if they match!

        if (!blockState.getFluidState().isEmpty()) {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000);
                boolean canDestContainFluid = !fluid.getFluidType().isVaporizedOnPlacement(level, blockPos, fluidStack);
                if (!canDestContainFluid) return; //Skip -- This is for like, water in the nether and stuff
            }
        }

        if (!blockState.canSurvive(level, blockPos)) {
            if (serverBuildList.retryList.contains(blockPos))
                return; //Don't retry if this is already retried
            statePosList.add(statePos);
            serverBuildList.retryList.add(blockPos); //Only retry once!
            return;
        }

        List<ItemStack> neededItems = new ArrayList<>();
        if (blockState.getFluidState().isEmpty()) { //Check for Items
            if (!player.isCreative() && serverBuildList.needItems) {
                if (!blockState.isAir()) {
                    neededItems.addAll(GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, blockState, player));
                    if (!removeStacksFromInventory(player, neededItems, true, serverBuildList.boundPos, serverBuildList.getDirection()))
                        return; //Return without placing the block
                }
            }
        } else {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                if (!player.isCreative() && serverBuildList.needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    if (!removeFluidStacksFromInventory(player, fluidStack, true, serverBuildList.boundPos, serverBuildList.getDirection()))
                        return; //Return without placing the block
                }
            }
        }


        boolean placed = false;

        //Handles situations where we are undoing an exchange
        BlockState oldRenderState = oldState;
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

        if (blockState.getFluidState().isEmpty()) { //Check for Items
            if (!player.isCreative() && serverBuildList.needItems) {
                if (!blockState.isAir()) {
                    removeStacksFromInventory(player, neededItems, false, serverBuildList.boundPos, serverBuildList.getDirection());
                }
            }
        } else {
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                Fluid fluid = fluidState.getType();
                FluidStack fluidStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                if (!player.isCreative() && serverBuildList.needItems) { //Check if player has needed items before using energy -- a real check happens again in ServerTicks
                    removeFluidStacksFromInventory(player, fluidStack, false, serverBuildList.boundPos, serverBuildList.getDirection());
                }
            }
        }

        if (!player.isCreative() && serverBuildList.returnItems && !oldState.isAir()) {
            if (!oldState.getFluidState().isEmpty()) {
                FluidState fluidState = oldState.getFluidState();
                if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                    Fluid fluid = fluidState.getType();
                    FluidStack returnStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                    giveFluidToPlayer(player, returnStack, serverBuildList.boundPos, serverBuildList.getDirection());
                }
            } else {
                List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockStateGadget((ServerLevel) level, blockPos, oldState, serverBuildList.gadget);
                for (ItemStack returnedItem : returnedItems)
                    giveItemToPlayer(player, returnedItem, serverBuildList.boundPos, serverBuildList.getDirection());
            }
        }

        if (oldRenderState.equals(blockState))
            be.setRenderData(Blocks.AIR.defaultBlockState(), blockState, serverBuildList.renderType);
        else
            be.setRenderData(oldState, blockState, serverBuildList.renderType);
        if (drawSize != -1) //Only if changed from default
            be.drawSize = drawSize;

        if (serverBuildList.teData == null && bg2Data.containsUndoList(serverBuildList.buildUUID)) {
            serverBuildList.addToBuiltList(new StatePos(oldState, blockPos));
            bg2Data.addToUndoList(serverBuildList.buildUUID, serverBuildList.actuallyBuildList, level);
        }
        if (serverBuildList.teData != null) { //If theres ANY TE data (even an empty list), we are doing a cut paste
            serverBuildList.addToBuiltList(new StatePos(blockState, statePos.pos)); //Add the non-adjust blockpos to the list for reference later
            bg2Data.addToUndoList(GadgetNBT.getUUID(serverBuildList.gadget), serverBuildList.actuallyBuildList, level);

            CompoundTag compoundTag = serverBuildList.getTagForPos(blockPos); //First check if theres TE data for this block
            if (!compoundTag.isEmpty()) {
                be.setBlockEntityData(compoundTag);
                bg2Data.addToTEMap(GadgetNBT.getUUID(serverBuildList.gadget), serverBuildList.teData); //If the server crashes mid-build you'll maybe dupe blocks but at least not dupe TE data? TODO Improve
            }
        }
    }

    public static void remove(ServerBuildList serverBuildList, Player player) {
        Level level = serverBuildList.level;

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;

        byte drawSize = -99;

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
            if (!oldState.getFluidState().isEmpty()) {
                FluidState fluidState = oldState.getFluidState();
                if (!fluidState.isEmpty() && fluidState.isSource()) { //This should always be true since we only copy sources
                    Fluid fluid = fluidState.getType();
                    FluidStack returnStack = new FluidStack(fluid, 1000); //Sources are always 1000, right?
                    giveFluidToPlayer(player, returnStack, serverBuildList.boundPos, serverBuildList.getDirection());
                }
            } else {
                List<ItemStack> returnedItems = GadgetUtils.getDropsForBlockState((ServerLevel) level, blockPos, oldState, player);
                for (ItemStack returnedItem : returnedItems)
                    giveItemToPlayer(player, returnedItem, serverBuildList.boundPos, serverBuildList.getDirection());
            }
        }

        boolean placed = level.setBlock(affectedBlock.pos, Registration.RenderBlock.get().defaultBlockState(), 3);
        RenderBlockBE be = (RenderBlockBE) level.getBlockEntity(affectedBlock.pos);
        if (placed && be != null) {
            be.setRenderData(affectedBlock.state, Blocks.AIR.defaultBlockState(), serverBuildList.renderType);
            if (drawSize != -99)
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

        if (blockState.isAir()) return; //Do nothing if the old state was Air

        BlockState oldState = level.getBlockState(blockPos);
        if (!oldState.canBeReplaced() && !(oldState.getBlock() instanceof RenderBlock))
            return; //Don't overwrite any blocks that have been placed since destroying - only air or replacables like grass/water.

        if ((oldState.getBlock() instanceof RenderBlock)) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof RenderBlockBE renderBlockBE) {
                byte drawSize = renderBlockBE.drawSize;
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
        if (serverBuildList.teData == null)
            serverBuildList.teData = new ArrayList<>(); //Initialize the list since it isn't done in the ServerBuildList class

        ArrayList<StatePos> statePosList = serverBuildList.statePosList;
        if (statePosList.isEmpty()) return;
        StatePos statePos = statePosList.remove(0);

        BlockPos blockPos = statePos.pos;
        BlockState blockState = level.getBlockState(blockPos);
        boolean doRemove = false;

        if (GadgetUtils.isValidBlockState(blockState, level, blockPos) && customCutValidation(blockState, level, player, blockPos)) {
            serverBuildList.updateActuallyBuiltList(new StatePos(blockState, blockPos.subtract(serverBuildList.cutStart)));
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (!blockState.isAir()) //Don't remove air
                doRemove = true;
            if (blockEntity != null) {
                CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                TagPos tagPos = new TagPos(blockTag, blockPos.subtract(serverBuildList.cutStart));
                serverBuildList.teData.add(tagPos);
            }
        } else { //All blocks in a cut-paste area need to be populated, so fill in air for blocks we skip.
            serverBuildList.updateActuallyBuiltList(new StatePos(Blocks.AIR.defaultBlockState(), blockPos.subtract(serverBuildList.cutStart))); //We need to have a block in EVERY position, so write air if invalid
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
package com.direwolf20.buildinggadgets2.common.items;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetTarget;
import com.direwolf20.buildinggadgets2.common.events.ServerBuildList;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.datagen.BG2BlockTags;
import com.direwolf20.buildinggadgets2.setup.Config;
import com.direwolf20.buildinggadgets2.util.BuildingUtils;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import com.direwolf20.buildinggadgets2.util.modes.Cut;
import com.direwolf20.buildinggadgets2.util.modes.Paste;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class GadgetCutPaste extends BaseGadget {
    public GadgetCutPaste() {
        super();
    }

    @Override
    public int getEnergyMax() {
        return Config.CUTPASTEGADGET_MAXPOWER.get();
    }

    @Override
    public int getEnergyCost() {
        return Config.CUTPASTEGADGET_COST.get();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Minecraft mc = Minecraft.getInstance();

        if (level == null || mc.player == null) {
            return;
        }

        if (GadgetNBT.getPasteReplace(stack))
            tooltip.add(Component.translatable("buildinggadgets2.voidwarning").withStyle(ChatFormatting.RED));

        boolean sneakPressed = Screen.hasShiftDown();

        if (sneakPressed) {

        }
    }

    @Override
    InteractionResultHolder<ItemStack> onAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("cut")) {
            GadgetNBT.setCopyStartPos(gadget, context.pos());
            //buildAndStore(context, gadget);
        } else if (mode.getId().getPath().equals("paste")) {
            UUID uuid = GadgetNBT.getUUID(gadget);
            if (ServerTickHandler.gadgetWorking(GadgetNBT.getUUID(gadget))) {
                context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.cutinprogress"), true);
                return InteractionResultHolder.pass(gadget); // Do nothing if this gadget is already doing stuff!
            }
            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());
            ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(uuid, false); //Don't remove the data just yet
            ArrayList<TagPos> tagList = bg2Data.peekTEMap(uuid);

            // This should go through some translation based process
            // mode -> beforeBuild (validation) -> scheduleBuild / Build -> afterBuild (cleanup & use of items etc)
            ArrayList<StatePos> actuallyBuiltList = BuildingUtils.buildWithTileData(context.level(), context.player(), buildList, getHitPos(context).above().offset(GadgetNBT.getRelativePaste(gadget)), tagList, gadget);
            if (!actuallyBuiltList.isEmpty())
                GadgetNBT.clearAnchorPos(gadget);
            //GadgetNBT.clearCopyUUID(gadget); // Erase copy UUID so the user doesn't get the 'are you sure' prompt
            GadgetNBT.setMode(gadget, new Cut()); // Set it back to cut mode - no need to stay in paste since the paste is gone :)
            return InteractionResultHolder.success(gadget);
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    /**
     * Selects the block assuming you're actually looking at one
     */
    @Override
    InteractionResultHolder<ItemStack> onShiftAction(ItemActionContext context) {
        var gadget = context.stack();

        var mode = GadgetNBT.getMode(gadget);
        if (mode.getId().getPath().equals("cut")) {
            GadgetNBT.setCopyEndPos(gadget, context.pos());
            //buildAndStore(context, gadget);
        } else if (mode.equals(new Paste())) {
            //Paste
        } else {
            return InteractionResultHolder.pass(gadget);
        }

        return InteractionResultHolder.success(gadget);
    }

    public void cutAndStore(Player player, ItemStack gadget) {
        if (ServerTickHandler.gadgetWorking(GadgetNBT.getUUID(gadget)))
            return; // Do nothing if this gadget is already doing stuff!
        ArrayList<StatePos> buildList = new ArrayList<>();
        ArrayList<TagPos> teData = new ArrayList<>();
        UUID buildUUID = UUID.randomUUID();
        ItemStack heldItem = BaseGadget.getGadget(player);
        if (!(heldItem.getItem() instanceof GadgetCutPaste gadgetCutPaste)) return; //Impossible....right?
        Level level = player.level();
        BlockPos cutStart = GadgetNBT.getCopyStartPos(heldItem);
        BlockPos cutEnd = GadgetNBT.getCopyEndPos(heldItem);

        if (cutStart.equals(GadgetNBT.nullPos) || cutEnd.equals(GadgetNBT.nullPos)) return;

        AABB area = new AABB(cutStart, cutEnd);

        Stream<BlockPos> areaStream = BlockPos.betweenClosedStream(area);
        long size = areaStream.count();
        int maxSize = 100000; //Todo Config?
        if (size > maxSize) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.areatoolarge", maxSize, size), false);
            return;
        }

        int totalCost = gadgetCutPaste.getEnergyCost() * (int) size;
        if (!player.isCreative() && !BuildingUtils.hasEnoughEnergy(heldItem, totalCost)) {
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.notenoughenergy", totalCost, BuildingUtils.getEnergyStored(heldItem)), false);
            return;
        }

        //ArrayList<BlockPos> removeList = new ArrayList<>();
        BlockPos.betweenClosedStream(area).map(BlockPos::immutable).forEach(pos -> {
            ServerTickHandler.addToMap(buildUUID, new StatePos(Blocks.AIR.defaultBlockState(), pos), level, GadgetNBT.getRenderTypeByte(gadget), player, false, false, gadget, ServerBuildList.BuildType.CUT, false, BlockPos.ZERO);
            /*if (GadgetUtils.isValidBlockState(level.getBlockState(pos), level, pos) && customCutValidation(level.getBlockState(pos), level, player, pos)) {
                buildList.add(new StatePos(level.getBlockState(pos), pos.subtract(cutStart)));
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (!level.getBlockState(pos).isAir()) //Don't remove air - also used to detect how many blocks are actually removed
                    removeList.add(pos);
                if (blockEntity != null) {
                    CompoundTag blockTag = blockEntity.saveWithFullMetadata();
                    TagPos tagPos = new TagPos(blockTag, pos.subtract(cutStart));
                    teData.add(tagPos);
                }
            } else {
                buildList.add(new StatePos(Blocks.AIR.defaultBlockState(), pos.subtract(cutStart))); //We need to have a block in EVERY position, so write air if invalid
            //}*/
        });
        ServerTickHandler.setCutStart(buildUUID, cutStart);
        GadgetNBT.setCopyStartPos(gadget, GadgetNBT.nullPos);
        GadgetNBT.setCopyEndPos(gadget, GadgetNBT.nullPos);
        player.displayClientMessage(Component.translatable("buildinggadgets2.messages.cutblocks", size), true);
        //if (removeList.isEmpty())
        //    return; //The remove list is how many blocks we're removing (Not air) so validate on that
        //BuildingUtils.remove(level, player, removeList, false, false, heldItem);

        UUID uuid = GadgetNBT.getUUID(gadget);
        GadgetNBT.setCopyUUID(gadget, buildUUID); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(player.level().getServer()).overworld());
        bg2Data.addToCopyPaste(uuid, buildList);
        bg2Data.addToTEMap(uuid, teData);
        //new Cut().removeBlocks(player);
    }

    public static boolean customCutValidation(BlockState blockState, Level level, Player player, BlockPos blockPos) {
        if (blockState.is(BG2BlockTags.NO_MOVE)) return false;
        if (!level.mayInteract(player, blockPos)) return false; //Chunk Protection like spawn and FTB Utils
        return true;
    }

    /**
     * Used to retrieve the correct building modes in various places
     */
    @Override
    public GadgetTarget gadgetTarget() {
        return GadgetTarget.CUTPASTE;
    }
}

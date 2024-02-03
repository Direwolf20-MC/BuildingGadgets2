package com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
import com.direwolf20.buildinggadgets2.common.items.*;
import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.VectorHelper;
import com.direwolf20.buildinggadgets2.util.context.ItemActionContext;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.collect.ImmutableSortedSet;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public enum ActionGadget {
    ANCHOR((context) -> {
        var player = context.player();
        var gadgetStack = context.gadget();

        // If the anchor is already set, clear it
        if (!GadgetNBT.getAnchorPos(gadgetStack).equals(GadgetNBT.nullPos)) {
            GadgetNBT.clearAnchorPos(gadgetStack);
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorcleared"), true);
            return;
        }

        BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadgetStack);
        BlockPos lookingAtPos = lookingAt.getBlockPos();
        BlockState lookingAtState = player.level().getBlockState(lookingAtPos);
        if (lookingAtState.isAir()) return;
        GadgetNBT.setAnchorPos(gadgetStack, lookingAtPos);
        GadgetNBT.setAnchorSide(gadgetStack, lookingAt.getDirection());
        if (gadgetStack.getItem() instanceof GadgetBuilding || gadgetStack.getItem() instanceof GadgetExchanger) {
            BlockState renderBlockState = GadgetNBT.getGadgetBlockState(gadgetStack);
            if (renderBlockState.isAir()) return;
            BaseMode mode = GadgetNBT.getMode(gadgetStack);
            ArrayList<StatePos> buildList = mode.collect(lookingAt.getDirection(), player, lookingAtPos, renderBlockState);
            ArrayList<BlockPos> blockPosList = new ArrayList<>();
            buildList.forEach(e -> blockPosList.add(e.pos));
            GadgetNBT.setAnchorList(gadgetStack, blockPosList);
        }
        player.displayClientMessage(Component.translatable("buildinggadgets2.messages.anchorset").append(lookingAtPos.toShortString()), true);
    }),
    COPY_COORDS(context -> {
        var player = context.player();
        var gadgetStack = context.gadget();

        // Read the nbt data from the context using our BiPos codec
        // Hehe, good luck dire :P
        lazyCodecRead(GadgetActionCodecs.BiPos.CODEC, context.payload().metaData(), biPos -> {
            GadgetNBT.setCopyStartPos(gadgetStack, biPos.startPos());
            GadgetNBT.setCopyEndPos(gadgetStack, biPos.endPos());

            if (gadgetStack.getItem() instanceof GadgetCopyPaste gadgetCopyPaste) {
                BlockHitResult lookingAt = VectorHelper.getLookingAt(player, gadgetStack);
                ItemActionContext itemContext = new ItemActionContext(lookingAt.getBlockPos(), lookingAt, player, player.level(), InteractionHand.MAIN_HAND, gadgetStack);
                gadgetCopyPaste.buildAndStore(itemContext, gadgetStack);
            }
        }, error -> {
            // TODO: Translate
            player.displayClientMessage(Component.translatable("buildinggadgets2.messages.copycoordsfailed"), true);
        });
    }),
    CUT(context -> {
        var gadget = context.gadget();
        if (gadget.getItem() instanceof GadgetCutPaste gadgetCopyPaste) {
            gadgetCopyPaste.cutAndStore(context.player(), gadget);
        }
    }),
    DESTRUCTION_RANGES(context -> {
        lazyCodecRead(GadgetActionCodecs.DestructionRanges.CODEC, context.payload().metaData(), destructionRanges -> {
            GadgetNBT.setToolValue(context.gadget(), destructionRanges.left(), "left");
            GadgetNBT.setToolValue(context.gadget(), destructionRanges.right(), "right");
            GadgetNBT.setToolValue(context.gadget(), destructionRanges.up(), "up");
            GadgetNBT.setToolValue(context.gadget(), destructionRanges.down(), "down");
            GadgetNBT.setToolValue(context.gadget(), destructionRanges.depth(), "depth");
        }, error -> {
            // TODO: Translate
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.copycoordsfailed"), true);
        });
    }),
    MODE_SWITCH(context -> {
        lazyCodecRead(GadgetActionCodecs.ModeSwitch.CODEC, context.payload().metaData(), modeSwitch -> {
            var gadget = context.gadget();

            // This is safe as it's checked higher up
            BaseGadget actualGadget = (BaseGadget) context.gadget().getItem();
            if (modeSwitch.rotate()) {
                actualGadget.rotateModes(gadget);
            }

            ResourceLocation modeId = modeSwitch.modeId();
            ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(actualGadget.gadgetTarget());

            var modeToUse = modesForGadget
                    .stream()
                    .filter(e -> e.getId().equals(modeId))
                    .findFirst()
                    .orElse(modesForGadget.first());

            GadgetNBT.setMode(gadget, modeToUse);
        }, error -> {
            // TODO: Translate
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.copycoordsfailed"), true);
        });
    }),
    RANGE_CHANGE(context -> {
        // This only requires an int
        // TODO: Ensure this is valid...
        int range = context.payload().metaData().getInt("range");
        GadgetNBT.setToolRange(context.gadget(), range);
        context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.range_set", range), true);
    }),
    RELATIVE_PASTE(context -> {
        // This only requires an blockpos so we'll use the blockpos codec
        lazyCodecRead(BlockPos.CODEC, context.payload().metaData(), relativePos -> {
            GadgetNBT.setRelativePaste(context.gadget(), relativePos);
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.relativepaste", relativePos.toShortString()), true);
        }, error -> {
            // TODO: Translate
            context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.copycoordsfailed"), true);
        });
    }),
    RENDER_CHANGE(context -> {
        // This only requires a byte
        byte renderType = context.payload().metaData().getByte("renderType");
        GadgetNBT.setRenderType(context.gadget(), renderType);
        context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.render_set", Component.translatable(GadgetNBT.getRenderType(context.gadget()).getLang())), true);
    }),
    ROTATE(context -> {
        var gadget = context.gadget();

        UUID gadgetUUID = GadgetNBT.getUUID(gadget);

        if (gadget.getItem() instanceof GadgetCutPaste) {
            if (ServerTickHandler.gadgetWorking(gadgetUUID)) {
                context.player().displayClientMessage(Component.translatable("buildinggadgets2.messages.cutinprogress"), true);
                return; //If the gadget is mid cut, don't sync data
            }
        }

        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());

        ArrayList<StatePos> currentPosList = bg2Data.getCopyPasteList(gadgetUUID, false);
        ArrayList<TagPos> tagListMutable = bg2Data.peekTEMap(gadgetUUID);

        ArrayList<StatePos> newPosList = StatePos.rotate90Degrees(currentPosList, tagListMutable);

        bg2Data.addToCopyPaste(gadgetUUID, newPosList);
        GadgetNBT.setCopyUUID(gadget);

        //Handle TE Data - do nothing if null or empty
        if (tagListMutable == null || tagListMutable.isEmpty()) return;
        bg2Data.addToTEMap(gadgetUUID, tagListMutable);
    }),
    SEND_COPY_DATA_TO_SERVER(context -> {
        // We're gunna receive compound tag data so just use that's codec
        lazyCodecRead(CompoundTag.CODEC, context.payload().metaData(), compoundTag -> {
            AbstractContainerMenu container = context.player().containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack templateStack = container.getSlot(1).getItem();

            if (templateStack.isEmpty())
                return;

            if (templateStack.is(Items.PAPER)) {
                container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                templateStack = container.getSlot(1).getItem();
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());

            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(compoundTag);
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack);

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);

            ((ServerPlayer) context.player()).connection.send(
                    new SendCopyDataPayload(
                            GadgetNBT.getUUID(templateStack),
                            GadgetNBT.getCopyUUID(templateStack),
                            tag
                    )
            );
        }, error -> {

        });
    }),
    TOGGLE_SETTING(context -> {
        // This only requires a string
        // TODO: valid this is sent correctly
        String setting = context.payload().metaData().getString("setting");
        GadgetNBT.toggleSetting(context.gadget(), setting);
    }),
    UNDO(context -> {
        if (context.gadget().getItem() instanceof BaseGadget actualGadget) {
            actualGadget.undo(context.player().level(), context.player(), context.gadget());
        }
    });

    private final Consumer<GadgetActionContext> handler;

    ActionGadget(Consumer<GadgetActionContext> handler) {
        this.handler = handler;
    }

    public Consumer<GadgetActionContext> getHandler() {
        return handler;
    }

    private static <T> void lazyCodecRead(Codec<T> codec, CompoundTag compound, Consumer<T> onSuccess, Consumer<String> onFailure) {
        // Read the nbt data from the context using our BiPos codec
        // Hehe, good luck dire :P
        DataResult<T> parseResult = codec.parse(NbtOps.INSTANCE, compound);
        Either<T, DataResult.PartialResult<T>> result = parseResult.get();

        // If the parse result is successful, set the copy start and end positions
        result.ifLeft(onSuccess);
        result.ifRight(partialResult -> onFailure.accept(partialResult.message()));
    }
}

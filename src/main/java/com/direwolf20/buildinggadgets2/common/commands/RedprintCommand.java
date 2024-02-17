package com.direwolf20.buildinggadgets2.common.commands;

import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
import com.google.common.collect.BiMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import static com.direwolf20.buildinggadgets2.util.MiscHelpers.playSound;

public class RedprintCommand {
    public RedprintCommand() {

    }

    public static void register(LiteralArgumentBuilder<CommandSourceStack> subCommand) {
        subCommand
                .requires(p_214470_ -> p_214470_.hasPermission(Commands.LEVEL_ALL))
                .then(
                        Commands.literal("list")
                                .requires(p_214470_ -> p_214470_.hasPermission(Commands.LEVEL_ALL))
                                .executes(
                                        p_258233_ -> listRedprints(
                                                p_258233_.getSource()
                                        )
                                )
                )
                .then(
                        Commands.literal("remove")
                                .requires(p_214470_ -> p_214470_.hasPermission(Commands.LEVEL_ADMINS))
                                .then(
                                        Commands.argument("name", StringArgumentType.word())
                                                .executes(
                                                        p_258232_ -> removeRedprint(
                                                                p_258232_.getSource(), StringArgumentType.getString(p_258232_, "name")
                                                        )
                                                )
                                )

                )
                .then(
                        Commands.literal("give")
                                .requires(p_214470_ -> p_214470_.hasPermission(Commands.LEVEL_ADMINS))
                                .then(
                                        Commands.argument("name", StringArgumentType.word())
                                                .then(
                                                        Commands.argument("targets", EntityArgument.player())
                                                                .executes(
                                                                        p_258232_ -> giveRedprint(
                                                                                p_258232_.getSource(), StringArgumentType.getString(p_258232_, "name"), EntityArgument.getPlayer(p_258232_, "targets")))
                                                )
                                )
                );
    }

    private static int listRedprints(CommandSourceStack pSource) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(pSource.getPlayer().level().getServer()).overworld());
        BiMap<UUID, String> redmaps = bg2Data.getRedprintLookup();
        for (BiMap.Entry<UUID, String> entry : redmaps.entrySet()) {
            pSource.sendSuccess(() -> Component.literal(entry.getValue()), false);
        }
        return redmaps.size();
    }

    private static int removeRedprint(CommandSourceStack pSource, String name) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(pSource.getPlayer().level().getServer()).overworld());
        if (bg2Data.removeFromRedprints(name)) {
            pSource.sendSuccess(() -> Component.translatable("buildinggadgets2.messages.redprintremovesuccess", name), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("buildinggadgets2.messages.redprintremovefail", name), false);
        }
        return 1;
    }

    private static int giveRedprint(CommandSourceStack pSource, String name, ServerPlayer serverPlayer) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(pSource.getPlayer().level().getServer()).overworld());
        UUID sourceUUID = bg2Data.getRedprintUUIDfromName(name);
        if (sourceUUID == null) {
            pSource.sendSuccess(() -> Component.translatable("buildinggadgets2.messages.redprintgivefail", name, serverPlayer.getDisplayName()), false);
            playSound(serverPlayer, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.WAXED_SIGN_INTERACT_FAIL.getLocation().toString()))));
            return 0;
        }

        ItemStack newRedprint = new ItemStack(Registration.Redprint.get());
        UUID targetUUID = GadgetNBT.getUUID(newRedprint);

        ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);
        GadgetNBT.setCopyUUID(newRedprint); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        bg2Data.addToCopyPaste(targetUUID, buildList);

        ArrayList<TagPos> teMap = bg2Data.peekTEMap(sourceUUID);
        ArrayList<TagPos> copiedMap = new ArrayList<>(Objects.requireNonNullElseGet(teMap, ArrayList::new)); //Put a blank TEMap there if we don't have one
        bg2Data.addToTEMap(targetUUID, copiedMap);

        //Ensure client has the updated values for both objects
        CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(targetUUID, false);
        serverPlayer.connection.send(new SendCopyDataPayload(targetUUID, GadgetNBT.getCopyUUID(newRedprint), tag));

        serverPlayer.addItem(newRedprint);
        playSound(serverPlayer, Holder.direct(SoundEvent.createVariableRangeEvent(new ResourceLocation(SoundEvents.ENCHANTMENT_TABLE_USE.getLocation().toString()))));

        return 1;
    }
}

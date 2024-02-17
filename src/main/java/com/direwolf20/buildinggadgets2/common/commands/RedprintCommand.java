package com.direwolf20.buildinggadgets2.common.commands;

import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.google.common.collect.BiMap;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.Objects;
import java.util.UUID;

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

    private static int giveRedprint(CommandSourceStack pSource, String name) {
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(pSource.getPlayer().level().getServer()).overworld());
        if (bg2Data.removeFromRedprints(name)) {
            pSource.sendSuccess(() -> Component.translatable("buildinggadgets2.messages.redprintremovesuccess", name), false);
        } else {
            pSource.sendSuccess(() -> Component.translatable("buildinggadgets2.messages.redprintremovefail", name), false);
        }
        return 1;
    }
}

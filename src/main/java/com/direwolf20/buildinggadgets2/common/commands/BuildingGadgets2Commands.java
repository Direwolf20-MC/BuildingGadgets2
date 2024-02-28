package com.direwolf20.buildinggadgets2.common.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;


import java.util.function.Consumer;

public class BuildingGadgets2Commands {
    public static void registerCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("buildinggadgets2");

        // sub commands
        registerCommand(builder, "redprints", RedprintCommand::register);

        // register final command
        event.getDispatcher().register(builder);
    }

    /** Registers a sub command for the root BG2 command */
    private static void registerCommand(LiteralArgumentBuilder<CommandSourceStack> root, String name, Consumer<LiteralArgumentBuilder<CommandSourceStack>> consumer) {
        LiteralArgumentBuilder<CommandSourceStack> subCommand = Commands.literal(name);
        consumer.accept(subCommand);
        root.then(subCommand);
    }
}

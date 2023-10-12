package com.github.NGoedix.videoplayer.commands;

import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class PlayVideoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated){
        dispatcher.register(CommandManager.literal("playvideo")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.players())
                .then(CommandManager.argument("volume", IntegerArgumentType.integer(0, 100))
                .then(CommandManager.argument("url", SymbolStringArgumentType.symbolString()) // Making url argument mandatory
                    .executes(e -> PlayVideoCommand.execute(e, false)) // This executes if blocked argument is not provided
                    .then(CommandManager.argument("control_blocked", BoolArgumentType.bool()) // Making blocked argument optional
                        .executes(e -> PlayVideoCommand.execute(e, true))))))); // This executes if blocked argument is provided
    }

    private static int execute(CommandContext<ServerCommandSource> command, boolean control){
        Collection<ServerPlayerEntity> players;

        try {
            players = EntityArgumentType.getPlayers(command, "target");
        } catch (CommandSyntaxException e) {
            command.getSource().sendError(Text.of("Error with target parameter."));
            return Command.SINGLE_SUCCESS;
        }

        for (ServerPlayerEntity player : players) {
            PacketHandler.sendS2CSendVideo(player,
                    StringArgumentType.getString(command, "url"),
                    IntegerArgumentType.getInteger(command, "volume"),
                    control && BoolArgumentType.getBool(command, "control_blocked"));
        }
        return Command.SINGLE_SUCCESS;
    }
}

package com.github.NGoedix.videoplayer.commands;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
                .requires((command)-> command.hasPermissionLevel(2))
                .then(CommandManager.argument("target", EntityArgumentType.players())
                .then(CommandManager.argument("volume", IntegerArgumentType.integer(0, 100))
                .then(CommandManager.argument("video", StringArgumentType.greedyString())
                    .executes(PlayVideoCommand::execute)))));
    }


    private static int execute(CommandContext<ServerCommandSource> command){
        Collection<ServerPlayerEntity> players;
        try {
            players = EntityArgumentType.getPlayers(command, "target");
        } catch (CommandSyntaxException e) {
            command.getSource().sendError(Text.of("Error with target parameter."));
            return Command.SINGLE_SUCCESS;
        }

        String video = StringArgumentType.getString(command,"video");

        if (video == null){
            command.getSource().sendError(Text.of("Error with file not exist"));
            return Command.SINGLE_SUCCESS;
        }

        int volume = IntegerArgumentType.getInteger(command, "volume");

        for (ServerPlayerEntity player : players) {
            Constants.LOGGER.info("Sending video to player: " + player.getName().asString());
            PacketHandler.sendMsgSendVideo(player, video, volume);
        }

        return Command.SINGLE_SUCCESS;
    }
}

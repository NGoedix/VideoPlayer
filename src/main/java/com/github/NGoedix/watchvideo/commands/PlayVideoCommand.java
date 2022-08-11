package com.github.NGoedix.watchvideo.commands;

import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.SendVideoMessage;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class PlayVideoCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("playvideo")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(PlayVideoCommand::execute)));
    }

    private static int execute(CommandContext<CommandSourceStack> command){
        PacketHandler.sendToAll(new SendVideoMessage(StringArgumentType.getString(command, "url")));
        return Command.SINGLE_SUCCESS;
    }
}

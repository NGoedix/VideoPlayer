package com.github.NGoedix.watchvideo.commands;

import com.github.NGoedix.watchvideo.client.gui.VideoScreen;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;

public class PlayVideoCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("playvideo").executes(PlayVideoCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> command){
        if(command.getSource().getEntity() instanceof Player player){
            player.sendMessage(new TextComponent("[Watchvideo] Playing video..."), Util.NIL_UUID);
            Minecraft.getInstance().setScreen(new VideoScreen());
        }
        return Command.SINGLE_SUCCESS;
    }
}

package com.lapaman.watchvideo.commands;

import com.lapaman.watchvideo.network.PacketHandler;
import com.lapaman.watchvideo.network.message.MessageShowGUI;
import com.lapaman.watchvideo.network.message.MessageStopGUI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class StopCommand extends CommandBase {
    @Override
    public String getName() {
        return "stopVideo";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Stop a video.";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (sender instanceof EntityPlayerMP) {
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(new TextComponentString("Faltan argumentos."));
            return;
        }

        PacketHandler.INSTANCE.sendToAll(new MessageStopGUI());
    }
}

package com.lapaman.watchvideo.commands;

import com.lapaman.watchvideo.ClientProxy;
import com.lapaman.watchvideo.network.PacketHandler;
import com.lapaman.watchvideo.network.message.MessageShowGUI;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class ShowCommand extends CommandBase {
    @Override
    public String getName() {
        return "showVideo";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Show a video pre-sent";
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

        PacketHandler.INSTANCE.sendToAll(new MessageShowGUI());
    }
}

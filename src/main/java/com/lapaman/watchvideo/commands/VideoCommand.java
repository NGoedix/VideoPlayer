package com.lapaman.watchvideo.commands;

import com.lapaman.watchvideo.WatchVideoMod;
import com.lapaman.watchvideo.network.PacketHandler;
import com.lapaman.watchvideo.network.message.MessageVideo;
import com.lapaman.watchvideo.util.FileUtil;
import com.lapaman.watchvideo.util.VideoTools;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.io.File;

public class VideoCommand extends CommandBase {

    @Override
    public String getName() {
        return "playvideo";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "Play a video from the server";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        // playvideo <all/username> <video>
        if (sender instanceof EntityPlayerMP) {
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(new TextComponentString("El formato del comando es /playvideo <all/username> <video>."));
            return;
        }

        File video = VideoTools.getVideo(args[1]);

        if (!video.exists()) {
            sender.sendMessage(new TextComponentString("The video does not exist."));
            return;
        }

        // Converting video
        WatchVideoMod.getWatchVideoMod().getLogger().info("Converting video to bytes.");
        byte[] videoBytes = FileUtil.toBytes(video);

        // Sending video
        WatchVideoMod.getWatchVideoMod().getLogger().info("Sending video to " + args[0]);

        if (args[0].equals("all")) {
            new Thread(() -> PacketHandler.INSTANCE.sendToAll(new MessageVideo(videoBytes))).start();
        } else {
            new Thread(() -> PacketHandler.INSTANCE.sendTo(new MessageVideo(videoBytes), (EntityPlayerMP) Minecraft.getMinecraft().world.getPlayerEntityByName(args[0]))).start();
        }
    }
}

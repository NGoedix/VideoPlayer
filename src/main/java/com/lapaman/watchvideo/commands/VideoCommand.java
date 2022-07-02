package com.lapaman.watchvideo.commands;

import com.lapaman.watchvideo.WatchVideoMod;
import com.lapaman.watchvideo.network.PacketHandler;
import com.lapaman.watchvideo.network.message.MessageVideo;
import com.lapaman.watchvideo.util.TextureCache;
import com.lapaman.watchvideo.util.VideoTools;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import org.jcodec.common.*;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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
            sender.sendMessage(new TextComponentString("El formato del comando es /playvideo <all/username> <video> <fullscreen/1280x720>."));
            return;
        }

        if (!VideoTools.getVideo(args[1]).exists()) {
            sender.sendMessage(new TextComponentString("The video does not exist."));
            return;
        }

        // Get data of video
        sender.sendMessage(new TextComponentString("Separating video into frames to send it to the clients..."));

        new Thread(() -> VideoTools.sendVideo(args[1])).start();
    }
}

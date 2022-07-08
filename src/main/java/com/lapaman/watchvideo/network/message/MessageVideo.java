package com.lapaman.watchvideo.network.message;

import com.lapaman.watchvideo.WatchVideoMod;
import com.lapaman.watchvideo.util.FileUtil;
import com.lapaman.watchvideo.util.VideoManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageVideo implements IMessage, IMessageHandler<MessageVideo, IMessage> {

    private byte[] video;

    public MessageVideo() {}

    public MessageVideo(byte[] video) {
        this.video = video;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        WatchVideoMod.getWatchVideoMod().getLogger().info("Reading bytes...");
        this.video = new byte[buf.readInt()];
        buf.readBytes(this.video);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        WatchVideoMod.getWatchVideoMod().getLogger().info("Writing bytes...");
        buf.writeInt(video.length);
        buf.writeBytes(video);
    }

    @Override
    public IMessage onMessage(MessageVideo message, MessageContext ctx) {
        WatchVideoMod.getWatchVideoMod().getLogger().info("Received message");
        Minecraft.getMinecraft().addScheduledTask(() -> VideoManager.setUp(FileUtil.fromBytes(message.video)));
        return null;
    }
}
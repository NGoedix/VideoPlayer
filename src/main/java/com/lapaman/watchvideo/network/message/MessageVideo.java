package com.lapaman.watchvideo.network.message;

import com.lapaman.watchvideo.ClientProxy;
import com.lapaman.watchvideo.util.ImageTools;
import com.lapaman.watchvideo.util.TextureCache;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.awt.image.BufferedImage;

public class MessageVideo implements IMessage, IMessageHandler<MessageVideo, IMessage> {

    private String filename;
    private BufferedImage frame;
    private double fps;

    public MessageVideo() {}

    public MessageVideo(String filename, BufferedImage frame, double fps) {
        this.filename = filename;
        this.frame = frame;
        this.fps = fps;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int lengthFilename = buf.readInt();
        this.filename = buf.readCharSequence(lengthFilename, StandardCharsets.UTF_8).toString();

        this.fps = buf.readDouble();
        
        byte[] framesBytes = new byte[buf.readInt()];
        buf.readBytes(framesBytes);
        try {
            this.frame = ImageTools.fromBytes(framesBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(filename.length());
        buf.writeCharSequence(filename, StandardCharsets.UTF_8);
        
        buf.writeDouble(fps);

        byte[] framesBytes = new byte[0];
        try {
            framesBytes = ImageTools.toBytes(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
        buf.writeInt(framesBytes.length);
        buf.writeBytes(framesBytes);
    }

    @Override
    public IMessage onMessage(MessageVideo message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(() -> TextureCache.instance().addVideo(message.filename, message.frame, message.fps));
        return null;
    }
}
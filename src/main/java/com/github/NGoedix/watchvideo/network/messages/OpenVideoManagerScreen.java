package com.github.NGoedix.watchvideo.network.messages;

import com.github.NGoedix.watchvideo.VideoPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;

public class OpenVideoManagerScreen implements IMessage {

    private BlockPos blockPos;
    private String url;
    private int tick;
    private int volume;
    private boolean loop;

    public OpenVideoManagerScreen() {}

    public OpenVideoManagerScreen(BlockPos blockPos, String url, int tick, int volume, boolean loop) {
        this.blockPos = blockPos;
        this.url = url;
        this.tick = tick;
        this.volume = volume;
        this.loop = loop;
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.blockPos = BlockPos.fromLong(buffer.readLong());

        int l = buffer.readInt();
        this.url = String.valueOf(buffer.readCharSequence(l, StandardCharsets.UTF_8));

        this.tick = buffer.readInt();
        this.volume = buffer.readInt();
        this.loop = buffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(blockPos.toLong());

        buffer.writeInt(url.length());
        buffer.writeCharSequence(url, StandardCharsets.UTF_8);

        buffer.writeInt(tick);
        buffer.writeInt(volume);
        buffer.writeBoolean(loop);
    }

    public static class Handler implements IMessageHandler<OpenVideoManagerScreen, IMessage> {

        @Override
        public IMessage onMessage(OpenVideoManagerScreen message, MessageContext ctx) {
            VideoPlayer.proxy.openVideoGUI(message.blockPos, message.url, message.tick, message.volume, message.loop);
            return null;
        }
    }
}

package com.github.NGoedix.watchvideo.network.messages;

import com.github.NGoedix.watchvideo.VideoPlayer;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class FrameVideoMessage implements IMessage {

    private BlockPos pos;
    private boolean playing;
    private int tick;

    public FrameVideoMessage() {}

    public FrameVideoMessage(BlockPos pos, boolean playing, int tick) {
        this.pos = pos;
        this.playing = playing;
        this.tick = tick;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(pos.toLong());
        buffer.writeBoolean(playing);
        buffer.writeInt(tick);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.pos = BlockPos.fromLong(buffer.readLong());
        this.playing = buffer.readBoolean();
        this.tick = buffer.readInt();
    }

    public static class Handler implements IMessageHandler<FrameVideoMessage, IMessage> {

        @Override
        public IMessage onMessage(FrameVideoMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(FrameVideoMessage message, MessageContext ctx)
        {
            VideoPlayer.proxy.manageVideo(message.pos, message.playing, message.tick);
        }
    }
}
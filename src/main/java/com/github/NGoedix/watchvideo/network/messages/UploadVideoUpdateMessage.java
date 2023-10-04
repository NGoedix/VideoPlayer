package com.github.NGoedix.watchvideo.network.messages;

import com.github.NGoedix.watchvideo.block.entity.TVBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class UploadVideoUpdateMessage implements IMessage {

    private BlockPos blockPos;
    private String url;
    private int volume;
    private boolean loop;
    private boolean isPlaying;
    private boolean reset;


    public UploadVideoUpdateMessage() {}

    public UploadVideoUpdateMessage(BlockPos blockPos, String url, int volume, boolean loop, boolean isPlaying, boolean reset) {
        this.blockPos = blockPos;
        this.url = url;
        this.volume = volume;
        this.loop = loop;
        this.isPlaying = isPlaying;
        this.reset = reset;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(blockPos.getX());
        buffer.writeInt(blockPos.getY());
        buffer.writeInt(blockPos.getZ());

        buffer.writeInt(url.length());
        buffer.writeCharSequence(url, StandardCharsets.UTF_8);

        buffer.writeInt(volume);
        buffer.writeBoolean(loop);
        buffer.writeBoolean(isPlaying);
        buffer.writeBoolean(reset);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.blockPos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());

        this.url = String.valueOf(buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8));

        this.volume = buffer.readInt();
        this.loop = buffer.readBoolean();
        this.isPlaying = buffer.readBoolean();
        this.reset = buffer.readBoolean();
    }

    public static class Handler implements IMessageHandler<UploadVideoUpdateMessage, IMessage> {

        @Override
        public IMessage onMessage(UploadVideoUpdateMessage message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(UploadVideoUpdateMessage message, MessageContext ctx)
        {
            EntityPlayerMP player = ctx.getServerHandler().player;
            if (player == null) return;
            if (player.world.getTileEntity(message.blockPos) instanceof TVBlockEntity) {
                TVBlockEntity tvBlockEntity = (TVBlockEntity) player.world.getTileEntity(message.blockPos);
                if (tvBlockEntity == null) return;

                tvBlockEntity.setBeingUsed(new UUID(0, 0));
                if (message.volume == -1) // NO UPDATE
                    return;

                tvBlockEntity.setUrl(message.url);
                tvBlockEntity.setVolume(message.volume);
                tvBlockEntity.setLoop(message.loop);
                tvBlockEntity.setPlaying(message.isPlaying);
                tvBlockEntity.notifyPlayer();

                if (message.reset)
                    tvBlockEntity.setTick(0);
            }
        }
    }
}

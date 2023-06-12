package com.github.NGoedix.watchvideo.network.message;

import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class UploadVideoUpdateMessage implements IMessage<UploadVideoUpdateMessage> {

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
    public void encode(UploadVideoUpdateMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.blockPos);
        buffer.writeUtf(message.url);
        buffer.writeInt(message.volume);
        buffer.writeBoolean(message.loop);
        buffer.writeBoolean(message.isPlaying);
        buffer.writeBoolean(message.reset);
    }

    @Override
    public UploadVideoUpdateMessage decode(FriendlyByteBuf buffer) {
        return new UploadVideoUpdateMessage(buffer.readBlockPos(), buffer.readUtf(), buffer.readInt(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
    }

    @Override
    public void handle(UploadVideoUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player.level().getBlockEntity(message.blockPos) instanceof TVBlockEntity tvBlockEntity) {
                tvBlockEntity.setBeingUsed(new UUID(0, 0));
                if (message.volume == -1) // NO UPDATE
                    return;

                tvBlockEntity.setUrl(message.url);
                VideoPlayer.LOGGER.info("Received url: " + message.url);
                tvBlockEntity.setVolume(message.volume);
                tvBlockEntity.setLoop(message.loop);
                tvBlockEntity.setPlaying(message.isPlaying);
                tvBlockEntity.notifyPlayer();

                if (message.reset)
                    tvBlockEntity.setTick(0);
            }
        });
    }
}

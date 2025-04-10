package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class StopPacket extends AbstractClientPacket<StopPacket> {

    private BlockPos blockPos;

    public StopPacket() {}

    public StopPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    protected void handleClientSide() {
        if (this.blockPos != null) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) Minecraft.getInstance().level.getBlockEntity(this.blockPos);
            if (videoPlayer != null) {
                videoPlayer.setPlaying(false);
                videoPlayer.setTick(0);
            }
        }
        ClientHandler.stop(this.blockPos);
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setTick(0);
            videoPlayer.setPlaying(false);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }
}

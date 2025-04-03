package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

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
    protected void handleServerSide(ServerPlayerEntity player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setTick(0);
            videoPlayer.setPlaying(false);
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
    }
}

package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class PausePacket extends AbstractClientPacket<PausePacket> {

    private BlockPos blockPos;
    private boolean pause;

    public PausePacket() {}

    public PausePacket(BlockPos blockPos, boolean pause) {
        this.blockPos = blockPos;
        this.pause = pause;
    }

    @Override
    protected void handleClientSide() {
        if (this.blockPos != null) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) Minecraft.getInstance().level.getBlockEntity(this.blockPos);
            if (videoPlayer != null) videoPlayer.setPlaying(!this.pause);
        }
        ClientHandler.setPauseMode(this.blockPos, this.pause);
    }

    @Override
    protected void handleServerSide(ServerPlayerEntity player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setPlaying(!this.pause);
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeBoolean(this.pause);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
        this.pause = buf.readBoolean();
    }
}

package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class VolumePacket extends AbstractClientPacket<VolumePacket> {

    private BlockPos blockPos;
    private int volume;

    public VolumePacket() {}

    public VolumePacket(BlockPos blockPos, int tick) {
        this.blockPos = blockPos;
        this.volume = tick;
    }

    @Override
    protected void handleClientSide() {
        if (this.blockPos != null) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) Minecraft.getInstance().level.getBlockEntity(this.blockPos);
            if (videoPlayer != null) videoPlayer.setVolume(this.volume);
        }
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setVolume(this.volume);
        }
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeInt(this.volume);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
        this.volume = buf.readInt();
    }
}

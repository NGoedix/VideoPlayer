package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class UrlPacket extends AbstractClientPacket<UrlPacket> {

    private BlockPos blockPos;
    private String uri;

    public UrlPacket() {}

    public UrlPacket(BlockPos blockPos, String uri) {
        this.blockPos = blockPos;
        this.uri = uri;
    }

    @Override
    protected void handleClientSide() {
        if (this.blockPos != null) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) Minecraft.getInstance().level.getBlockEntity(this.blockPos);
            videoPlayer.setUrl(this.uri);
            videoPlayer.setTick(0);
        }
    }

    @Override
    protected void handleServerSide(ServerPlayerEntity player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setUrl(this.uri);
            videoPlayer.setTick(0);
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeUtf(this.uri);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
        this.uri = buf.readUtf();
    }
}

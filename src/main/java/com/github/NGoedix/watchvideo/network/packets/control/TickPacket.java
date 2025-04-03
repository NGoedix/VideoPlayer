package com.github.NGoedix.watchvideo.network.packets.control;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class TickPacket extends AbstractClientPacket<TickPacket> {

    private BlockPos blockPos;
    private int tick;

    public TickPacket() {}

    public TickPacket(BlockPos blockPos, int tick) {
        this.blockPos = blockPos;
        this.tick = tick;
    }

    @Override
    protected void handleClientSide() {
        if (this.blockPos != null) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) Minecraft.getInstance().level.getBlockEntity(this.blockPos);
            if (videoPlayer != null) videoPlayer.setTick(this.tick);
        }
        ClientHandler.setTick(this.blockPos, this.tick);
    }

    @Override
    protected void handleServerSide(ServerPlayerEntity player) {
        if (player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setTick(this.tick);
        }
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeInt(this.tick);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
        this.tick = buf.readInt();
    }
}

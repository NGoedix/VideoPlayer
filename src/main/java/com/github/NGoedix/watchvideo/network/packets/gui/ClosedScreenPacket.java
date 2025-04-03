package com.github.NGoedix.watchvideo.network.packets.gui;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.network.packets.AbstractServerPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class ClosedScreenPacket extends AbstractServerPacket<ClosedScreenPacket> {

    private BlockPos blockPos;

    public ClosedScreenPacket() {}

    public ClosedScreenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
    }

    @Override
    public void read(PacketBuffer buf) {
        this.blockPos = buf.readBlockPos();
    }

    @Override
    protected void handleServerSide(ServerPlayerEntity player) {
        if (this.blockPos !=null && player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setBeingUsed(null);
        }
    }
}

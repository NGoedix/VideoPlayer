package com.github.NGoedix.watchvideo.network.packets.gui;

import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.network.packets.AbstractServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ClosedScreenPacket extends AbstractServerPacket<ClosedScreenPacket> {

    private BlockPos blockPos;

    public ClosedScreenPacket() {}

    public ClosedScreenPacket(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.blockPos = buf.readBlockPos();
    }

    @Override
    protected void handleServerSide(ServerPlayer player) {
        if (this.blockPos !=null && player.level.getBlockEntity(this.blockPos) instanceof VideoPlayerBlockEntity) {
            VideoPlayerBlockEntity videoPlayer = (VideoPlayerBlockEntity) player.level.getBlockEntity(this.blockPos);
            videoPlayer.setBeingUsed(null);
        }
    }
}

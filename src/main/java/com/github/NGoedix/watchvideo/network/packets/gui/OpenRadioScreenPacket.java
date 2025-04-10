package com.github.NGoedix.watchvideo.network.packets.gui;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class OpenRadioScreenPacket extends AbstractClientPacket<OpenRadioScreenPacket> {

    private BlockPos blockPos;
    private String url;
    private int volume;
    private boolean isPlaying;

    public OpenRadioScreenPacket() {}

    public OpenRadioScreenPacket(BlockPos blockPos, String url, int volume, boolean isPlaying) {
        this.blockPos = blockPos;
        this.url = url;
        this.volume = volume;
        this.isPlaying = isPlaying;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.blockPos);
        buffer.writeUtf(this.url);
        buffer.writeInt(this.volume);
        buffer.writeBoolean(this.isPlaying);
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        this.blockPos = buffer.readBlockPos();
        this.url = buffer.readUtf();
        this.volume = buffer.readInt();
        this.isPlaying = buffer.readBoolean();
    }

    @Override
    protected void handleClientSide() {
        ClientHandler.openRadioGUI(this.blockPos, this.url, this.volume, this.isPlaying);
    }
}

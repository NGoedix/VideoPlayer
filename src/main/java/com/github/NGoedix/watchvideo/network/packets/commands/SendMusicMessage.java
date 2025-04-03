package com.github.NGoedix.watchvideo.network.packets.commands;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.network.PacketBuffer;

public class SendMusicMessage extends AbstractClientPacket<SendMusicMessage> {

    private String url;
    private int volume;
    private MusicMessageType state;

    public SendMusicMessage() {
        this.state = MusicMessageType.STOP;
    }

    public SendMusicMessage(String url, int volume) {
        this.url = url;
        this.volume = volume;
        this.state = MusicMessageType.START;
    }

    @Override
    public void write(PacketBuffer buffer) {
        buffer.writeEnum(this.state);
        if (this.state == MusicMessageType.START) {
            buffer.writeUtf(this.url);
            buffer.writeInt(this.volume);
        }
    }

    @Override
    public void read(PacketBuffer buffer) {
        this.state = buffer.readEnum(MusicMessageType.class);
        if (this.state == MusicMessageType.START) {
            this.url = buffer.readUtf();
            this.volume = buffer.readInt();
        }
    }

    @Override
    protected void handleClientSide() {
        if (this.state == MusicMessageType.START) ClientHandler.playMusic(this.url, this.volume);
        if (this.state == MusicMessageType.STOP) ClientHandler.stopMusicIfPlaying();
    }

    enum MusicMessageType {
        START,
        STOP
    }
}

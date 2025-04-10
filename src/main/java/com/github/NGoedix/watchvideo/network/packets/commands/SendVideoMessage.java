package com.github.NGoedix.watchvideo.network.packets.commands;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.network.FriendlyByteBuf;

public class SendVideoMessage extends AbstractClientPacket<SendVideoMessage> {

    private String url;
    private int volume;
    private boolean isControlBlocked;
    private boolean canSkip;
    private VideoMessageType state;

    public SendVideoMessage() {
        this.state = VideoMessageType.STOP;
    }

    public SendVideoMessage(String url, int volume, boolean isControlBlocked, boolean canSkip) {
        this.url = url;
        this.volume = volume;
        this.isControlBlocked = isControlBlocked;
        this.canSkip = canSkip;
        this.state = VideoMessageType.START;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.state);
        if (this.state == VideoMessageType.START) {
            buffer.writeUtf(this.url);
            buffer.writeInt(this.volume);
            buffer.writeBoolean(this.isControlBlocked);
            buffer.writeBoolean(this.canSkip);
        }
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        this.state = buffer.readEnum(VideoMessageType.class);
        if (this.state == VideoMessageType.START) {
            this.url = buffer.readUtf();
            this.volume = buffer.readInt();
            this.isControlBlocked = buffer.readBoolean();
            this.canSkip = buffer.readBoolean();
        }
    }

    @Override
    protected void handleClientSide() {
        if (this.state == VideoMessageType.START)
            ClientHandler.openVideo(this.url, this.volume, this.isControlBlocked, this.canSkip);
        if (this.state == VideoMessageType.STOP) ClientHandler.stopVideoIfExists();
    }

    enum VideoMessageType {
        START,
        STOP
    }
}
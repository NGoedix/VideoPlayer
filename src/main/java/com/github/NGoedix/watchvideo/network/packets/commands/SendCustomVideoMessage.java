package com.github.NGoedix.watchvideo.network.packets.commands;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.network.packets.AbstractClientPacket;
import net.minecraft.network.FriendlyByteBuf;

public class SendCustomVideoMessage extends AbstractClientPacket<SendCustomVideoMessage> {

    private String url;
    private int volume;
    private boolean isControlBlocked;
    private boolean canSkip;
    private int mode;
    private int position;
    private int optionInMode;
    private int optionInSecs;
    private int optionOutMode;
    private int optionOutSecs;
    private VideoMessageType state;

    public SendCustomVideoMessage() {
        this.state = VideoMessageType.STOP;
    }

    public SendCustomVideoMessage(String url, int volume, boolean isControlBlocked, boolean canSkip, int mode, int position, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        this.url = url;
        this.volume = volume;
        this.isControlBlocked = isControlBlocked;
        this.canSkip = canSkip;
        this.mode = mode;
        this.position = position;
        this.optionInMode = optionInMode;
        this.optionInSecs = optionInSecs;
        this.optionOutMode = optionOutMode;
        this.optionOutSecs = optionOutSecs;
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
            buffer.writeInt(this.mode);
            buffer.writeInt(this.position);
            buffer.writeInt(this.optionInMode);
            buffer.writeInt(this.optionInSecs);
            buffer.writeInt(this.optionOutMode);
            buffer.writeInt(this.optionOutSecs);
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
            this.mode = buffer.readInt();
            this.position = buffer.readInt();
            this.optionInMode = buffer.readInt();
            this.optionInSecs = buffer.readInt();
            this.optionOutMode = buffer.readInt();
            this.optionOutSecs = buffer.readInt();
        }
    }

    @Override
    protected void handleClientSide() {
        if (this.state == VideoMessageType.START) {
            // Fullscreen = 0, Partial = 1
            if (mode == 0)
                ClientHandler.openVideo(this.url, this.volume, this.isControlBlocked, this.canSkip, this.optionInMode, this.optionInSecs, this.optionOutMode, this.optionOutSecs);
            // if (mode == 1) ClientHandler.openVideo(this.url, this.volume, this.posX, this.posY, this.width, this.height);
        }
        if (this.state == VideoMessageType.STOP) ClientHandler.stopVideoIfExists();
    }

    enum VideoMessageType {
        START,
        STOP
    }
}
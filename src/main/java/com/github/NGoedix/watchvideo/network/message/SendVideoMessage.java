package com.github.NGoedix.watchvideo.network.message;

import ca.weblite.objc.Client;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SendVideoMessage implements IMessage<SendVideoMessage> {

    private String url;
    private int volume;
    private boolean controlBlocked;

    public SendVideoMessage() {}

    public SendVideoMessage(String url, int volume, boolean controlBlocked) {
        this.url = url;
        this.volume = volume;
        this.controlBlocked = controlBlocked;
    }

    @Override
    public void encode(SendVideoMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.url.length());
        buffer.writeCharSequence(message.url, StandardCharsets.UTF_8);
        buffer.writeInt(message.volume);
        buffer.writeBoolean(message.controlBlocked);
    }

    @Override
    public SendVideoMessage decode(FriendlyByteBuf buffer) {
        int urlLength = buffer.readInt();
        String url = String.valueOf(buffer.readCharSequence(urlLength, StandardCharsets.UTF_8));
        int volume = buffer.readInt();
        boolean controlBlocked = buffer.readBoolean();
        return new SendVideoMessage(url, volume, controlBlocked);
    }

    @Override
    public void handle(SendVideoMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientHandler.openVideo(message.url, message.volume, message.controlBlocked));
        supplier.get().setPacketHandled(true);
    }
}

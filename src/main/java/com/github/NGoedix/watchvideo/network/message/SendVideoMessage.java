package com.github.NGoedix.watchvideo.network.message;

import com.github.NGoedix.watchvideo.client.ClientHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public class SendVideoMessage implements IMessage<SendVideoMessage> {

    private String url;
    private int volume;

    public SendVideoMessage() {}

    public SendVideoMessage(String url, int volume) {
        this.url = url;
        this.volume = volume;
    }

    @Override
    public void encode(SendVideoMessage message, PacketBuffer buffer) {
        buffer.writeInt(message.url.length());
        buffer.writeCharSequence(message.url, StandardCharsets.UTF_8);
        buffer.writeInt(message.volume);
    }

    @Override
    public SendVideoMessage decode(PacketBuffer buffer) {
        int l = buffer.readInt();
        String url = String.valueOf(buffer.readCharSequence(l, StandardCharsets.UTF_8));
        int volume = buffer.readInt();
        return new SendVideoMessage(url, volume);
    }

    @Override
    public void handle(SendVideoMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientHandler.openVideo(message.url, message.volume));
        supplier.get().setPacketHandled(true);
    }
}

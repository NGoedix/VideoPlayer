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

    public SendVideoMessage() {}

    public SendVideoMessage(String url) {
        this.url = url;
    }

    @Override
    public void encode(SendVideoMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.url.length());
        buffer.writeCharSequence(message.url, StandardCharsets.UTF_8);
    }

    @Override
    public SendVideoMessage decode(FriendlyByteBuf buffer) {
        int l = buffer.readInt();
        String url = String.valueOf(buffer.readCharSequence(l, StandardCharsets.UTF_8));
        return new SendVideoMessage(url);
    }

    @Override
    public void handle(SendVideoMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() -> ClientHandler.openVideo(message.url));
        supplier.get().setPacketHandled(true);
    }
}

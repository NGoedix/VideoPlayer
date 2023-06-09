package com.github.NGoedix.videoplayer.network.packets;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class SendVideoMessage {

    public static final Identifier ID = new Identifier(Constants.MOD_ID, "send_video");

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Constants.LOGGER.info("Received video player message");
        String url = buf.readString();
        int volume = buf.readInt();

        ClientHandler.openVideo(client, url, volume);
    }
}

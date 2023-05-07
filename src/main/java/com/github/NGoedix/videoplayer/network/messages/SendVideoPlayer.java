package com.github.NGoedix.videoplayer.network.messages;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class SendVideoPlayer {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Constants.LOGGER.info("Received video player message");
        String url = buf.readString();

        ClientHandler.openVideo(client, url);
    }
}

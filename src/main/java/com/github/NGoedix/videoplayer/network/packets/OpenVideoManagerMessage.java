package com.github.NGoedix.videoplayer.network.packets;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class OpenVideoManagerMessage {

    public static final Identifier ID = new Identifier(Reference.MOD_ID, "open_video_manager");

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Reference.LOGGER.info("Received open video message");

        BlockPos pos = buf.readBlockPos();
        String url = buf.readString();
        int tick = buf.readInt();
        int volume = buf.readInt();
        boolean loop = buf.readBoolean();

        ClientHandler.openVideoGUI(client, pos, url, tick, volume, loop);
    }
}
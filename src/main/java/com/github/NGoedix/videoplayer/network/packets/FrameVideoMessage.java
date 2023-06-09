package com.github.NGoedix.videoplayer.network.packets;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FrameVideoMessage {

    public static final Identifier ID = new Identifier(Constants.MOD_ID, "frame_video");

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        Constants.LOGGER.info("Received frame message");

        BlockPos pos = buf.readBlockPos();
        boolean playing = buf.readBoolean();
        int tick = buf.readInt();

        ClientHandler.manageVideo(client, pos, playing, tick);
    }
}

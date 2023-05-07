package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.network.messages.SendVideoPlayer;
import com.github.NGoedix.videoplayer.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketHandler {

    public static final Identifier NET_ID = new Identifier(Constants.MOD_ID, "networking");

    public static void registerClient(){
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, SendVideoPlayer::receive);
    }

    public static void sendTo(ServerPlayerEntity player, String url){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(url);
        ServerPlayNetworking.send(player, NET_ID, buf);
    }
}

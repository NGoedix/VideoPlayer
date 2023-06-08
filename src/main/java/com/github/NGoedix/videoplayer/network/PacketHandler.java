package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.Constants;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class PacketHandler {

    public static final Identifier NET_ID = new Identifier(Constants.MOD_ID, "networking");

    public static void registerClient(){
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, PacketManager::receiveSendVideo);
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, PacketManager::receiveFrameVideo);
    }

    public static void sendMsgSendVideo(ServerPlayerEntity player, String url, int volume){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(url);
        buf.writeInt(volume);
        ServerPlayNetworking.send(player, NET_ID, buf);
    }

    public static void sendMsgFrameVideo(WorldChunk chunk, BlockPos pos, boolean playing, int tick) {
        ServerWorld world = (ServerWorld) chunk.getWorld();
        PacketByteBuf packet = PacketByteBufs.create();

        packet.writeBlockPos(pos);
        packet.writeBoolean(playing);
        packet.writeInt(tick);

        for (ServerPlayerEntity player : PlayerLookup.tracking(world, chunk.getPos()))
            ServerPlayNetworking.send(player, NET_ID, packet);
    }
}

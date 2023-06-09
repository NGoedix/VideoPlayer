package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.network.packet.FrameVideoMessage;
import com.github.NGoedix.videoplayer.network.packet.OpenVideoManagerMessage;
import com.github.NGoedix.videoplayer.network.packet.SendVideoMessage;
import com.github.NGoedix.videoplayer.network.packet.UpdateVideoMessage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class PacketHandler {

    public static void registerC2SPackets(){
        ServerPlayNetworking.registerGlobalReceiver(UpdateVideoMessage.ID, UpdateVideoMessage::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SendVideoMessage.ID, SendVideoMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(FrameVideoMessage.ID, FrameVideoMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(OpenVideoManagerMessage.ID, OpenVideoManagerMessage::receive);
    }

    // SEND MESSAGES S2C
    public static void sendS2CSendVideo(ServerPlayerEntity player, String url, int volume){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(url);
        buf.writeInt(volume);
        ServerPlayNetworking.send(player, SendVideoMessage.ID, buf);
    }

    public static void sendS2CFrameVideo(WorldChunk chunk, BlockPos pos, boolean playing, int tick) {
        ServerWorld world = (ServerWorld) chunk.getWorld();
        PacketByteBuf packet = PacketByteBufs.create();

        packet.writeBlockPos(pos);
        packet.writeBoolean(playing);
        packet.writeInt(tick);

        for (ServerPlayerEntity player : PlayerLookup.tracking(world, chunk.getPos()))
            ServerPlayNetworking.send(player, FrameVideoMessage.ID, packet);
    }

    public static void sendS2COpenVideoManager(ServerPlayerEntity player, BlockPos blockPos, String url, int tick, int volume, boolean loop) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeString(url);
        buf.writeInt(tick);
        buf.writeInt(volume);
        buf.writeBoolean(loop);
        ServerPlayNetworking.send(player, OpenVideoManagerMessage.ID, buf);
    }

    // SEND MESSAGES C2S
    public static void sendC2SUpdateVideo(BlockPos blockPos, String url, int volume, boolean loop, boolean isPlaying, boolean reset) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeString(url);
        buf.writeInt(volume);
        buf.writeBoolean(loop);
        buf.writeBoolean(isPlaying);
        buf.writeBoolean(reset);

        ClientPlayNetworking.send(UpdateVideoMessage.ID, buf);
    }
}

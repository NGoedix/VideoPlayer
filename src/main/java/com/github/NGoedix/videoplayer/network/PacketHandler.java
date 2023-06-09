package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.network.packets.FrameVideoMessage;
import com.github.NGoedix.videoplayer.network.packets.OpenVideoManagerMessage;
import com.github.NGoedix.videoplayer.network.packets.SendVideoMessage;
import com.github.NGoedix.videoplayer.network.packets.UpdateVideoMessage;
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

    public static void registerC2SPackets(){
        ServerPlayNetworking.registerGlobalReceiver(NET_ID, UpdateVideoMessage::receive);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, SendVideoMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, FrameVideoMessage::receive);
        ClientPlayNetworking.registerGlobalReceiver(NET_ID, OpenVideoManagerMessage::receive);
    }

    // SEND MESSAGES

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

    public static void sendMsgOpenVideoManager(ServerPlayerEntity player, BlockPos blockPos, String url, int tick, int volume, boolean loop) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeString(url);
        buf.writeInt(tick);
        buf.writeInt(volume);
        buf.writeBoolean(loop);
        ServerPlayNetworking.send(player, NET_ID, buf);
    }

    public static void sendMsgUpdateVideo(ServerPlayerEntity player, BlockPos blockPos, String url, int volume, boolean loop, boolean isPlaying, boolean reset) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(blockPos);
        buf.writeString(url);
        buf.writeInt(volume);
        buf.writeBoolean(loop);
        buf.writeBoolean(isPlaying);
        buf.writeBoolean(reset);

        ServerPlayNetworking.send(player, NET_ID, buf);
    }
}

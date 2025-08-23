package com.github.NGoedix.videoplayer.network;

import com.github.NGoedix.videoplayer.network.packet.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;

public class PacketHandler {
    public static void registerPackets() {
        PayloadTypeRegistry.playC2S().register(VideoUpdateMessage.TYPE, VideoUpdateMessage.CODEC);
        PayloadTypeRegistry.playC2S().register(RadioUpdateMessage.TYPE, RadioUpdateMessage.CODEC);

        PayloadTypeRegistry.playS2C().register(FrameVideoMessage.TYPE, FrameVideoMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenVideoManagerScreenMessage.TYPE, OpenVideoManagerScreenMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(OpenRadioManagerScreenMessage.TYPE, OpenRadioManagerScreenMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StartVideoMessage.TYPE, StartVideoMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StopVideoMessage.TYPE, StopVideoMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StartCustomVideoMessage.TYPE, StartCustomVideoMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StopCustomVideoMessage.TYPE, StopCustomVideoMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StartMusicMessage.TYPE, StartMusicMessage.CODEC);
        PayloadTypeRegistry.playS2C().register(StopMusicMessage.TYPE, StopMusicMessage.CODEC);
    }

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(VideoUpdateMessage.TYPE, VideoUpdateMessage::handle);
        ServerPlayNetworking.registerGlobalReceiver(RadioUpdateMessage.TYPE, RadioUpdateMessage::handle);
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(FrameVideoMessage.TYPE, FrameVideoMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(OpenVideoManagerScreenMessage.TYPE, OpenVideoManagerScreenMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(OpenRadioManagerScreenMessage.TYPE, OpenRadioManagerScreenMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StartVideoMessage.TYPE, StartVideoMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StartCustomVideoMessage.TYPE, StartCustomVideoMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StartMusicMessage.TYPE, StartMusicMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StopVideoMessage.TYPE, StopVideoMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StopCustomVideoMessage.TYPE, StopCustomVideoMessage::handle);
        ClientPlayNetworking.registerGlobalReceiver(StopMusicMessage.TYPE, StopMusicMessage::handle);
    }

    // SEND MESSAGES S2C
    public static void sendS2CSendVideoStart(ServerPlayer player, String url, int volume, boolean controlBlocked, boolean canSkip) {
        ServerPlayNetworking.send(player, new StartVideoMessage(url, volume, controlBlocked, canSkip));
    }

    public static void sendS2CSendVideoStop(ServerPlayer player) {
        ServerPlayNetworking.send(player, StopVideoMessage.INSTANCE);
    }

    public static void sendS2CSendMusicStart(ServerPlayer player, String url, int volume) {
        ServerPlayNetworking.send(player, new StartMusicMessage(url, volume));
    }

    public static void sendS2CSendMusicStop(ServerPlayer player) {
        ServerPlayNetworking.send(player, StopMusicMessage.INSTANCE);
    }

    public static void sendS2CSendVideoStart(ServerPlayer player, String url, int volume, boolean controlBlocked, boolean canSkip, int mode, int position, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        ServerPlayNetworking.send(player, new StartCustomVideoMessage(
            url, volume, controlBlocked, canSkip,
            mode, position, optionInMode, optionInSecs,
            optionOutMode, optionOutSecs
        ));
    }

    public static void sendS2COpenVideoManagerScreen(ServerPlayer player, BlockPos pos, String url, int volume, int tick, boolean isPlaying) {
        ServerPlayNetworking.send(player, new OpenVideoManagerScreenMessage(pos, url, volume, tick, isPlaying));
    }

    public static void sendS2COpenRadioManagerScreen(ServerPlayer player, BlockPos pos, String url, int volume, boolean isPlaying) {
        ServerPlayNetworking.send(player, new OpenRadioManagerScreenMessage(pos, url, volume, isPlaying));
    }

    public static void sendS2CFrameVideoMessage(LevelChunk chunk, String url, BlockPos pos, boolean playing, int tick) {
        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()))
            ServerPlayNetworking.send(player, new FrameVideoMessage(url, pos, playing, tick));
    }

    public static void sendS2CRadioMessage(LevelChunk chunk, String url, BlockPos pos, boolean playing) {
        for (ServerPlayer player : PlayerLookup.tracking((ServerLevel) chunk.getLevel(), chunk.getPos()))
            ServerPlayNetworking.send(player, new RadioMessage(url, pos, playing));
    }

    public static void sendC2SVideoUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean stopped, boolean exit) {
        ClientPlayNetworking.send(new VideoUpdateMessage(pos, url, volume, tick, isPlaying, stopped, exit));
    }

    public static void sendC2SRadioUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean exit) {
        ClientPlayNetworking.send(new RadioUpdateMessage(pos, url, volume, tick, isPlaying, exit));
    }
}

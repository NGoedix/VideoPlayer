package com.github.NGoedix.watchvideo.network;

import com.github.NGoedix.watchvideo.network.message.*;
import com.github.NGoedix.watchvideo.Reference;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {

    public static final String PROTOCOL_VERSION = "2";

    private static SimpleChannel INSTANCE;

    private static int nextId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Reference.MOD_ID, "network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();
        register(SendVideoMessage.class, new SendVideoMessage());
        register(FrameVideoMessage.class, new FrameVideoMessage());
        register(OpenVideoManagerScreen.class, new OpenVideoManagerScreen());
        register(UploadVideoUpdateMessage.class, new UploadVideoUpdateMessage());
    }

    private static <T> void register(Class<T> clazz, IMessage<T> message) {
        INSTANCE.registerMessage(nextId++, clazz, message::encode, message::decode, message::handle);
    }

    public static SimpleChannel getPlayChannel()
    {
        return INSTANCE;
    }

    public static <MSG> void sendTo(MSG msg, Player player) {
        INSTANCE.sendTo(msg, ((ServerPlayer)player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToClient(MSG message, Level level, BlockPos pos) {
        sendToClient(message, level.getChunkAt(pos));
    }

    public static <MSG> void sendToClient(MSG msg, LevelChunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

    public static <MSG> void sendToAllTracking(MSG msg, LivingEntity entityToTrack) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entityToTrack), msg);
    }

    public static <MSG> void sendToAll(MSG msg) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }

}

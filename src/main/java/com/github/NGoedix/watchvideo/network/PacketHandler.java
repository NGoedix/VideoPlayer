package com.github.NGoedix.watchvideo.network;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.network.packets.*;
import com.github.NGoedix.watchvideo.network.packets.commands.SendCustomVideoMessage;
import com.github.NGoedix.watchvideo.network.packets.commands.SendMusicMessage;
import com.github.NGoedix.watchvideo.network.packets.commands.SendVideoMessage;
import com.github.NGoedix.watchvideo.network.packets.control.*;
import com.github.NGoedix.watchvideo.network.packets.gui.ClosedScreenPacket;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenTVScreenPacket;
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

import java.util.function.Supplier;

public class PacketHandler {

    public static final String PROTOCOL_VERSION = "3";

    private static SimpleChannel INSTANCE;

    private static int nextId = 0;

    public static void init() {
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(Reference.MOD_ID, "network"))
                .networkProtocolVersion(() -> PROTOCOL_VERSION)
                .clientAcceptedVersions(PROTOCOL_VERSION::equals)
                .serverAcceptedVersions(PROTOCOL_VERSION::equals)
                .simpleChannel();

        // COMMANDS
        register(SendCustomVideoMessage.class, SendCustomVideoMessage::new);
        register(SendVideoMessage.class, SendVideoMessage::new);
        register(SendMusicMessage.class, SendMusicMessage::new);

        // CONTROL
        register(PausePacket.class, PausePacket::new);
        register(StopPacket.class, StopPacket::new);
        register(VolumePacket.class, VolumePacket::new);
        register(UrlPacket.class, UrlPacket::new);
        register(TickPacket.class, TickPacket::new);

        // GUI
        register(ClosedScreenPacket.class, ClosedScreenPacket::new);
        register(OpenRadioScreenPacket.class, OpenRadioScreenPacket::new);
        register(OpenTVScreenPacket.class, OpenTVScreenPacket::new);
    }

    private static <T extends AbstractPacket<T>> void register(Class<T> clazz, Supplier<T> factory) {
        INSTANCE.registerMessage(
                nextId++,
                clazz,
                // encode
                AbstractPacket::write,
                // decode
                buf -> {
                    T msg = factory.get();
                    msg.read(buf);
                    return msg;
                },
                // handle
                (msg, ctx) -> {
                    msg.handlePacket(ctx.get());
                    ctx.get().setPacketHandled(true);
                }
        );
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

package com.github.NGoedix.watchvideo.network;

import com.github.NGoedix.watchvideo.network.packets.*;
import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.network.packets.control.*;
import com.github.NGoedix.watchvideo.network.packets.commands.SendCustomVideoMessage;
import com.github.NGoedix.watchvideo.network.packets.commands.SendMusicMessage;
import com.github.NGoedix.watchvideo.network.packets.commands.SendVideoMessage;
import com.github.NGoedix.watchvideo.network.packets.gui.ClosedScreenPacket;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenRadioScreenPacket;
import com.github.NGoedix.watchvideo.network.packets.gui.OpenTVScreenPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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

    public static <MSG> void sendTo(MSG msg, PlayerEntity player) {
        INSTANCE.sendTo(msg, ((ServerPlayerEntity)player).connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public static <MSG> void sendToClient(MSG message, World level, BlockPos pos) {
        sendToClient(message, level.getChunkAt(pos));
    }

    public static <MSG> void sendToClient(MSG msg, Chunk chunk) {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), msg);
    }

    public static <MSG> void sendToServer(MSG msg) {
        INSTANCE.sendToServer(msg);
    }
}

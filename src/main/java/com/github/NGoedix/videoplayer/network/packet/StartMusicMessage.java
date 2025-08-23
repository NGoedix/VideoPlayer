package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartMusicMessage(
    String url, int volume
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "start_music");
    public static final CustomPacketPayload.Type<StartMusicMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, StartMusicMessage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, StartMusicMessage::url,
        ByteBufCodecs.VAR_INT, StartMusicMessage::volume,
        StartMusicMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(StartMusicMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.playMusic(ctx.client(), message.url(), message.volume());
    }
}

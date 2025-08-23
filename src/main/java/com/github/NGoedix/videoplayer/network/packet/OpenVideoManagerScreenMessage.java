package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenVideoManagerScreenMessage(
    BlockPos pos, String url,
    int volume, int tick, boolean isPlaying
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenVideoManagerScreenMessage> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OpenVideoManagerScreenMessage::pos,
        ByteBufCodecs.STRING_UTF8, OpenVideoManagerScreenMessage::url,
        ByteBufCodecs.VAR_INT, OpenVideoManagerScreenMessage::volume,
        ByteBufCodecs.VAR_INT, OpenVideoManagerScreenMessage::tick,
        ByteBufCodecs.BOOL, OpenVideoManagerScreenMessage::isPlaying,
        OpenVideoManagerScreenMessage::new
    );

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "open_video_manager");
    public static final CustomPacketPayload.Type<OpenVideoManagerScreenMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(OpenVideoManagerScreenMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.openVideoGUI(ctx.client(), message.pos(), message.url(), message.volume(), message.tick(), message.isPlaying());
    }
}

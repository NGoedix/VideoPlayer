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

public record OpenRadioManagerScreenMessage(
    BlockPos pos, String url,
    int volume, boolean isPlaying
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenRadioManagerScreenMessage> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, OpenRadioManagerScreenMessage::pos,
        ByteBufCodecs.STRING_UTF8, OpenRadioManagerScreenMessage::url,
        ByteBufCodecs.VAR_INT, OpenRadioManagerScreenMessage::volume,
        ByteBufCodecs.BOOL, OpenRadioManagerScreenMessage::isPlaying,
        OpenRadioManagerScreenMessage::new
    );

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "open_radio_manager");
    public static final CustomPacketPayload.Type<OpenRadioManagerScreenMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(OpenRadioManagerScreenMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.openRadioGUI(ctx.client(), message.pos(), message.url(), message.volume(), message.isPlaying());
    }
}

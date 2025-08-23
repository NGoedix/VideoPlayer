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

public record FrameVideoMessage(
    String url, BlockPos pos, boolean playing, int tick
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, FrameVideoMessage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, FrameVideoMessage::url,
        BlockPos.STREAM_CODEC, FrameVideoMessage::pos,
        ByteBufCodecs.BOOL, FrameVideoMessage::playing,
        ByteBufCodecs.VAR_INT, FrameVideoMessage::tick,
        FrameVideoMessage::new
    );

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "frame_video");
    public static final CustomPacketPayload.Type<FrameVideoMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(FrameVideoMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.manageVideo(ctx.client(), message.url(), message.pos(), message.playing(), message.tick());
    }
}

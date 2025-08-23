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

public record RadioMessage(
    String url, BlockPos pos, boolean playing
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, RadioMessage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, RadioMessage::url,
        BlockPos.STREAM_CODEC, RadioMessage::pos,
        ByteBufCodecs.BOOL, RadioMessage::playing,
        RadioMessage::new
    );

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "radio");
    public static final CustomPacketPayload.Type<RadioMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(RadioMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.manageRadio(ctx.client(), message.url(), message.pos(), message.playing());
    }
}

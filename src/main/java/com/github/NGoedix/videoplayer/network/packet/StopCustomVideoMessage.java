package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class StopCustomVideoMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "stop_custom_video");
    public static final CustomPacketPayload.Type<StopCustomVideoMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StopCustomVideoMessage INSTANCE = new StopCustomVideoMessage();
    public static final StreamCodec<RegistryFriendlyByteBuf, StopCustomVideoMessage> CODEC = StreamCodec.unit(INSTANCE);

    private StopCustomVideoMessage() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(StopCustomVideoMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.stopVideoIfExists(ctx.client());
    }
}

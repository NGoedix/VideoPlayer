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

public class StopVideoMessage implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "stop_video");
    public static final CustomPacketPayload.Type<StopVideoMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StopVideoMessage INSTANCE = new StopVideoMessage();
    public static final StreamCodec<RegistryFriendlyByteBuf, StopVideoMessage> CODEC = StreamCodec.unit(INSTANCE);

    private StopVideoMessage() {}

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(StopVideoMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.stopVideoIfExists(ctx.client());
    }
}

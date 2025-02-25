package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import com.github.NGoedix.videoplayer.util.ByteUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record StartVideoMessage(
    String url, int volume,
    byte flags
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, StartVideoMessage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, StartVideoMessage::url,
        ByteBufCodecs.VAR_INT, StartVideoMessage::volume,
        ByteBufCodecs.BYTE, StartVideoMessage::flags,
        StartVideoMessage::new
    );
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "start_video");
    public static final CustomPacketPayload.Type<StartVideoMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    public StartVideoMessage(String url, int volume, boolean isControlBlocked, boolean canSkip) {
        this(url, volume, (byte) (ByteUtil.setBitAndShift(isControlBlocked, 1) | ByteUtil.setBitAndShift(canSkip, 0)));
    }

    public boolean isControlBlocked() {
        return ((this.flags() >> 1) & 1) != 0;
    }

    public boolean canSkip() {
        return (this.flags() & 1) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(StartVideoMessage message, ClientPlayNetworking.Context ctx) {
        ClientHandler.openVideo(ctx.client(), message.url(), message.volume(), message.isControlBlocked(), message.canSkip());
    }
}

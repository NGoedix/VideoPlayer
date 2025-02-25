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

public record StartCustomVideoMessage(
    String url, int volume, byte flags,
    long modeAndPosition, long optionsIn, long optionsOut
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "start_custom_video");
    public static final CustomPacketPayload.Type<StartCustomVideoMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, StartCustomVideoMessage> CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, StartCustomVideoMessage::url,
        ByteBufCodecs.VAR_INT, StartCustomVideoMessage::volume,
        ByteBufCodecs.BYTE, StartCustomVideoMessage::flags,
        ByteBufCodecs.VAR_LONG, StartCustomVideoMessage::modeAndPosition,
        ByteBufCodecs.VAR_LONG, StartCustomVideoMessage::optionsIn,
        ByteBufCodecs.VAR_LONG, StartCustomVideoMessage::optionsOut,
        StartCustomVideoMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public static void handle(StartCustomVideoMessage message, ClientPlayNetworking.Context ctx) {
        if (message.mode() == 0)
            ClientHandler.openVideo(ctx.client(), message.url(), message.volume(), message.isControlBlocked(), message.canSkip(), message.optionInMode(), message.optionInSecs(), message.optionOutMode(), message.optionOutSecs());
    }

    public StartCustomVideoMessage(String url, int volume, boolean isControlBlocked, boolean canSkip, int mode, int position, int optionsInMode, int optionsInSecs, int optionsOutMode, int optionsOutSecs) {
        this(url, volume, (byte) (ByteUtil.setBitAndShift(isControlBlocked, 1) | ByteUtil.setBitAndShift(canSkip, 0)),
            (((long) mode << 31) | position),
            (((long) optionsInMode << 31) | optionsInSecs),
            (((long) optionsOutMode << 31) | optionsOutSecs)
        );
    }

    public boolean isControlBlocked() {
        return ((this.flags >> 1) & 1) != 0;
    }

    public boolean canSkip() {
        return (this.flags & 1) != 0;
    }

    public int mode() {
        return (int) ((this.modeAndPosition >> 31) & Integer.MAX_VALUE);
    }

    public int position() {
        return (int) (this.modeAndPosition & Integer.MAX_VALUE);
    }

    public int optionInMode() {
        return (int) ((this.optionsIn >> 31) & Integer.MAX_VALUE);
    }

    public int optionInSecs() {
        return (int) ((this.optionsIn) & Integer.MAX_VALUE);
    }

    public int optionOutMode() {
        return (int) ((this.optionsOut >> 31) & Integer.MAX_VALUE);
    }

    public int optionOutSecs() {
        return (int) ((this.optionsOut) & Integer.MAX_VALUE);
    }
}

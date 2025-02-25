package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.util.ByteUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record VideoUpdateMessage(
    BlockPos pos, String url, int volume, int tick,
    byte flags
) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "update_video");
    public static final CustomPacketPayload.Type<VideoUpdateMessage> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, VideoUpdateMessage> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, VideoUpdateMessage::pos,
        ByteBufCodecs.STRING_UTF8, VideoUpdateMessage::url,
        ByteBufCodecs.VAR_INT, VideoUpdateMessage::volume,
        ByteBufCodecs.VAR_INT, VideoUpdateMessage::tick,
        ByteBufCodecs.BYTE, VideoUpdateMessage::flags,
        VideoUpdateMessage::new
    );

    public VideoUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean isStopped, boolean shouldExit) {
        this(pos, url, volume, tick,
            (byte) (ByteUtil.setBitAndShift(isPlaying, 2) | ByteUtil.setBitAndShift(isStopped, 1) | ByteUtil.setBitAndShift(shouldExit, 0))
        );
    }

    public boolean isPlaying() {
        return ((flags >> 2) & 1) != 0;
    }

    public boolean isStopped() {
        return ((flags >> 1) & 1) != 0;
    }

    public boolean shouldExit() {
        return (flags & 1) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(VideoUpdateMessage message, ServerPlayNetworking.Context ctx) {
        var server = ctx.server();
        var player = ctx.player();

        var pos = message.pos();
        var url = message.url();
        var volume = message.volume();
        var tick = message.tick();
        var isPlaying = message.isPlaying();
        var stopped = message.isStopped();
        var exit = message.shouldExit();

        server.execute(() -> {
            if (player.level().getBlockEntity(pos) instanceof TVBlockEntity) {
                TVBlockEntity tvBlockEntity = (TVBlockEntity) player.level().getBlockEntity(pos);
                if (tvBlockEntity == null) return;

                if (exit)
                    tvBlockEntity.setBeingUsed(new UUID(0, 0));
                else {
                    tvBlockEntity.setUrl(url);
                    tvBlockEntity.setVolume(volume);

                    if (tick != -1)
                        tvBlockEntity.setTick(tick);

                    tvBlockEntity.setPlaying(isPlaying);

                    if (stopped)
                        tvBlockEntity.stop();

                    tvBlockEntity.notifyPlayer();
                }}
        });
    }
}

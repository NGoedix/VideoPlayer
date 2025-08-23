package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.videoplayer.util.ByteUtil;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RadioUpdateMessage(
    BlockPos pos, String url,
    int volume, int tick,
    byte flags
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, RadioUpdateMessage> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, RadioUpdateMessage::pos,
        ByteBufCodecs.STRING_UTF8, RadioUpdateMessage::url,
        ByteBufCodecs.VAR_INT, RadioUpdateMessage::volume,
        ByteBufCodecs.VAR_INT, RadioUpdateMessage::tick,
        ByteBufCodecs.BYTE, RadioUpdateMessage::flags,
        RadioUpdateMessage::new
    );
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "update_radio");
    public static final CustomPacketPayload.Type<RadioUpdateMessage> TYPE = new CustomPacketPayload.Type<>(ID);

    public RadioUpdateMessage(BlockPos pos, String url, int volume, int tick, boolean isPlaying, boolean shouldExit) {
        this(pos, url, volume, tick, (byte) (ByteUtil.setBitAndShift(isPlaying, 1) | ByteUtil.setBitAndShift(shouldExit, 0)));
    }

    public boolean isPlaying() {
        return ((flags >> 1) & 1) != 0;
    }

    public boolean shouldExit() {
        return (flags & 1) != 0;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RadioUpdateMessage message, ServerPlayNetworking.Context ctx) {
        ctx.server().execute(() -> {
            if (ctx.player().level().getBlockEntity(message.pos()) instanceof RadioBlockEntity radioBlockEntity) {
                if (message.shouldExit())
                    radioBlockEntity.setBeingUsed(new UUID(0, 0));
                else {
                    radioBlockEntity.setUrl(message.url());

                    if (message.tick() != -1)
                        radioBlockEntity.setTick(message.tick());

                    radioBlockEntity.setVolume(message.volume());
                    radioBlockEntity.setPlaying(message.isPlaying());

                    radioBlockEntity.notifyPlayer();
                }
            }
        });
    }
}

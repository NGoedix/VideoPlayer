package com.github.NGoedix.videoplayer.network.packet;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

public class UpdateVideoMessage {

    public static final Identifier ID = new Identifier(Constants.MOD_ID, "update_video");

    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
        Constants.LOGGER.info("Received update video message");

        BlockPos pos = buf.readBlockPos();
        String url = buf.readString();
        int volume = buf.readInt();
        boolean loop = buf.readBoolean();
        boolean isPlaying = buf.readBoolean();
        boolean reset = buf.readBoolean();

        server.execute(() -> {
            if (player.getWorld().getBlockEntity(pos) instanceof TVBlockEntity tvBlockEntity) {
                tvBlockEntity.setBeingUsed(new UUID(0, 0));
                if (volume == -1) // NO UPDATE
                    return;

                tvBlockEntity.setUrl(url);
                Constants.LOGGER.info("Received url: " + url);
                tvBlockEntity.setVolume(volume);
                tvBlockEntity.setLoop(loop);
                tvBlockEntity.setPlaying(isPlaying);
                tvBlockEntity.notifyPlayer();

                if (reset)
                    tvBlockEntity.setTick(0);
            }
        });
    }
}

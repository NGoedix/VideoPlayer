package com.github.NGoedix.videoplayer.client;

import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.client.render.TVVideoScreen;
import com.github.NGoedix.videoplayer.client.render.VideoScreen;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("Initializing Client");

        PacketHandler.registerS2CPackets();
    }

    public static void openVideo(MinecraftClient client, String url, int volume){
        client.execute(() -> client.setScreen(new VideoScreen(url, volume)));
    }

    public static void manageVideo(MinecraftClient client, BlockPos pos, boolean playing, int tick) {
        client.execute(() -> {
            BlockEntity be = client.getInstance().world.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setPlaying(playing);
                tv.setTick(tick);
                if (tv.display != null) {
                    if (playing)
                        tv.display.resume(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
                    else
                        tv.display.pause(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
                }
            }
        });
    }

    public static void openVideoGUI(MinecraftClient client, BlockPos pos, String url, int tick, int volume, boolean loop) {
        client.execute(() -> {
            BlockEntity be = client.world.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setTick(tick);
                tv.volume = volume;
                tv.loop = loop;
                client.setScreen(new TVVideoScreen(be, url, tick, volume, loop));
            }
        });

    }
}

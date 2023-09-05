package com.github.NGoedix.watchvideo.client;

import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.TVVideoScreen;
import com.github.NGoedix.watchvideo.client.gui.VideoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ClientHandler {
    public static void openVideo(String url, int volume) {
        Minecraft.getInstance().setScreen(new VideoScreen(url, volume));
    }

    public static void manageVideo(BlockPos pos, boolean playing, int tick) {
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (be instanceof TVBlockEntity tv) {
            tv.setPlaying(playing);
            tv.setTick(tick);
            if (tv.display != null) {
                if (playing)
                    tv.display.resume(tv.getUrl(), tv.getVolume(), tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.isLoop(), tv.getTick());
                else
                    tv.display.pause(tv.getUrl(), tv.getVolume(), tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.isLoop(), tv.getTick());
            }
        }
    }

    public static void openVideoGUI(BlockPos pos, String url, int tick, int volume, boolean loop) {
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (be instanceof TVBlockEntity tv) {
            tv.setUrl(url);
            tv.setTick(tick);
            tv.setVolume(volume);
            tv.setLoop(loop);
            Minecraft.getInstance().setScreen(new TVVideoScreen(be, url, volume));
        }
    }
}

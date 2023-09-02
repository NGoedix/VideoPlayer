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
            if (tv.requestDisplay() != null) {
                if (playing)
                    tv.requestDisplay().resume(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
                else
                    tv.requestDisplay().pause(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
            }
        }
    }

    public static void openVideoGUI(BlockPos pos, String url, int tick, int volume, boolean loop) {
        BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (be instanceof TVBlockEntity tv) {
            tv.setUrl(url);
            tv.setTick(tick);
            tv.volume = volume;
            tv.loop = loop;
            Minecraft.getInstance().setScreen(new TVVideoScreen(be, url, tick, volume, loop));
        }
    }
}

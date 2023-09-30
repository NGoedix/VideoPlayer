package com.github.NGoedix.watchvideo.client;

import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.TVVideoScreen;
import com.github.NGoedix.watchvideo.client.gui.VideoScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientHandler {

    public static void openVideo(String url, int volume, boolean controlBlocked) {
        Minecraft.getInstance().setScreen(new VideoScreen(url, volume, controlBlocked));
    }

    public static void manageVideo(BlockPos pos, boolean playing, int tick) {
        TileEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (be instanceof TVBlockEntity) {
            TVBlockEntity tv = (TVBlockEntity) be;
            tv.setPlaying(playing);
            tv.setTick(tick);
            if (tv.requestDisplay() != null) {
                if (playing)
                    tv.requestDisplay().resume(tv.getUrl(), tv.getVolume(), tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.isLoop(), tv.getTick());
                else
                    tv.requestDisplay().pause(tv.getUrl(), tv.getVolume(), tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.isLoop(), tv.getTick());
            }
        }
    }

    public static void openVideoGUI(BlockPos pos, String url, int tick, int volume, boolean loop) {
        TileEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
        if (be instanceof TVBlockEntity) {
            TVBlockEntity tv = (TVBlockEntity) be;
            tv.setUrl(url);
            tv.setTick(tick);
            tv.setVolume(volume);
            tv.setLoop(loop);
            Minecraft.getInstance().setScreen(new TVVideoScreen(be, url, volume));
        }
    }
}

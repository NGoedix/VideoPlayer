package com.github.NGoedix.watchvideo.util.vlc;

import me.lib720.caprica.vlcj.player.base.MediaPlayer;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.RenderCallbackAdapter;

import java.nio.ByteBuffer;

public class MediaPlayerCallback extends RenderCallbackAdapter {
    private final MediaPlayerBase mediaPlayer;
    private int width;

    public MediaPlayerCallback(int width, MediaPlayerBase mediaPlayer) {
        this.width = width;
        this.mediaPlayer = mediaPlayer;
    }

    public void setBuffer(int sourceWidth, int sourceHeight) {
        this.width = sourceWidth;
        setBuffer(new int[sourceWidth * sourceHeight]);
    }

    @Override
    protected void onDisplay(MediaPlayer mediaPlayer, int[] buffer) {
        this.mediaPlayer.setIntBuffer(new IntegerBuffer2D(width, buffer));
    }
}

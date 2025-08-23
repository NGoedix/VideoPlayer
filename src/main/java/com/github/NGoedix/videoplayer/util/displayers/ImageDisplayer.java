package com.github.NGoedix.videoplayer.util.displayers;

import net.minecraft.client.Minecraft;
import org.watermedia.api.image.ImageRenderer;

import java.awt.*;

public class ImageDisplayer implements IDisplay {

    public final ImageRenderer picture;
    private String url;

    public ImageDisplayer(ImageRenderer picture) {
        this.picture = picture;
        this.url = null;
    }

    @Override
    public int maxTick() {
        return 0;
    }

    @Override
    public boolean isStopped() {
        return true;
    }

    @Override
    public boolean isPlaying() {
        return true;
    }

    @Override
    public int prepare(String url, boolean playing, boolean loop, int tick) {
        this.url = url;
        long time = tick * 50L + (playing ? (long) (Minecraft.getInstance().isPaused() ? 1.0F : Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(true) * 50) : 0);
        long duration = picture.duration;
        if (duration > 0 && time > duration && loop) time %= duration;
        return picture.texture(time);
    }

    @Override
    public int getRenderTexture() {
        return picture.texture(0);
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void pause(int tick) {}

    @Override
    public void resume(int tick) {}

    @Override
    public void stop() {}

    @Override
    public void release() {
        if (picture != null)
            picture.release();
    }

    @Override
    public Dimension getDimensions() {
        if (picture == null) return new Dimension(0, 0);
        return new Dimension(picture.width, picture.height);
    }
}

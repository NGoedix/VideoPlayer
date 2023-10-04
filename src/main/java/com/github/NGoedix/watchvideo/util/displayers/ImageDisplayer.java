package com.github.NGoedix.watchvideo.util.displayers;

import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

import java.awt.*;

public class ImageDisplayer implements IDisplay {

    public final ImageRenderer picture;

    public ImageDisplayer(ImageRenderer picture) {
        this.picture = picture;
    }

    @Override
    public int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        long time = tick * 50L + (playing ? (long) (Minecraft.getMinecraft().isGamePaused() ? 1.0F : Minecraft.getMinecraft().getRenderPartialTicks() * 50) : 0);
        long duration = picture.duration;
        if (duration > 0 && time > duration && loop) time %= duration;
        return picture.texture(time);
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}

    @Override
    public void release() {
        picture.release();
    }

    @Override
    public Dimension getDimensions() {
        return new Dimension(picture.width, picture.height);
    }
}

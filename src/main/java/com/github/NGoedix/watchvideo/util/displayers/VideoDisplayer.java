package com.github.NGoedix.watchvideo.util.displayers;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.util.MemoryTracker;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import com.mojang.blaze3d.platform.GlStateManager;
import me.lib720.watermod.safety.TryCore;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class VideoDisplayer implements IDisplay {

    private static final String VLC_FAILED = "https://i.imgur.com/XCcN2uX.png";

    private static final int ACCEPTABLE_SYNC_TIME = 1000;

    private static final List<VideoDisplayer> OPEN_DISPLAYS = new ArrayList<>();

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (VideoDisplayer display: OPEN_DISPLAYS) {
                if (Minecraft.getInstance().isPaused()) {
                    SyncVideoPlayer media = display.player;
                    if (media.isPlaying() && display.player.isLive()) media.setPauseMode(true);
                    else if (media.getDuration() > 0 && media.isPlaying()) media.setPauseMode(true);
                }
            }
        }
    }

    public static void unload() {
        synchronized (OPEN_DISPLAYS) {
            for (VideoDisplayer display : OPEN_DISPLAYS) display.free();
            OPEN_DISPLAYS.clear();
        }
    }

    public static IDisplay createVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing) {
        return TryCore.withReturn((defaultVar) -> {
            VideoDisplayer display = new VideoDisplayer(pos, url, volume, minDistance, maxDistance, loop);
            if (display.player.raw() == null) throw new IllegalStateException("MediaDisplay uses a broken player");
            OPEN_DISPLAYS.add(display);
            return display;

        }, ((Supplier<IDisplay>) () -> {
            TextureCache cache = TextureCache.get(VLC_FAILED);
            if (cache.ready()) return cache.createDisplay(pos, VLC_FAILED, volume, minDistance, maxDistance, loop, playing);
            return null;
        }).get());
    }

    public SyncVideoPlayer player;

    private final Vec3d pos;
    private float lastSetVolume;
    private long lastCorrectedTime = Long.MIN_VALUE;

    public VideoDisplayer(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.pos = pos;

        if (url.isEmpty()) return;

        player = new SyncVideoPlayer(null, Minecraft.getInstance(), MemoryTracker::create);
        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        player.setVolume((int) volume);
        lastSetVolume = volume;
        player.setRepeatMode(loop);
        player.start(url);
    }

    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null)
            return 0;

        Minecraft mc = Minecraft.getInstance();
        float distance = (float) pos.distance(Objects.requireNonNull(Minecraft.getInstance().player).getPosition(mc.isPaused() ? 1.0F : mc.getFrameTime()));
        if (minDistance > maxDistance) {
            float temp = maxDistance;
            maxDistance = minDistance;
            minDistance = temp;
        }

        if (distance > minDistance)
            if (distance > maxDistance)
                volume = 0;
            else
                volume *= 1 - ((distance - minDistance) / (maxDistance - minDistance));
        return (int) (volume * 100F);
    }

    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null || url == null)
            return;

        Reference.LOGGER.info("Tick: " + tick);

        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        if (volume != lastSetVolume) {
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }

        if (player.isValid()) {
            boolean realPlaying = playing && !Minecraft.getInstance().isPaused();

            if (player.getRepeatMode() != loop)
                player.setRepeatMode(loop);
            long tickTime = 50;
            if (player.isLive()) {
                if (player.isPlaying() != realPlaying)
                    player.setPauseMode(!realPlaying);
            } else {
                if (player.getDuration() > 0) {
                    if (player.isPlaying() != realPlaying)
                        player.setPauseMode(!realPlaying);

                    if (player.isSeekAble()) {
                        Minecraft mc = Minecraft.getInstance();
                        long time = tick * tickTime + (realPlaying ? (long) (mc.isPaused() ? 1.0F : mc.getFrameTime() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getDuration();
                        if (Math.abs(time - player.getTime()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                            lastCorrectedTime = time;
                            player.seekTo(time);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int maxTick() {
        return IDisplay.super.maxTick();
    }

    @Override
    public int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return -1;
        return player.prepareTexture();
    }

    public void free() {
        if (player != null) {
            player.release();
            if (player.getTexture() != -1) {
                GlStateManager._deleteTexture(player.getTexture());
            }
            player = null;
        }
    }

    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }

    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekTo(tick);
        player.pause();
    }

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekTo(tick);
        player.play();
    }

    @Override
    public Dimension getDimensions() {
        if (player == null) return null;
        return player.getDimensions();
    }
}

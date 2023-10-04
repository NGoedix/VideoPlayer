package com.github.NGoedix.watchvideo.util.displayers;

import com.github.NGoedix.watchvideo.util.MemoryTracker;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import com.github.NGoedix.watchvideo.util.math.Vector3d;
import me.lib720.watermod.safety.TryCore;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VideoDisplayer implements IDisplay {

    private static final String VLC_FAILED = "https://i.imgur.com/XCcN2uX.png";

    private static final int ACCEPTABLE_SYNC_TIME = 1500;

    private static final List<VideoDisplayer> OPEN_DISPLAYS = new ArrayList<>();

    private boolean stream = false;

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (VideoDisplayer display: OPEN_DISPLAYS) {
                if (Minecraft.getMinecraft().isGamePaused()) {
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

        player = new SyncVideoPlayer(null, Runnable::run, MemoryTracker::create);
        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        player.setVolume((int) volume);
        lastSetVolume = volume;
        player.setRepeatMode(loop);
        player.start(url);
    }

    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null)
            return 0;

        float distance = (float) pos.distance(new Vector3d(Minecraft.getMinecraft().player.getPosition().getX(), Minecraft.getMinecraft().player.getPosition().getY(), Minecraft.getMinecraft().player.getPosition().getZ()));
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

        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        if (volume != lastSetVolume) {
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }

        if (player.isSafeUse() && player.isValid()) {
            if (!stream && player.isLive()) stream = true;

            boolean currentPlaying = playing && !Minecraft.getMinecraft().isGamePaused();

            player.setPauseMode(!currentPlaying);
            if (!stream && player.isSeekAble()) {
                long time = WaterMediaAPI.math_ticksToMillis(tick);
                if (time > player.getTime()) time = floorMod(time, player.getMediaInfoDuration());

//                Reference.LOGGER.info("Tick: " + tick + " Time: " + time + " Player time: " + player.getTime() + " correction: " + Math.abs(time - player.getTime()));

                if (Math.abs(time - player.getTime()) > ACCEPTABLE_SYNC_TIME && Math.abs(time - lastCorrectedTime) > ACCEPTABLE_SYNC_TIME) {
                    lastCorrectedTime = time;
                    player.seekTo(time);
                }
            }
        }
    }

    public static long floorMod(long x, long y) {
        try {
            final long r = x % y;
            // if the signs are different and modulo not zero, adjust result
            if ((x ^ y) < 0 && r != 0) {
                return r + y;
            }
            return r;
        } catch (ArithmeticException e) {
            return 0;
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
                GlStateManager.deleteTexture(player.getTexture());
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

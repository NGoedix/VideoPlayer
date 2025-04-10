package com.github.NGoedix.watchvideo.util.displayers;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.VideoPlayerBlockEntity;
import com.github.NGoedix.watchvideo.util.config.TVConfig;
import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.github.NGoedix.watchvideo.util.math.geo.Vec3d;
import net.minecraft.client.Minecraft;
import org.watermedia.api.math.MathAPI;
import org.watermedia.api.player.videolan.BasePlayer;
import org.watermedia.api.player.videolan.MusicPlayer;
import org.watermedia.api.player.videolan.VideoPlayer;

import java.awt.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Display {

    private static final List<Display> DISPLAYS = new ArrayList<>();

    public static void tick() {
        synchronized (DISPLAYS) {
            for (Display display : new ArrayList<>(DISPLAYS)) {
                if (display.be.isRemoved()) // I don't know why but the block is not releasing correctly the display, so I hope that this fixes that
                    display.release();
                if (Minecraft.getInstance().isPaused() && display.be.isPlaying() &&
                        (display.player.isLive() || display.player.getDuration() > 0))
                    display.player.setPauseMode(true);
            }
        }
    }

    public static void unload() {
        synchronized (DISPLAYS) {
            DISPLAYS.forEach(Display::internalRelease);
            DISPLAYS.clear();
        }
    }

    public enum DisplayType {
        VIDEO,
        MUSIC
    }

    private final VideoPlayerBlockEntity be;

    private final Vec3d pos;
    private final DisplayType type;
    private BasePlayer player;

    private float lastSetVolume;

    private boolean stream = false;

    private long lastCorrectedTime = Long.MIN_VALUE;

    public Display(VideoPlayerBlockEntity be, URI url, DisplayType type) {
        this.pos = new Vec3d(be.getBlockPos());
        this.be = be;
        this.type = type;
        this.player = type == DisplayType.VIDEO ? new VideoPlayer(Minecraft.getInstance()) : new MusicPlayer();

        this.player.setVolume(be.getVolume());
        this.player.setRepeatMode(true);
        this.player.setPauseMode(!be.isPlaying());
        this.player.setMuteMode(false);
        this.player.start(url);

        synchronized (DISPLAYS) {
            DISPLAYS.add(this);
        }
    }

    public void tick(int tick) {
        if (player == null || player.isStopped())
            return;

        // Change volume if needed
        if (be.getVolume() != lastSetVolume) {
            player.setVolume(calculateVolume(be.getVolume()));
            lastSetVolume = be.getVolume();
        }

        // If player is safe run the tick
        if (player.isSafeUse() && player.isValid()) {
            if (!stream && player.isLive()) stream = true;

            // Change pause mode
            boolean currentPlaying = be.isPlaying() && !Minecraft.getInstance().isPaused();
            player.setPauseMode(!currentPlaying);

            // Sync time
            if (!stream && player.isSeekAble()) {
                long time = MathAPI.tickToMs(tick);
                if (time > player.getTime()) time = VideoMathUtil.floorMod(time, player.getMediaInfoDuration());

                if (Math.abs(time - player.getTime()) > TVConfig.SYNC_TIME && Math.abs(time - lastCorrectedTime) > TVConfig.SYNC_TIME) {
                    lastCorrectedTime = time;
                    player.seekTo(time);
                }
            }
        }
    }

    private int calculateVolume(float volume) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return 0;
        float distance = (float) pos.distanceTo(mc.player.getPosition(mc.isPaused() ? 1.0F : mc.getFrameTime()));
        volume = VideoMathUtil.calculateVolume(volume, distance, TVConfig.MIN_DISTANCE, TVConfig.MAX_DISTANCE);
        return (int) volume;
    }

    public int renderTexture() {
        if (be.isURLEmpty()) return 0;
        switch (type) {
            case VIDEO:
                return ((VideoPlayer) player).preRender();
            case MUSIC:
            default:
                return 0;
        }
    }

    public void stop() {
        if (player == null) return;
        player.stop();
    }

    public void setPauseMode(boolean pause) {
        if (player == null) return;
        if (player.isStopped() && !pause) player.play();
        player.setPauseMode(pause);
    }

    public void seekTo(long tick) {
        if (player == null) return;
        if (player.isReady() && !player.isLive())
            player.seekTo(tick);
    }

    public boolean isReady() {
        if (player == null) return false;
        return player.isReady();
    }

    public boolean isLive() {
        if (player == null) return false;
        return player.isLive();
    }

    public boolean isPlaying() {
        if (player == null) return false;
        return player.isPlaying();
    }

    public boolean isStopped() {
        if (player == null) return false;
        return player.isStopped();
    }

    public Dimension getDimensions() {
        if (player == null || type == DisplayType.MUSIC) return new Dimension(0, 0);
        return ((VideoPlayer) player).dimension();
    }

    public long getDuration() {
        if (player == null) return 0;
        return player.getMediaInfoDuration();
    }

    public long getTime() {
        if (player == null) return 0;
        return player.getTime();
    }

    private void internalRelease() {
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }

    public void release() {
        internalRelease();

        synchronized (DISPLAYS) {
            DISPLAYS.remove(this);
        }
    }
}

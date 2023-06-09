package com.github.NGoedix.videoplayer.util.displayers;

import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.math.Vec3d;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj4.player.embedded.videosurface.callback.BufferFormat;
import me.lib720.caprica.vlcj4.player.embedded.videosurface.callback.BufferFormatCallback;
import me.srrapero720.watermedia.api.media.players.WaterVLCPlayer;
import me.srrapero720.watermedia.internal.util.ThreadUtil;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.util.GlAllocationUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class VideoDisplayer implements IDisplay {

    private static final String VLC_FAILED = "https://i.imgur.com/UAXbZeM.jpg";

    private static final int ACCEPTABLE_SYNC_TIME = 1000;
    
    private static final List<VideoDisplayer> OPEN_DISPLAYS = new ArrayList<>();

    public static void tick() {
        synchronized (OPEN_DISPLAYS) {
            for (var display: OPEN_DISPLAYS) {
                if (MinecraftClient.getInstance().isPaused()) {
                    var media = display.player;
                    if (display.stream && media.isPlaying()) media.setPauseMode(true);
                    else if (media.getMediaLength() > 0 && media.isPlaying()) media.setPauseMode(true);
                }
            }
        }
    }

    public static void unload() {
        synchronized (OPEN_DISPLAYS) {
            for (var display : OPEN_DISPLAYS) display.free();
            OPEN_DISPLAYS.clear();
        }
    }

    public static IDisplay createVideoDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return ThreadUtil.tryAndReturn((defaultVar) -> {
            var display = new VideoDisplayer(pos, url, volume, minDistance, maxDistance, loop);
            OPEN_DISPLAYS.add(display);
            return display;

        }, ((Supplier<IDisplay>) () -> {
            var cache = TextureCache.get(VLC_FAILED);
            if (cache.ready()) return cache.createDisplay(pos, VLC_FAILED, volume, minDistance, maxDistance, loop, true);
            return null;
        }).get());
    }
    
    public volatile int width = 1;
    public volatile int height = 1;
    
    public WaterVLCPlayer player;
    
    private final Vec3d pos;
    public volatile IntBuffer buffer;
    public int texture;
    private boolean stream = false;
    private float lastSetVolume;
    private volatile boolean needsUpdate = false;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean first = true;
    private long lastCorrectedTime = Long.MIN_VALUE;
    
    public VideoDisplayer(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        super();
        this.pos = pos;
        texture = GlStateManager._genTexture();

        player = new WaterVLCPlayer(url, (mediaPlayer, nativeBuffers, bufferFormat) -> {
            lock.lock();
            try {
                buffer.put(nativeBuffers[0].asIntBuffer());
                buffer.rewind();
                needsUpdate = true;
            } finally {
                lock.unlock();
            }
        }, new BufferFormatCallback() {

            @Override
            public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
                lock.lock();
                try {
                    VideoDisplayer.this.width = sourceWidth;
                    VideoDisplayer.this.height = sourceHeight;
                    VideoDisplayer.this.first = true;
                    buffer = GlAllocationUtils.allocateByteBuffer(sourceWidth * sourceHeight * 4).asIntBuffer();
                    needsUpdate = true;
                } finally {
                    lock.unlock();
                }
                return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * 4 }, new int[] { sourceHeight });
            }

            @Override
            public void allocatedBuffers(ByteBuffer[] buffers) {}

        });
        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        player.setVolume((int) volume);
        lastSetVolume = volume;
        player.setRepeatMode(loop);
        player.start(url);
    }
    
    public int getVolume(float volume, float minDistance, float maxDistance) {
        if (player == null)
            return 0;
        MinecraftClient mc = MinecraftClient.getInstance();
        float distance = (float) pos.distance(mc.player.getLeashPos(mc.isPaused() ? 1.0F : mc.getTickDelta()));
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
        if (player == null)
            return;
        
        volume = pos != null ? getVolume(volume, minDistance, maxDistance) : volume;
        if (volume != lastSetVolume) {
            player.setVolume((int) volume);
            lastSetVolume = volume;
        }
        
        if (player.isValid()) {
            boolean realPlaying = playing && !MinecraftClient.getInstance().isPaused();
            
            if (player.getRepeatMode() != loop)
                player.setRepeatMode(loop);
            long tickTime = 50;
            long newDuration = player.getMediaLength();
            if (!stream && newDuration != -1 && newDuration != 0 && player.getDuration() == 0)
                stream = true;
            if (stream) {
                if (player.isPlaying() != realPlaying)
                    player.setPauseMode(!realPlaying);
            } else {
                if (player.getMediaLength() > 0) {
                    if (player.isPlaying() != realPlaying)
                        player.setPauseMode(!realPlaying);

                    if (player.isSeekable()) {
                        MinecraftClient mc = MinecraftClient.getInstance();
                        long time = tick * tickTime + (realPlaying ? (long) (mc.isPaused() ? 1.0F : mc.getTickDelta() * tickTime) : 0);
                        if (time > player.getTime() && loop)
                            time %= player.getMediaLength();
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
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null)
            return;
        lock.lock();
        try {
            if (needsUpdate) {
                // fixes random crash, when values are too high it causes a jvm crash, caused weird behavior when game is paused
                GlStateManager._pixelStore(3314, 0);
                GlStateManager._pixelStore(3316, 0);
                GlStateManager._pixelStore(3315, 0);
                RenderSystem.bindTexture(texture);
                if (first) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                    first = false;
                } else
                    GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                needsUpdate = false;
            }
        } finally {
            lock.unlock();
        }
        
    }
    
    public void free() {
        if (player != null)
            player.release();
        if (texture != -1) {
            GlStateManager._deleteTexture(texture);
            texture = -1;
        }
        player = null;
    }
    
    @Override
    public void release() {
        free();
        synchronized (OPEN_DISPLAYS) {
            OPEN_DISPLAYS.remove(this);
        }
    }
    
    @Override
    public int texture() {
        return texture;
    }

    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.pause();
    }

    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        if (player == null) return;
        player.seekGameTicksTo(tick);
        player.play();
    }
    
    @Override
    public int getWidth() {
        return width;
    }
    
    @Override
    public int getHeight() {
        return height;
    }
    
}

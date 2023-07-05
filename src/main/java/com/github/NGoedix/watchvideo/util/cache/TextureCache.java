package com.github.NGoedix.watchvideo.util.cache;

import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.displayers.ImageDisplayer;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.github.NGoedix.watchvideo.util.math.Vec3d;
import com.mojang.blaze3d.platform.GlStateManager;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.external.GifDecoder;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TextureCache {
    private static final Map<String, TextureCache> cached = new HashMap<>();

    public static void renderInternal() {
        for (Iterator<TextureCache> iterator = cached.values().iterator(); iterator.hasNext();) {
            var type = iterator.next();
            if (!type.isUsed()) {
                type.remove();
                iterator.remove();
            }
        }
    }

    public static void tick() {
        VideoDisplayer.tick();
    }


    
    public static void reloadAll() {
        for (var cache : cached.values())
            cache.reload();
    }
    
    @SubscribeEvent
    public static void unload(WorldEvent.Unload event) {
        if (event.getWorld().isClientSide()) {
            for (TextureCache cache : cached.values())
                cache.remove();
            cached.clear();
            VideoDisplayer.unload();
        }
    }
    
    public static TextureCache get(String url) {
        TextureCache cache = cached.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureCache(url);
        cached.put(url, cache);
        return cache;
    }
    
    public final String url;
    private int[] textures;
    private int width;
    private int height;
    private long[] delay;
    private long duration;
    private boolean isVideo;
    
    private TextureSeeker seeker;
    private boolean ready = false;
    private String error;
    
    private int usage = 0;
    
    private GifDecoder decoder;
    private int remaining;
    
    public TextureCache(String url) {
        this.url = url;
        use();
        trySeek();
    }
    
    private void trySeek() {
        if (seeker != null)
            return;
        synchronized (TextureSeeker.LOCK) {
            if (TextureSeeker.activeDownloads < TextureSeeker.MAXIMUM_ACTIVE_DOWNLOADS)
                this.seeker = new TextureSeeker(this);
        }
    }
    
    private int getTexture(int index) {
        if (textures[index] == -1 && decoder != null) {
            textures[index] = WaterMediaAPI.preRender(decoder.getFrame(index), width, height);
            remaining--;
            if (remaining <= 0)
                decoder = null;
        }
        return textures[index];
    }
    
    public int getTexture(long time) {
        if (textures == null)
            return -1;
        if (textures.length == 1)
            return getTexture(0);
        int last = getTexture(0);
        for (int i = 1; i < delay.length; i++) {
            if (delay[i] > time)
                break;
            last = getTexture(i);
        }
        return last;
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop) {
        return createDisplay(pos, url, volume, minDistance, maxDistance, loop, false);
    }
    
    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean noVideo) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (textures == null && !noVideo)
            return VideoDisplayer.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop);
        return new ImageDisplayer(this);
    }
    
    public String getError() {
        return error;
    }
    
    public void processVideo() {
        this.textures = null;
        this.error = null;
        this.isVideo = true;
        this.ready = true;
        this.seeker = null;
    }
    
    public void processFailed(String error) {
        this.textures = null;
        this.error = error;
        this.ready = true;
        this.seeker = null;
    }
    
    public void process(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        textures = new int[] { WaterMediaAPI.preRender(image, width, height) };
        delay = new long[] { 0 };
        duration = 0;
        seeker = null;
        ready = true;
    }
    
    public void process(GifDecoder decoder) {
        Dimension frameSize = decoder.getFrameSize();
        width = (int) frameSize.getWidth();
        height = (int) frameSize.getHeight();
        textures = new int[decoder.getFrameCount()];
        delay = new long[decoder.getFrameCount()];
        
        this.decoder = decoder;
        this.remaining = decoder.getFrameCount();
        long time = 0;
        for (int i = 0; i < decoder.getFrameCount(); i++) {
            textures[i] = -1;
            delay[i] = time;
            time += decoder.getDelay(i);
        }
        duration = time;
        seeker = null;
        ready = true;
    }
    
    public boolean ready() {
        if (ready)
            return true;
        trySeek();
        return false;
    }
    
    public boolean isVideo() {
        return isVideo;
    }
    
    public void reload() {
        remove();
        error = null;
        trySeek();
    }
    
    public void use() {
        usage++;
    }
    
    public void unuse() {
        usage--;
    }
    
    public boolean isUsed() {
        return usage > 0;
    }
    
    public void remove() {
        ready = false;
        if (textures != null) for (int texture: textures) GlStateManager._deleteTexture(texture);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public long[] getDelay() {
        return delay;
    }
    
    public long getDuration() {
        return duration;
    }
    
    public boolean isAnimated() {
        return textures.length > 1;
    }
    
    public int getFrameCount() {
        return textures.length;
    }
}

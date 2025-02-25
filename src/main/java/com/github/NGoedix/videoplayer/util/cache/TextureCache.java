package com.github.NGoedix.videoplayer.util.cache;

import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.displayers.ImageDisplayer;
import com.github.NGoedix.videoplayer.util.displayers.VideoDisplayer;
import com.github.NGoedix.videoplayer.util.math.geo.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.watermedia.api.image.ImageFetch;
import org.watermedia.api.image.ImageRenderer;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TextureCache {
    private static final Map<String, TextureCache> CACHE = new HashMap<>();

    public final String url;
    private volatile ImageFetch seeker;
    private volatile ImageRenderer picture;
    private volatile String error;
    private volatile boolean ready = false;
    private volatile boolean isVideo = false;
    private final AtomicInteger usage = new AtomicInteger();

    private TextureCache(String url) {
        this.url = url;
        use();
        attemptToLoad();
    }

    public static TextureCache get(String url) {
        TextureCache cache = CACHE.get(url);
        if (cache != null) {
            cache.use();
            return cache;
        }
        cache = new TextureCache(url);
        CACHE.put(url, cache);
        return cache;
    }

    public ImageRenderer getPicture() {
        return picture;
    }

    private synchronized void attemptToLoad() {
        if (this.seeker != null) return;
        if (!this.url.isEmpty()) {
            this.seeker = new FramePictureFetcher(this, url);
            this.seeker.start();
        }
    }

    public IDisplay createDisplay(Vec3d pos, String url, float volume, float minDistance, float maxDistance, boolean loop, boolean playing, boolean isOnlyMusic) {
        volume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
        if (picture == null) return VideoDisplayer.createVideoDisplay(pos, url, volume, minDistance, maxDistance, loop, playing, isOnlyMusic);

        return new ImageDisplayer(picture) {
            @Override
            public void release() { deuse(); }
        };
    }

    public void process(ImageRenderer picture) {
        if (ready) return;
        this.ready = true;
        this.seeker = null;
        this.picture = picture;
        this.error = null;
        this.isVideo = false;
    }

    public void processVideo() {
        this.ready = true;
        this.seeker = null;
        this.picture = null;
        this.error = null;
        this.isVideo = true;
    }

    public void processFailed(String error) {
        this.error = error;
        this.seeker = null;
        this.picture = null;
        this.ready = true;
        this.isVideo = false;
    }

    public synchronized boolean ready() {
        if (ready || seeker == null) return true;
        attemptToLoad();
        return false;
    }

    public String getError() { return error; }
    public boolean isVideo() { return isVideo; }
    public void use() { usage.incrementAndGet(); }
    public void deuse() { usage.decrementAndGet(); }
    public boolean isUsed() { return usage.get() > 0; }

    public void remove() {
        ready = false;
        if (picture != null) {
            picture.release();
            Arrays.fill(picture.textures, -1);
        }
        picture = null;
        seeker = null;
    }

    public static void clientTick() { CACHE.values().removeIf(o -> !o.isUsed()); }
    public static void renderTick() { VideoDisplayer.tick(); }
    public static void unload() { for (TextureCache cache : CACHE.values()) cache.remove(); CACHE.clear(); }

    private static final class FramePictureFetcher extends ImageFetch {
        public FramePictureFetcher(TextureCache cache, String originalURL) {
            super(URI.create(originalURL));

            setSuccessCallback((imageRenderer, isCache) -> Minecraft.getInstance().executeBlocking(() -> cache.process(imageRenderer)));

            setErrorCallback((e, isVideo) -> Minecraft.getInstance().executeBlocking(() -> {
                if (isVideo) {
                    cache.processVideo();
                    return;
                }

                if (!cache.isVideo()) {
                    if (e == null) cache.processFailed("download.exception.gif");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 403")) cache.processFailed("download.exception.forbidden");
                    else if (e.getMessage().startsWith("Server returned HTTP response code: 404")) cache.processFailed("download.exception.notfound");
                    else cache.processFailed("download.exception.invalid");
                }
            }));
        }
    }
}

package com.github.NGoedix.watchvideo.util.displayers;

import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import net.minecraft.client.Minecraft;

public class ImageDisplayer implements IDisplay {
    
    public final TextureCache texture;
    private int textureId;
    
    public ImageDisplayer(TextureCache texture) {
        this.texture = texture;
    }
    
    @Override
    public void prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {
        Minecraft mc = Minecraft.getInstance();
        long time = tick * 50L + (playing ? (long) ((mc.isPaused() ? 1.0F : mc.getFrameTime()) * 50) : 0);
        if (texture.getDuration() > 0 && time > texture.getDuration())
            if (loop)
                time %= texture.getDuration();
        textureId = texture.getTexture(time);
    }
    
    @Override
    public void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick) {}
    
    @Override
    public int texture() {
        return textureId;
    }
    
    @Override
    public void release() {
        texture.unuse();
    }
    
    @Override
    public int getWidth() {
        return texture.getWidth();
    }
    
    @Override
    public int getHeight() {
        return texture.getHeight();
    }
}

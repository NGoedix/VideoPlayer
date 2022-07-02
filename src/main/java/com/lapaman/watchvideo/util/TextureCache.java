package com.lapaman.watchvideo.util;

import com.lapaman.watchvideo.Reference;
import com.lapaman.watchvideo.WatchVideoMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TextureCache {

    /**
     * <b>Integer</b>: Number of frame
     * <b>BufferedImage</b>: Specific frame
     */
    public Map<Integer, CameraTextureObject> clientVideoCache;
    public Map<Integer, ResourceLocation> clientResourceCache;

    private String videoTitle = "";
    private double fps;

    private static TextureCache instance;

    private static long beginVideo;

    public TextureCache() {
        clientVideoCache = new HashMap<>();
        clientResourceCache = new HashMap<>();
    }

    public void addFrame(Integer frame, BufferedImage image) {
        ResourceLocation resourceLocation = new ResourceLocation(Reference.MOD_ID, "textures/lapamanvideo/" + videoTitle + '/' + frame);
        CameraTextureObject cameraTextureObject = new CameraTextureObject(resourceLocation, image);
        clientVideoCache.put(frame, cameraTextureObject);
        clientResourceCache.put(frame, resourceLocation);
        Minecraft.getMinecraft().renderEngine.loadTexture(resourceLocation, cameraTextureObject);
    }

    public void addVideo(String videoTitle, BufferedImage frame, double fps) {
        if (!(this.videoTitle.equals(videoTitle))) {
            clientVideoCache = new HashMap<>();
            clientResourceCache = new HashMap<>();
            this.videoTitle = videoTitle;
            this.fps = fps;
        }

        if ((clientResourceCache.size() + 1) % 20 == 0)
            WatchVideoMod.getWatchVideoMod().getLogger().info("Getting frame " + (clientResourceCache.size()  + 1) + "...");

        addFrame(clientResourceCache.size() + 1, frame);
    }

    public ResourceLocation getFrame(Integer frame) {
        return clientResourceCache.get(frame);
    }

    public BufferedImage getBufferedImage() {
        if (beginVideo > System.currentTimeMillis())
            return null;
        int ACTUAL_FRAME = (int) (((System.currentTimeMillis() - beginVideo) / 1000) / (1 / fps));
        return clientVideoCache.get(ACTUAL_FRAME).image;
    }

    public static class CameraTextureObject extends SimpleTexture {
        private final BufferedImage image;

        public CameraTextureObject(ResourceLocation textureResourceLocation, BufferedImage image) {
            super(textureResourceLocation);
            this.image = image;
        }

        @ParametersAreNonnullByDefault
        @Override
        public void loadTexture(IResourceManager resourceManager) {
            TextureUtil.uploadTextureImage(super.getGlTextureId(), image);
        }
    }

    public static TextureCache instance() {
        if (instance == null) {
            beginVideo = System.currentTimeMillis() + 30000;
            return instance = new TextureCache();
        }
        return instance;
    }

    public ResourceLocation getImage() {
        if (beginVideo > System.currentTimeMillis())
            return null;
        int ACTUAL_FRAME = (int) (((System.currentTimeMillis() - beginVideo) / 1000) / (1 / fps));
        return clientResourceCache.get(ACTUAL_FRAME);
    }
}

package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.lib720.caprica.vlcj.media.MediaRef;
import me.lib720.caprica.vlcj.media.TrackType;
import me.lib720.caprica.vlcj.player.base.MediaPlayer;
import me.lib720.caprica.vlcj.player.base.MediaPlayerEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    private static final ClassLoader CL = VideoScreen.class.getClassLoader();
    private final String url;
    private final int volume;
    private int tick;
    private final AtomicBoolean ENDED = new AtomicBoolean(false);

    private boolean firstIteration;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;

    @Override
    protected void init() {
        if (Minecraft.getInstance().screen != null) {
            this.imageWidth = Minecraft.getInstance().screen.width;
            this.imageHeight = Minecraft.getInstance().screen.height;
        }
        super.init();
    }

    public IDisplay requestDisplay() {
        if (cache == null || !cache.url.equals(url)) {
            cache = TextureCache.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (!cache.isVideo() && (!cache.ready() || cache.getError() != null))
            return null;
        if (display != null)
            return display;
        return display = cache.createDisplay(null, url, volume, 0, 0, false);
    }

    public VideoScreen(String url, int volume) {
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getInventory() : null, Component.literal(""));
        this.url = url;
        this.volume = volume;

        display = requestDisplay();
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    private synchronized static void checkIfCurrentThreadHaveClassLoader() {
        if (Thread.currentThread().getContextClassLoader() == null) Thread.currentThread().setContextClassLoader(CL);
    }

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (url.isBlank()) {
            if (display != null) display.release();
            return;
        }

        IDisplay display = requestDisplay();
        if (display == null) return;

        if (!firstIteration) {
            firstIteration = true;
            ((VideoDisplayer) display).player.raw().mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
                @Override
                public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void opening(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void buffering(MediaPlayer mediaPlayer, float v) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                    ENDED.set(true);
                }

                @Override
                public void forward(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void backward(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long l) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void positionChanged(MediaPlayer mediaPlayer, float v) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void seekableChanged(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void pausableChanged(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void titleChanged(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void snapshotTaken(MediaPlayer mediaPlayer, String s) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void lengthChanged(MediaPlayer mediaPlayer, long l) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void videoOutput(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void scrambledChanged(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void corked(MediaPlayer mediaPlayer, boolean b) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void muted(MediaPlayer mediaPlayer, boolean b) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void volumeChanged(MediaPlayer mediaPlayer, float v) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void chapterChanged(MediaPlayer mediaPlayer, int i) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }

                @Override
                public void error(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                    ENDED.set(true);
                }

                @Override
                public void mediaPlayerReady(MediaPlayer mediaPlayer) {
                    
                    checkIfCurrentThreadHaveClassLoader();
                }
            });
        }

        int texture;
        if (display instanceof VideoDisplayer) {
            if (ENDED.get()) {
                onClose();
                return;
            }
            if (!((VideoDisplayer) display).player.isPlaying()) return;
            texture = createTexture(display.getWidth(), display.getHeight(), ((VideoDisplayer) display).buffer);
        } else {
            display.prepare(url, 200, 1, 1, true, false, tick);
            texture = display.texture();
        }

        if (texture == -1) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(pPoseStack, 0, 0, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
        RenderSystem.disableBlend();
    }

    public int createTexture(int width, int height, IntBuffer buffer) {
        // Generate a new texture object in memory and bind it
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }

    @Override
    protected void containerTick() {
        if (display != null)
            display.tick(url, volume, 1, 1, true, false, tick);
        tick++;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (hasShiftDown() && pKeyCode == 256) {
            this.onClose();
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().getSoundManager().resume();
        super.onClose();
        if (display != null)
            display.release();
    }
}


package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.lib720.caprica.vlcj4.player.base.MediaPlayer;
import me.lib720.caprica.vlcj4.player.base.MediaPlayerEventAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    private final String url;
    private final int volume;
    private int tick;

    private boolean firstIteration;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;

    @Override
    protected void init() {
        this.imageWidth = Minecraft.getInstance().screen.width;
        this.imageHeight = Minecraft.getInstance().screen.height;
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
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getInventory() : null, new TextComponent(""));
        this.url = url;
        this.volume = volume;

        display = requestDisplay();
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}

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
            if (display instanceof VideoDisplayer) {
                ((VideoDisplayer) display).player.getRawPlayer().mediaPlayer().events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                    @Override
                    public void finished(MediaPlayer mediaPlayer) {
                        long time = mediaPlayer.status().time();
                        if (time > 10) {
                            VideoPlayer.LOGGER.warn("Video finished");
                            onClose();
                        }
                    }
                });
            }
        }

        int texture;
        if (display instanceof VideoDisplayer) {
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
        GuiComponent.blit(pPoseStack, 0, 0, 0.0F, 0.0F, Minecraft.getInstance().screen.width, Minecraft.getInstance().screen.height, Minecraft.getInstance().screen.width, Minecraft.getInstance().screen.height);
        RenderSystem.disableBlend();
    }

    public int createTexture(int width, int height, IntBuffer buffer) {
        // Generate a new texture object in memory and bind it
        int textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

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
        if (display != null)
            display.release();
        super.onClose();
    }
}


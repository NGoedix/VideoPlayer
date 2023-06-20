package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;

public class VideoScreen extends Screen {

    private final String url;
    private final int volume;
    private int tick;

    private boolean firstIteration;


    @Environment(EnvType.CLIENT)
    public IDisplay display;

    @Environment(EnvType.CLIENT)
    public TextureCache cache;

    @Override
    protected void init() {
        this.width = MinecraftClient.getInstance().currentScreen.width;
        this.height = MinecraftClient.getInstance().currentScreen.height;
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
        super(Text.of(""));
        this.url = url;
        this.volume = volume;

        display = requestDisplay();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
        if (url.isBlank()) {
            if (display != null) display.release();
            return;
        }

        IDisplay display = requestDisplay();
        if (display == null) return;

        if (!firstIteration) {
            firstIteration = true;
            if (display instanceof VideoDisplayer) {
                ((VideoDisplayer) display).player.events.setMediaFinishEvent((videoLanPlayer, eventData) -> {
                    Constants.LOGGER.warn("Video finished");
                    close();
                });
            }
        }

        int texture;
        if (display instanceof VideoDisplayer) {
            if (!((VideoDisplayer) display).player.isPlaying())
                return;
            texture = createTexture(display.getWidth(), display.getHeight(), ((VideoDisplayer) display).buffer);
        } else {
            display.prepare(url, 200, 1, 1, true, false, tick);

            texture = display.texture();
        }

        if (texture == -1) return;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
    }

    private int createTexture(int width, int height, IntBuffer buffer) {
        // Generate a new texture object in memory and bind it
        int textureId = GL11.glGenTextures();

        GlStateManager._pixelStore(3314, 0);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);

        // Set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        // Upload the texture data
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Unbind the texture
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return textureId;
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hasShiftDown() && keyCode == 256) {
            this.close();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().getSoundManager().resumeAll();
        super.close();
        if (display != null)
            display.release();
    }
}

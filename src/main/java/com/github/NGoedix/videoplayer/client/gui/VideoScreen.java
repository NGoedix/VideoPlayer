package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.util.MemoryTracker;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.ByteBuf;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.math.MathAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class VideoScreen extends Screen {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    int tick = 0;
    int closingOnTick = -1;
    float fadeLevel = 0;
    boolean started;
    boolean closing = false;
    boolean paused = false;
    float volume;
    boolean controlBlocked;

    // TOOLS
    private final SyncVideoPlayer player;

    // VIDEO INFO
    int videoTexture = -1;

    @Override
    protected void init() {
        if (MinecraftClient.getInstance().currentScreen != null) {
            this.width = MinecraftClient.getInstance().currentScreen.width;
            this.height = MinecraftClient.getInstance().currentScreen.height;
        }
        super.init();
    }

    public VideoScreen(String url, int volume, boolean controlBlocked) {
        super(Text.of(""));

        MinecraftClient minecraft = MinecraftClient.getInstance();
        minecraft.getSoundManager().pauseAll();

        this.volume = volume;
        this.controlBlocked = controlBlocked;

        this.player = new SyncVideoPlayer(null, minecraft, MemoryTracker::create);
        Constants.LOGGER.info("Playing video (" + url + " with volume: " + (int) (minecraft.options.getSoundVolume(SoundCategory.MASTER) * volume));

        player.setVolume((int) (minecraft.options.getSoundVolume(SoundCategory.MASTER) * volume));
        player.start(url);
        started = true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!started) return;

        videoTexture = player.getGlTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (delta / 8), 0.0f);
                renderBlackBackground(context);
                renderIcon(context, ImageAPI.loadingGif());
                if (fadeLevel == 0) close();
                return;
            }
        }

        boolean playingState = (player.isPlaying() || player.isPaused()) && (player.getRawPlayerState().equals(State.PLAYING) || player.getRawPlayerState().equals(State.PAUSED));
        fadeLevel = (playingState) ? Math.max(fadeLevel - (delta / 8), 0.0f) : Math.min(fadeLevel + (delta / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(context, videoTexture);
        }

        // BLACK SCREEN
        if (!paused)
            renderBlackBackground(context);

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) {
            if (player.isPaused() && player.getRawPlayerState().equals(State.PAUSED)) {
                renderIcon(context, VideoPlayer.pausedImage());
            } else {
                renderIcon(context, ImageAPI.loadingGif());
            }
        }

        // DEBUG RENDERING
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            draw(context, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(context, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(context, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
    }

    private void draw(DrawContext context, String text, int height) {
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, text, 5, height, 0xffffff);
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void renderBlackBackground(DrawContext context) {
        RenderSystem.enableBlend();
        context.fill(0, 0, width, height, MathAPI.getColorARGB((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderIcon(DrawContext guiGraphics, ImageRenderer image) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, image.texture(tick, 1, true));

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Matrix4f matrix4f = guiGraphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float)width - 36, (float)height - 36, (float)0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float)width - 36, (float)height, (float)0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float)width, (float)height, (float)0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float)width, (float)height - 36, (float)0).texture(1, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    private void renderTexture(DrawContext guiGraphics, int texture) {
        if (player.getDimensions() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, width, height, MathAPI.getColorARGB(255, 0, 0, 0));
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);

        // Get video dimensions
        Dimension videoDimensions = player.getDimensions();
        double videoWidth = videoDimensions.getWidth();
        double videoHeight = videoDimensions.getHeight();

        // Calculate aspect ratios for both the screen and the video
        float screenAspectRatio = (float) width / height;
        float videoAspectRatio = (float) ((float) videoWidth / videoHeight);

        // New dimensions for rendering video texture
        int renderWidth, renderHeight;

        // If video's aspect ratio is greater than screen's, it means video's width needs to be scaled down to screen's width
        if(videoAspectRatio > screenAspectRatio) {
            renderWidth = width;
            renderHeight = (int) (width / videoAspectRatio);
        } else {
            renderWidth = (int) (height * videoAspectRatio);
            renderHeight = height;
        }

        int xOffset = (width - renderWidth) / 2; // xOffset for centering the video
        int yOffset = (height - renderHeight) / 2; // yOffset for centering the video

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Matrix4f matrix4f = guiGraphics.getMatrices().peek().getPositionMatrix();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        bufferBuilder.vertex(matrix4f, (float)xOffset, (float)yOffset, (float)0).texture(0, 0).next();
        bufferBuilder.vertex(matrix4f, (float)xOffset, (float)(yOffset + renderHeight), (float)0).texture(0, 1).next();
        bufferBuilder.vertex(matrix4f, (float)(xOffset + renderWidth), (float)(yOffset + renderHeight), (float)0).texture(1, 1).next();
        bufferBuilder.vertex(matrix4f, (float)(xOffset + renderWidth), (float)yOffset, (float)0).texture(1, 0).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
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
    public boolean keyPressed(int pKeyCode, int scanCode, int modifiers) {
        // Shift + ESC (Exit)
        if (hasShiftDown() && pKeyCode == 256) {
            this.close();
        }

        // Up arrow key (Volume)
        if (pKeyCode == 265) {
            if (volume <= 95) {
                volume += 5;
            } else {
                volume = 100;
                float masterVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
                if (masterVolume <= 0.95)
                    MinecraftClient.getInstance().options.getSoundVolumeOption(SoundCategory.MASTER).setValue(masterVolume + 0.05D);
                else
                    MinecraftClient.getInstance().options.getSoundVolumeOption(SoundCategory.MASTER).setValue(1D);
            }

            float actualVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            Constants.LOGGER.info("Volume UP to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // Down arrow key (Volume)
        if (pKeyCode == 264) {
            if (volume >= 5) {
                volume -= 5;
            } else {
                volume = 0;
            }
            float actualVolume = MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            Constants.LOGGER.info("Volume DOWN to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // M to mute
        if (pKeyCode == 77) {
            if (!player.raw().mediaPlayer().audio().isMute()) {
                player.mute();
            } else {
                player.unmute();
            }
        }

        // If control blocked can't modify the video time
        if (controlBlocked) return super.keyPressed(pKeyCode, scanCode, modifiers);

        // Shift + Right arrow key (Forwards)
        if (hasShiftDown() && pKeyCode == 262) {
            player.seekTo(player.getTime() + 30000);
        }

        // Shift + Left arrow key (Backwards)
        if (hasShiftDown() && pKeyCode == 263) {
            player.seekTo(player.getTime() - 10000);
        }

        // Shift + Space (Pause / Play)
        if (hasShiftDown() && pKeyCode == 32) {
            if (!player.isPaused()) {
                paused = true;
                player.pause();
            } else {
                paused = false;
                player.play();
            }
        }
        return super.keyPressed(pKeyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
        super.close();
        if (started) {
            started = false;
            player.stop();
            MinecraftClient.getInstance().getSoundManager().resumeAll();
            GlStateManager._deleteTexture(videoTexture);
            player.release();
        }
    }
}

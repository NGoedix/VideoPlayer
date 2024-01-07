package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.util.MemoryTracker;
import com.github.NGoedix.videoplayer.util.cache.TextureCache;
import com.github.NGoedix.videoplayer.util.displayers.IDisplay;
import com.github.NGoedix.videoplayer.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import org.lwjgl.opengl.GL11;

import java.awt.*;
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
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta){
        if (!started) return;

        videoTexture = player.prepareTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (delta / 8), 0.0f);
                renderBlackBackground(matrices);
                renderIcon(matrices, ImageAPI.loadingGif());
                if (fadeLevel == 0) close();
                return;
            }
        }

        boolean playingState = (player.isPlaying() || player.isPaused()) && (player.getRawPlayerState().equals(State.PLAYING) || player.getRawPlayerState().equals(State.PAUSED));
        fadeLevel = (playingState) ? Math.max(fadeLevel - (delta / 8), 0.0f) : Math.min(fadeLevel + (delta / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(matrices, videoTexture);
        }

        // BLACK SCREEN
        if (!paused)
            renderBlackBackground(matrices);

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) {
            if (player.isPaused() && player.getRawPlayerState().equals(State.PAUSED)) {
                renderIcon(matrices, VideoPlayer.pausedImage());
            } else {
                renderIcon(matrices, ImageAPI.loadingGif());
            }
        }

        // DEBUG RENDERING
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            draw(matrices, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(matrices, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(matrices, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderTexture(MatrixStack stack, int texture) {
        if (!started) return;
        if (player.getDimensions() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB(255, 0, 0, 0));
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
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
        DrawableHelper.drawTexture(stack, xOffset, yOffset, 0.0F, 0.0F, renderWidth, renderHeight, renderWidth, renderHeight);
        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
    }

    private void draw(MatrixStack stack, String text, int height) {
        drawStringWithShadow(stack, MinecraftClient.getInstance().textRenderer, text, 5, height, 0xffffff);
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void renderBlackBackground(MatrixStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderIcon(MatrixStack stack, ImageRenderer image) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, image.texture(tick, 1, true));

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        DrawableHelper.drawTexture(stack, width - 36, height - 36 , 0, 0, 36, 36, 28, 28);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
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
                    MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MASTER, masterVolume + 0.05F);
                else
                    MinecraftClient.getInstance().options.setSoundVolume(SoundCategory.MASTER, 1);
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

package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.client.ClientHandler;
import com.github.NGoedix.videoplayer.util.math.VideoMathUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.math.MathAPI;
import org.watermedia.api.player.videolan.VideoPlayer;

import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    private int tick = 0;
    private int closingOnTick = -1;
    private float fadeLevel = 0;
    private float fadeStep10 = 0;
    private float fadeStep5 = 0;
    private boolean started;
    private boolean closing = false;
    private float volume;

    // CONTROL
    private final boolean controlBlocked;
    private final boolean canSkip;
    private int optionInMode;
    private int optionInSecs;
    private int optionOutMode;
    private int optionOutSecs;

    // TOOLS
    private final VideoPlayer player;

    // VIDEO INFO
    int videoTexture = -1;

    public VideoScreen(String url, int volume, boolean controlBlocked, boolean canSkip, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        this(url, volume, controlBlocked, canSkip, optionInMode != -1 && optionInSecs > 0);
        this.optionInMode = optionInMode;
        this.optionInSecs = optionInSecs;
        this.optionOutMode = optionOutMode;
        this.optionOutSecs = optionOutSecs;
    }

    public VideoScreen(String url, int volume, boolean controlBlocked, boolean canSkip, boolean fadeIn) {
        super(new DummyContainer(), Objects.requireNonNull(Minecraft.getInstance().player).getInventory(), Component.literal(""));

        URI uri;
        if (url.startsWith("local://")) {
            uri = Path.of(url.replaceFirst("local://", "")).toAbsolutePath().toUri();
        } else {
            uri = URI.create(url);
        }

        Minecraft minecraft = Minecraft.getInstance();
        Minecraft.getInstance().getSoundManager().pause();

        this.volume = volume;
        this.controlBlocked = controlBlocked;
        this.canSkip = canSkip;
        this.optionInMode = -1;
        this.optionInSecs = -1;
        this.optionOutMode = -1;
        this.optionOutSecs = -1;

        this.player = new VideoPlayer(null, minecraft);
        Reference.LOGGER.info("Playing video (" + (!controlBlocked ? "not" : "") + "blocked) (" + url + " with volume: " + (int) (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) * volume));

        player.setVolume((int) (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) * volume));
        if (!fadeIn) {
            started = true;
            player.start(uri);
        } else {
            player.startPaused(uri);
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics pPoseguiGraphics, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        if (started && !closing) {
            videoTexture = player.preRender();
        }

        // Handle easing for fade-in
        if ((tick < optionInSecs * 20 && optionInMode != -1) || !started) {
            float t = tick / (float) (optionInSecs * 20);
            fadeLevel = (float) applyEasing(optionInMode, 0, 1, t);
            if (!started && fadeLevel >= 1.0) {
                player.play();
                started = true;
                fadeLevel = 0;
            }
        }

        // Handle easing for fade-out
        if (closing || player.isEnded() || player.isBroken()) {
            if (optionOutMode == -1) {
                System.out.println("Closing without fading out");
                onClose();
            }
            if (optionInMode != -1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + optionOutSecs * 20;
                float t = (tick - closingOnTick + optionOutSecs * 20) / (float)(optionOutSecs * 20);
                fadeLevel = (float) applyEasing(optionOutMode, 1, 0, t);
                renderBlackBackground(guiGraphics);
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        // BLACK SCREEN
        if (!player.isPaused() || optionInMode != -1 || optionOutMode != -1)
            renderBlackBackground(guiGraphics);

        if (!started) return;

        boolean playingState = (player.isPlaying() || player.isPaused());

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(guiGraphics, videoTexture);
        }

        // BLACK SCREEN
        if (!player.isPaused())
            renderBlackBackground(guiGraphics);

        // RENDER GIF
        if (!player.isPlaying() || !player.isPlaying()) {
            if (player.isPaused() && player.isPaused()) {
                renderIcon(guiGraphics, ClientHandler.pausedImage());
            } else {
                renderIcon(guiGraphics, ImageAPI.loadingGif());
            }
        }

        // Render icons 10 and -5 seconds
        renderStepIcon(guiGraphics, pPartialTick, true);
        renderStepIcon(guiGraphics, pPartialTick, false);

        // DEBUG RENDERING
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            draw(guiGraphics, String.format("State: %s", player.getStateName()), getHeightCenter(-12));
            draw(guiGraphics, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(guiGraphics, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderTexture(GuiGraphics guiGraphics, int texture) {
        if (player.dimension() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, width, height, MathAPI.argb(255, 0, 0, 0));
        RenderSystem.disableBlend();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        // Get video dimensions
        Dimension videoDimensions = player.dimension();
        double videoWidth = videoDimensions.getWidth();
        double videoHeight = videoDimensions.getHeight();

        // Calculate aspect ratios for both the screen and the video
        float screenAspectRatio = (float) imageWidth / imageHeight;
        float videoAspectRatio = (float) ((float) videoWidth / videoHeight);

        // New dimensions for rendering video texture
        int renderWidth, renderHeight;

        // If video's aspect ratio is greater than screen's, it means video's width needs to be scaled down to screen's width
        if(videoAspectRatio > screenAspectRatio) {
            renderWidth = imageWidth;
            renderHeight = (int) (imageWidth / videoAspectRatio);
        } else {
            renderWidth = (int) (imageHeight * videoAspectRatio);
            renderHeight = imageHeight;
        }

        int xOffset = (imageWidth - renderWidth) / 2; // xOffset for centering the video
        int yOffset = (imageHeight - renderHeight) / 2; // yOffset for centering the video

        drawTexture(guiGraphics, texture, xOffset, yOffset, renderWidth, renderHeight, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private void renderBlackBackground(GuiGraphics guiGraphics) {
        RenderSystem.enableBlend();
        guiGraphics.fill(0, 0, width, height, MathAPI.argb((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void renderIcon(GuiGraphics guiGraphics, ImageRenderer image) {
        int iconSize = 36;
        int xOffset = width - iconSize;
        int yOffset = height - iconSize;

        drawTexture(guiGraphics, image.texture(tick, 1, true), xOffset, yOffset, iconSize, iconSize, 0.0f, 0.0f, 1.0f, 1.0f);
    }

    private void renderStepIcon(GuiGraphics stack, float pPartialTicks, boolean forward) {
        float alpha = forward ? fadeStep10 : fadeStep5;
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        drawTexture(stack, forward ? ClientHandler.step10Image().texture(0) : ClientHandler.step5Image().texture(0), width / 2 + (forward ? 70 : -134), height / 2 - 32, 64, 64, 0.0f, 0.0f, 1.0f, 1.0f);
        if (forward) {
            fadeStep10 = Math.max(fadeStep10 - (pPartialTicks / 8), 0.0f);
        } else {
            fadeStep5 = Math.max(fadeStep5 - (pPartialTicks / 8), 0.0f);
        }
    }

    private void drawTexture(GuiGraphics guiGraphics, int texture, int x, int y, int width, int height, float uMin, float vMin, float uMax, float vMax) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.addVertex(matrix4f, x, y + height, 0).setUv(uMin, vMax);   // Bottom-left
        bufferBuilder.addVertex(matrix4f, x + width, y + height, 0).setUv(uMax, vMax);  // Bottom-right
        bufferBuilder.addVertex(matrix4f, x + width, y, 0).setUv(uMax, vMin);  // Top-right
        bufferBuilder.addVertex(matrix4f, x, y, 0).setUv(uMin, vMin);   // Top-left

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    private double applyEasing(int mode, double start, double end, double t) {
        return switch (mode) {
            case 0 -> VideoMathUtil.easeIn(start, end, t);
            case 1 -> VideoMathUtil.easeOut(start, end, t);
            default -> end;
        };
    }

    private void draw(GuiGraphics guiGraphics, String text, int height) {
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, text, 155, height, -1);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        // Shift + ESC (Exit)
        if (canSkip && hasShiftDown() && pKeyCode == 256) {
            this.onClose();
        }

        // Up arrow key (Volume)
        if (pKeyCode == 265) {
            if (volume <= 120) {
                volume += 5;
            } else {
                volume = 125;
                float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
                Minecraft.getInstance().options.getSoundSourceOptionInstance(SoundSource.MASTER).set((double) (masterVolume <= 0.95 ? masterVolume + 0.1F : 1.0F));
            }

            float actualVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            float newVolume = volume * actualVolume;
            Reference.LOGGER.info("Volume UP to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // Down arrow key (Volume)
        if (pKeyCode == 264) {
            if (volume >= 5) {
                volume -= 5;
            } else {
                volume = 0;
            }
            float actualVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            float newVolume = volume * actualVolume;
            Reference.LOGGER.info("Volume DOWN to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // M to mute
        if (pKeyCode == 77) {
            if (player.isMuted()) {
                player.unmute();
            } else {
                player.mute();
            }
        }

        // If control blocked can't modify the video time
        if (controlBlocked) return super.keyPressed(pKeyCode, pScanCode, pModifiers);

        // Shift + Right arrow key (Forwards)
        if (hasShiftDown() && pKeyCode == 262) {
            player.seekTo(player.getTime() + 10000);
            fadeStep10 = 1;
        }

        // Shift + Left arrow key (Backwards)
        if (hasShiftDown() && pKeyCode == 263) {
            player.seekTo(player.getTime() - 5000);
            fadeStep5 = 1;
        }

        // Shift + Space (Pause / Play)
        if (hasShiftDown() && pKeyCode == 32) {
            player.togglePlayback();
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (started) {
            started = false;
            player.stop();
            Minecraft.getInstance().getSoundManager().resume();
            GlStateManager._deleteTexture(videoTexture);
            player.release();
        }
    }

    @Override
    protected void init() {
        if (Minecraft.getInstance().screen != null) {
            this.imageWidth = Minecraft.getInstance().screen.width;
            this.imageHeight = Minecraft.getInstance().screen.height;
        }
        super.init();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tick++;
    }
}


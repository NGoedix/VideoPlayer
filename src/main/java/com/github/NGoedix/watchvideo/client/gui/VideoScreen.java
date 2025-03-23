package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.util.VideoRenderer;
import com.github.NGoedix.watchvideo.util.math.VideoDimensionInfo;
import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.math.MathAPI;

import java.awt.*;
import java.net.URI;
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
    private final org.watermedia.api.player.videolan.VideoPlayer player;

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
        super(new DummyContainer(), Objects.requireNonNull(Minecraft.getInstance().player).getInventory(), new TextComponent(""));

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().pause();

        this.volume = volume;
        this.controlBlocked = controlBlocked;
        this.canSkip = canSkip;
        this.optionInMode = -1;
        this.optionInSecs = -1;
        this.optionOutMode = -1;
        this.optionOutSecs = -1;

        this.player = new org.watermedia.api.player.videolan.VideoPlayer(null, minecraft);
        Reference.LOGGER.info("Playing video (" + (!controlBlocked ? "not" : "") + "blocked) (" + url + " with volume: " + (int) (minecraft.options.getSoundSourceVolume(SoundSource.MASTER) * volume));

        player.setVolume((int) (minecraft.options.getSoundSourceVolume(SoundSource.MASTER) * volume));
        if (!fadeIn && player.isSafeUse()) {
            started = true;
            player.start(URI.create(url));
        } else {
            player.startPaused(URI.create(url));
        }
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull PoseStack stack, float pPartialTicks, int pMouseX, int pMouseY) {
        if (started && !closing) {
            videoTexture = player.preRender();
        }

        // Handle easing for fade-in
        if ((tick < optionInSecs * 20 && optionInMode != -1) || !started) {
            float t = tick / (float) (optionInSecs * 20);
            fadeLevel = (float) VideoRenderer.applyEasing(optionInMode, 0, 1, t);
            if (!started && fadeLevel >= 1.0) {
                if (player.isSafeUse())
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
                fadeLevel = (float) VideoRenderer.applyEasing(optionOutMode, 1, 0, t);
                renderBlackBackground(stack);
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        // BLACK SCREEN
        if (!player.isPaused() || optionInMode != -1 || optionOutMode != -1)
            renderBlackBackground(stack);

        if (!started) return;

        boolean playingState = (player.isPlaying() || player.isPaused());

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(stack, videoTexture);
        }

        // BLACK SCREEN
        if (!player.isPaused())
            renderBlackBackground(stack);

        // RENDER GIF
        if (!player.isPlaying() || !player.isPlaying()) {
            if (player.isPaused() && player.isPaused()) {
                VideoRenderer.renderTexture(stack, VideoPlayer.pausedImage().texture(tick, 1, true), 1, 0, 0, width - 36, height - 36, 36, 36);
            } else {
                VideoRenderer.renderTexture(stack, ImageAPI.loadingGif().texture(tick, 1, true), 1, 0, 0,width - 36, height - 36, 36, 36);
            }
        }

        // Render icons 10 and -5 seconds
        renderStepIcon(stack, pPartialTicks, true);
        renderStepIcon(stack, pPartialTicks, false);

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            if (!player.isReady()) return;
            VideoRenderer.drawString(stack, String.format("State: %s", player.raw().mediaPlayer().media().info().state().toString()), VideoMathUtil.getHeightCenter(height, -12));
            VideoRenderer.drawString(stack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), VideoMathUtil.getHeightCenter(height, 0));
            VideoRenderer.drawString(stack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), VideoMathUtil.getHeightCenter(height, 12));
        }
    }

    private void renderTexture(PoseStack stack, int texture) {
        if (player.dimension() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, MathAPI.argb(255, 0, 0, 0));

        // Get video dimensions
        Dimension videoDimensions = player.dimension();
        VideoDimensionInfo info = VideoMathUtil.calculateAspectRatio(width, height, (int) videoDimensions.getWidth(), (int) videoDimensions.getHeight());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        VideoRenderer.renderTexture(stack, texture, 1, info.getOffsetX(), info.getOffsetY(), 0, 0, info.getWidth(), info.getHeight());
    }


    private void renderBlackBackground(PoseStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, MathAPI.argb((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderStepIcon(PoseStack stack, float pPartialTicks, boolean forward) {
        int texture = forward ? VideoPlayer.step10Image().texture(tick, 1, true) : VideoPlayer.step5Image().texture(tick, 1, true);
        float alpha = forward ? fadeStep10 : fadeStep5;
        VideoRenderer.renderTexture(stack, texture, alpha, width / 2 + (forward ? 70 : -134), height / 2 - 32, 0, 0, 64, 64);

        if (forward) {
            fadeStep10 = Math.max(fadeStep10 - (pPartialTicks / 8), 0.0f);
        } else {
            fadeStep5 = Math.max(fadeStep5 - (pPartialTicks / 8), 0.0f);
        }
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
                Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MASTER, masterVolume <= 0.95 ? masterVolume + 0.1F : 1.0F);
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
            player.release();
            Minecraft.getInstance().getSoundManager().resume();
        }
    }

    public boolean isFinished() {
        return !started;
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


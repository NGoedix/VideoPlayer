package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.VideoPlayerUtils;
import com.github.NGoedix.watchvideo.util.VideoRenderer;
import com.github.NGoedix.watchvideo.util.math.VideoDimensionInfo;
import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
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

public class VideoScreen extends Screen {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/background.png");

    public static final ResourceLocation PLAY_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/play_button.png");
    public static final ResourceLocation PLAY_HOVER_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/play_button_hover.png");

    public static final ResourceLocation PAUSE_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/pause_button.png");
    public static final ResourceLocation PAUSE_HOVER_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/pause_button_hover.png");

    public static final ResourceLocation STOP_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/stop_button.png");
    public static final ResourceLocation STOP_HOVER_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/stop_button_hover.png");

    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    private final int tick = 0;
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

    public VideoScreen(final String url, final int volume, final boolean controlBlocked, final boolean canSkip, final int optionInMode, final int optionInSecs, final int optionOutMode, final int optionOutSecs) {
        this(url, volume, controlBlocked, canSkip, optionInMode != -1 && optionInSecs > 0);
        this.optionInMode = optionInMode;
        this.optionInSecs = optionInSecs;
        this.optionOutMode = optionOutMode;
        this.optionOutSecs = optionOutSecs;
    }

    public VideoScreen(final String url, final int volume, final boolean controlBlocked, final boolean canSkip, final boolean fadeIn) {
        super(new TextComponent(""));

        final Minecraft minecraft = Minecraft.getInstance();
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

        this.player.setVolume((int) (minecraft.options.getSoundSourceVolume(SoundSource.MASTER) * volume));
        if (!fadeIn && this.player.isSafeUse()) {
            this.started = true;
            this.player.start(URI.create(url));
        } else {
            this.player.startPaused(URI.create(url));
        }
    }

    @Override
    public void render(final PoseStack stack, final int pMouseX, final int pMouseY, final float pPartialTick) {
        if (this.started && !this.closing) {
            this.videoTexture = this.player.preRender();
        }

        // Handle easing for fade-in
        if ((this.tick < this.optionInSecs * 20 && this.optionInMode != -1) || !this.started) {
            final float t = this.tick / (float) (this.optionInSecs * 20);
            this.fadeLevel = (float) VideoRenderer.applyEasing(this.optionInMode, 0, 1, t);
            if (!this.started && this.fadeLevel >= 1.0) {
                if (this.player.isSafeUse())
                    this.player.play();
                this.started = true;
                this.fadeLevel = 0;
            }
        }

        // Handle easing for fade-out
        if (this.closing || this.player.isEnded() || this.player.isBroken()) {
            if (this.optionOutMode == -1) {
                Reference.LOGGER.info("Closed video screen without fade-out");
                this.onClose();
            }

            if (this.optionInMode != -1 || this.closing) {
                this.closing = true;
                if (this.closingOnTick == -1) this.closingOnTick = this.tick + this.optionOutSecs * 20;
                final float t = (this.tick - this.closingOnTick + this.optionOutSecs * 20) / (float)(this.optionOutSecs * 20);
                this.fadeLevel = (float) VideoRenderer.applyEasing(this.optionOutMode, 1, 0, t);
                this.renderBlackBackground(stack);
                if (this.fadeLevel == 0) this.onClose();
                return;
            }
        }

        // BLACK SCREEN
        if (!this.player.isPaused() || this.optionInMode != -1 || this.optionOutMode != -1)
            this.renderBlackBackground(stack);

        if (!this.started) return;

        final boolean playingState = (this.player.isPlaying() || this.player.isPaused());

        // RENDER VIDEO
        if (playingState || this.player.isStopped() || this.player.isEnded()) {
            this.renderTexture(stack, this.videoTexture);
        }

        // BLACK SCREEN
        if (!this.player.isPaused())
            this.renderBlackBackground(stack);

        // RENDER GIF
        if (!this.player.isPlaying() || !this.player.isPlaying()) {
            if (this.player.isPaused() && this.player.isPaused()) {
                VideoRenderer.renderTexture(stack, VideoPlayer.pausedImage().texture(this.tick, 1, true), 1, 0, 0, this.width - 36, this.height - 36, 36, 36);
            } else {
                VideoRenderer.renderTexture(stack, ImageAPI.loadingGif().texture(this.tick, 1, true), 1, 0, 0, this.width - 36, this.height - 36, 36, 36);
            }
        }

        // Render icons 10 and -5 seconds
        this.renderStepIcon(stack, pPartialTick, true);
        this.renderStepIcon(stack, pPartialTick, false);

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            if (!this.player.isReady()) return;
            VideoRenderer.drawString(stack, String.format("State: %s", this.player.raw().mediaPlayer().media().info().state().toString()), VideoMathUtil.getHeightCenter(this.height, -12));
            VideoRenderer.drawString(stack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(this.player.getTime())), this.player.getTime(), FORMAT.format(new Date(this.player.getDuration())), this.player.getDuration()), VideoMathUtil.getHeightCenter(this.height, 0));
            VideoRenderer.drawString(stack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(this.player.getMediaInfoDuration())), this.player.getMediaInfoDuration()), VideoMathUtil.getHeightCenter(this.height, 12));
        }
    }

    private void renderTexture(final PoseStack stack, final int texture) {
        if (this.player.dimension() == null) return; // Checking if video available

        RenderSystem.enableBlend();
        fill(stack, 0, 0, this.width, this.height, MathAPI.argb(255, 0, 0, 0));

        // Get video dimensions
        final Dimension videoDimensions = this.player.dimension();
        final VideoDimensionInfo info = VideoMathUtil.calculateAspectRatio(this.width, this.height, (int) videoDimensions.getWidth(), (int) videoDimensions.getHeight());

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        VideoRenderer.renderTexture(stack, texture, 1, info.getOffsetX(), info.getOffsetY(), 0, 0, info.getWidth(), info.getHeight());
    }


    private void renderBlackBackground(final PoseStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, this.width, this.height, MathAPI.argb((int) (this.fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderStepIcon(final PoseStack stack, final float pPartialTicks, final boolean forward) {
        final int texture = forward ? VideoPlayer.step10Image().texture(this.tick, 1, true) : VideoPlayer.step5Image().texture(this.tick, 1, true);
        final float alpha = forward ? this.fadeStep10 : this.fadeStep5;
        VideoRenderer.renderTexture(stack, texture, alpha, this.width / 2 + (forward ? 70 : -134), this.height / 2 - 32, 0, 0, 64, 64);

        if (forward) {
            this.fadeStep10 = Math.max(this.fadeStep10 - (pPartialTicks / 8), 0.0f);
        } else {
            this.fadeStep5 = Math.max(this.fadeStep5 - (pPartialTicks / 8), 0.0f);
        }
    }

    @Override
    public boolean keyPressed(final int pKeyCode, final int pScanCode, final int pModifiers) {
        // Shift + ESC (Exit)
        if (this.canSkip && hasShiftDown() && pKeyCode == 256) {
            this.onClose();
        }

        // Up arrow key (Volume)
        if (pKeyCode == 265) {
            if (this.volume <= 120) {
                this.volume += 5;
            } else {
                this.volume = 125;
                final float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
                Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MASTER, masterVolume <= 0.95 ? masterVolume + 0.1F : 1.0F);
            }

            final float actualVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            final float newVolume = this.volume * actualVolume;
            Reference.LOGGER.info("Volume UP to: " + newVolume);
            this.player.setVolume((int) newVolume);
        }

        // Down arrow key (Volume)
        if (pKeyCode == 264) {
            if (this.volume >= 5) {
                this.volume -= 5;
            } else {
                this.volume = 0;
            }
            final float actualVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            final float newVolume = this.volume * actualVolume;
            Reference.LOGGER.info("Volume DOWN to: " + newVolume);
            this.player.setVolume((int) newVolume);
        }

        // M to mute
        if (pKeyCode == 77) {
            if (this.player.isMuted()) {
                this.player.unmute();
            } else {
                this.player.mute();
            }
        }

        // If control blocked can't modify the video time
        if (this.controlBlocked) return super.keyPressed(pKeyCode, pScanCode, pModifiers);

        // Shift + Right arrow key (Forwards)
        if (hasShiftDown() && pKeyCode == 262) {
            this.player.seekTo(this.player.getTime() + 10000);
            this.fadeStep10 = 1;
        }

        // Shift + Left arrow key (Backwards)
        if (hasShiftDown() && pKeyCode == 263) {
            this.player.seekTo(this.player.getTime() - 5000);
            this.fadeStep5 = 1;
        }

        // Shift + Space (Pause / Play)
        if (hasShiftDown() && pKeyCode == 32) {
            this.player.togglePlayback();
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
        if (this.started) {
            this.started = false;
            this.player.stop();
            this.player.release();
            Minecraft.getInstance().getSoundManager().resume();
        }
    }

    public boolean isFinished() {
        return !this.started;
    }

    @Override
    protected void init() {
        super.init();
    }
}


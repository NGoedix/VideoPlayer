package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;
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
        if (Minecraft.getInstance().screen != null) {
            this.imageWidth = Minecraft.getInstance().screen.width;
            this.imageHeight = Minecraft.getInstance().screen.height;
        }
        super.init();
    }

    public VideoScreen(String url, int volume, boolean controlBlocked) {
        super(new DummyContainer(), Objects.requireNonNull(Minecraft.getInstance().player).getInventory(), new TextComponent(""));

        Minecraft minecraft = Minecraft.getInstance();
        Minecraft.getInstance().getSoundManager().pause();

        this.volume = volume;
        this.controlBlocked = controlBlocked;

        this.player = new SyncVideoPlayer(null, minecraft, MemoryTracker::create);
        Reference.LOGGER.info("Playing video (" + url + " with volume: " + (int) (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) * volume));

        player.setVolume((int) (Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER) * volume));
        player.start(url);
        started = true;
    }

    @Override
    protected void renderLabels(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (!started) return;

        videoTexture = player.prepareTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (pPartialTick / 8), 0.0f);
                renderBlackBackground(pPoseStack);
                renderIcon(pPoseStack, ImageAPI.loadingGif());
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        boolean playingState = (player.isPlaying() || player.isPaused()) && (player.getRawPlayerState().equals(State.PLAYING) || player.getRawPlayerState().equals(State.PAUSED));
        fadeLevel = (playingState) ? Math.max(fadeLevel - (pPartialTick / 8), 0.0f) : Math.min(fadeLevel + (pPartialTick / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(pPoseStack, videoTexture);
        }

        // BLACK SCREEN
        if (!paused)
            renderBlackBackground(pPoseStack);

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) {
            if (player.isPaused() && player.getRawPlayerState().equals(State.PAUSED)) {
                renderIcon(pPoseStack, VideoPlayer.pausedImage());
            } else {
                renderIcon(pPoseStack, ImageAPI.loadingGif());
            }
        }

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            draw(pPoseStack, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(pPoseStack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(pPoseStack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderTexture(PoseStack stack, int texture) {
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

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GuiComponent.blit(stack, xOffset, yOffset, 0.0F, 0.0F, renderWidth, renderHeight, renderWidth, renderHeight);
        RenderSystem.disableBlend();
    }


    private void renderBlackBackground(PoseStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void renderIcon(PoseStack stack, ImageRenderer image) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, image.texture(tick, 1, true));

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(stack, width - 36, height - 36 , 0, 0, 36, 36, 28, 28);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    private void draw(PoseStack stack, String text, int height) {
        drawString(stack, Minecraft.getInstance().font, text, 5, height, 0xffffff);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        tick++;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        // Shift + ESC (Exit)
        if (hasShiftDown() && pKeyCode == 256) {
            this.onClose();
        }

        // Up arrow key (Volume)
        if (pKeyCode == 265) {
            if (volume <= 95) {
                volume += 5;
            } else {
                volume = 100;
                float masterVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
                if (masterVolume <= 0.95)
                    Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MASTER, masterVolume + 0.05F);
                else
                    Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MASTER, 1);
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
            if (!player.raw().mediaPlayer().audio().isMute()) {
                player.mute();
            } else {
                player.unmute();
            }
        }

        // If control blocked can't modify the video time
        if (controlBlocked) return super.keyPressed(pKeyCode, pScanCode, pModifiers);

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
}


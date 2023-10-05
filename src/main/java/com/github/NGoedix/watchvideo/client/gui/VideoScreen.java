package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.util.MemoryTracker;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Timer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

@SideOnly(Side.CLIENT)
public class VideoScreen extends GuiScreen {

    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // STATUS
    int tick = 0;
    Timer tickTimer;
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

    public VideoScreen(String url, int volume, boolean controlBlocked) {
        super();

        Minecraft minecraft = Minecraft.getMinecraft();
        minecraft.getSoundHandler().pauseSounds();

        this.volume = volume;
        this.controlBlocked = controlBlocked;

        this.player = new SyncVideoPlayer(null, Runnable::run, MemoryTracker::create);
        Reference.LOGGER.info("Playing video (" + (!controlBlocked ? "not" : "") + "blocked) (" + url + " with volume: " + (int) (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) * volume));

        player.setVolume((int) (Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER) * volume));
        player.start(url);

        tickTimer = new Timer(20);

        started = true;
    }

    @Override
    public void drawScreen(int pMouseX, int pMouseY, float pPartialTicks) {
        if (!started) return;

        videoTexture = player.prepareTexture();

        if (player.isEnded() || player.isStopped() || player.getRawPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (pPartialTicks / 8), 0.0f);
                renderBlackBackground();
                renderIcon(ImageAPI.loadingGif());
                if (fadeLevel == 0) onGuiClosed();
                return;
            }
        }

        boolean playingState = (player.isPlaying() || player.isPaused()) && (player.getRawPlayerState().equals(State.PLAYING) || player.getRawPlayerState().equals(State.PAUSED));
        fadeLevel = (playingState) ? Math.max(fadeLevel - (pPartialTicks / 8), 0.0f) : Math.min(fadeLevel + (pPartialTicks / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(videoTexture);
        }

        // BLACK SCREEN
        if (!paused)
            renderBlackBackground();

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) {
            if (player.isPaused() && player.getRawPlayerState().equals(State.PAUSED)) {
                renderIcon(VideoPlayer.pausedImage());
            } else {
                renderIcon(ImageAPI.loadingGif());
            }
        }

        // DEBUG RENDERING
//        if (!FMLInitializationEvent.isProduction()) {
//            draw(stack, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
//            draw(stack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
//            draw(stack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
//        }
    }

    private void renderBlackBackground() {
        GlStateManager.enableBlend();
        drawBackground((int) (fadeLevel * 255));
        GlStateManager.disableBlend();
    }

    public void drawBackground(int alpha) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        this.mc.getTextureManager().bindTexture(OPTIONS_BACKGROUND);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        bufferbuilder.pos(0.0D, this.height, 0.0D).tex(0.0D, (float) this.height / 32.0F).color(0, 0, 0, alpha).endVertex();
        bufferbuilder.pos(this.width, this.height, 0.0D).tex(((float) this.width / 32.0F), ((float) this.height / 32.0F)).color(0, 0, 0, alpha).endVertex();
        bufferbuilder.pos(this.width, 0.0D, 0.0D).tex(((float) this.width / 32.0F), 0.0D).color(0, 0, 0, alpha).endVertex();
        bufferbuilder.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(0, 0, 0, alpha).endVertex();

        tessellator.draw();
    }

    private void renderTexture(int texture) {
        if (player.getDimensions() == null) return; // Checking if video available

        GlStateManager.enableBlend();
        drawBackground(255);
        GlStateManager.disableBlend();

        GlStateManager.bindTexture(texture);

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

        GlStateManager.enableBlend();
        GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        drawScaledCustomSizeModalRect(xOffset, yOffset, 0, 0, renderWidth, renderHeight, renderWidth, renderHeight, renderWidth, renderHeight);

        GlStateManager.disableBlend();
    }

    private void renderIcon(ImageRenderer image) {
        tickTimer.updateTimer();
        tick += tickTimer.elapsedTicks;

        GlStateManager.enableBlend();
        GlStateManager.bindTexture(image.texture(tick, 1, true));
        drawScaledCustomSizeModalRect(width - 36, height - 36 , 0, 0, 36, 36, 28, 28, 36, 36);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.disableBlend();
    }

//    private int getHeightCenter(int offset) {
//        return (height / 2) + offset;
//    }
//
//    private void draw(String text, int height) {
//        drawString(Minecraft.getMinecraft().fontRenderer, text, 5, height, 0xffffff);
//    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        // Ignore ESC
        if (!isShiftKeyDown() && keyCode == 1) return;

        // Shift + ESC (Exit)
        if (isShiftKeyDown() && keyCode == 1) {
            this.onGuiClosed();
        }

        // Up arrow key (Volume)
        if (keyCode == 200) {
            if (volume <= 95) {
                volume += 5;
            } else {
                volume = 100;
                float masterVolume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
                if (masterVolume <= 0.95)
                    Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MASTER, masterVolume + 0.1F);
                else
                    Minecraft.getMinecraft().gameSettings.setSoundLevel(SoundCategory.MASTER, 1);
            }

            float actualVolume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            Reference.LOGGER.info("Volume UP to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // Down arrow key (Volume)
        if (keyCode == 208) {
            if (volume >= 5) {
                volume -= 5;
            } else {
                volume = 0;
            }
            float actualVolume = Minecraft.getMinecraft().gameSettings.getSoundLevel(SoundCategory.MASTER);
            float newVolume = volume * actualVolume;
            Reference.LOGGER.info("Volume DOWN to: " + newVolume);
            player.setVolume((int) newVolume);
        }

        // M to mute
        if (keyCode == 50) {
            if (!player.raw().mediaPlayer().audio().isMute()) {
                player.mute();
            } else {
                player.unmute();
            }
        }

        // If control blocked can't modify the video time
        if (controlBlocked) return;

        // Shift + Right arrow key (Forwards)
        if (isShiftKeyDown() && keyCode == 205) {
            player.seekTo(player.getTime() + 30000);
        }

        // Shift + Left arrow key (Backwards)
        if (isShiftKeyDown() && keyCode == 203) {
            player.seekTo(player.getTime() - 10000);
        }

        // Shift + Space (Pause / Play)
        if (isShiftKeyDown() && keyCode == 57) {
            if (!player.isPaused()) {
                paused = true;
                player.pause();
            } else {
                paused = false;
                player.play();
            }
        }

        super.keyTyped(typedChar, keyCode);
    }


    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        if (started) {
            started = false;
            player.stop();
            Minecraft.getMinecraft().getSoundHandler().resumeSounds();
            GlStateManager.deleteTexture(videoTexture);
            player.release();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        if (Minecraft.getMinecraft().currentScreen != null) {
            this.width = Minecraft.getMinecraft().currentScreen.width;
            this.height = Minecraft.getMinecraft().currentScreen.height;
            Reference.LOGGER.info(this.width + " " + this.height);
        }
    }
}

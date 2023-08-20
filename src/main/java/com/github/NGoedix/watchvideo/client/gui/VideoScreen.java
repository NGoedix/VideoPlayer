package com.github.NGoedix.watchvideo.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.lib720.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.MediaPlayer;
import me.srrapero720.watermedia.api.player.MediaPlayer.State;
import me.srrapero720.watermedia.api.player.VideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.loading.FMLLoader;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.NGoedix.watchvideo.WatchVideo.LOGGER;

public class VideoScreen extends Screen {
    private static final DateFormat FORMAT = new SimpleDateFormat("HH:mm:ss");
    static {
        FORMAT.setTimeZone(TimeZone.getTimeZone("GMT-00:00"));
    }

    // TOOLS
    ReentrantLock lock = new ReentrantLock();
    private final MediaPlayer player;

    // STATUS
    int tick = 0;
    int closingOnTick = -1;
    float fadeLevel = 0;
    boolean started;
    boolean closing = false;

    // VIDEO INFO
    IntBuffer videoBuffer;
    int videoWidth = 1;
    int videoHeight = 1;
    int videoTexture = -1;
    boolean firstFrame = false;
    boolean updateFrame = false;


    public VideoScreen(String url) {
        super(new StringTextComponent(""));
        Minecraft minecraft = Minecraft.getInstance();
        Minecraft.getInstance().getSoundManager().pause();

        player = new VideoPlayer(null, minecraft::execute, (mediaPlayer, byteBuffers, bufferFormat) -> {
            lock.lock();
            try {
                videoBuffer.put(byteBuffers[0].asIntBuffer());
                videoBuffer.rewind();
                updateFrame = true;
            } finally {
                lock.unlock();
            }
        }, (sourceWidth, sourceHeight) -> {
            lock.lock();
            try {
                videoWidth = sourceWidth;
                videoHeight = sourceHeight;
                firstFrame = true;
                updateFrame = true;
                videoBuffer = GLAllocation.createByteBuffer(sourceWidth * sourceHeight * 4).asIntBuffer();
            } finally {
                lock.unlock();
            }
            return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[]{sourceWidth * 4}, new int[]{sourceHeight});
        });
        player.setVolume(200);
        player.start(url);
        started = true;
    }

    @Override
    public void tick() {
        super.tick();
        tick++;
    }

    @Override
    public void render(MatrixStack stack, int pMouseX, int pMouseY, float pPartialTicks) {
        if (!started) return;

        if (player.isEnded() || player.isStopped() || player.getPlayerState().equals(State.ERROR)) {
            if (fadeLevel == 1 || closing) {
                closing = true;
                if (closingOnTick == -1) closingOnTick = tick + 20;
                if (tick >= closingOnTick) fadeLevel = Math.max(fadeLevel - (pPartialTicks / 8), 0.0f);
                renderBlackBackground(stack);
                renderLoadingGif(stack);
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        this.prepare();
        boolean playingState = player.isPlaying() && player.getPlayerState().equals(State.PLAYING);
        fadeLevel = (playingState) ? Math.max(fadeLevel - (pPartialTicks / 8), 0.0f) : Math.min(fadeLevel + (pPartialTicks / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(stack, videoTexture);
        }

        // BLACK SCREEN
        renderBlackBackground(stack);

        // RENDER GIF
        if (!player.isPlaying() || !player.getPlayerState().equals(State.PLAYING)) renderLoadingGif(stack);

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            draw(stack, String.format("State: %s", player.getPlayerState().name()), getHeightCenter(-12));
            draw(stack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(stack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderBlackBackground(MatrixStack stack) {
        RenderSystem.enableBlend();
        fill(stack, 0, 0, width, height, WaterMediaAPI.math_colorARGB((int) (fadeLevel * 255), 0, 0, 0));
        RenderSystem.disableBlend();
    }

    private void renderTexture(MatrixStack stack, int texture) {
        RenderSystem.enableBlend();
        RenderSystem.bindTexture(texture);
        RenderSystem.clearColor(1.0f, 1.0f, 1.0f, 1.0f);
        AbstractGui.blit(stack, 0, 0, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    private void renderLoadingGif(MatrixStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.bindTexture(WaterMediaAPI.render_getTexture(WaterMediaAPI.img_getLoading(), tick, 1, true));
        AbstractGui.blit(stack, width - 36, height - 36 , 0, 0, 36, 36, 28, 28);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    public void prepare() {
        lock.lock();
        try {
            if (videoTexture == -1) videoTexture = GlStateManager._genTexture();
            if (updateFrame) {
                GlStateManager._pixelStore(GL11.GL_UNPACK_ROW_LENGTH, GL11.GL_ZERO);
                GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, GL11.GL_ZERO);
                GlStateManager._pixelStore(GL11.GL_UNPACK_SKIP_ROWS, GL11.GL_ZERO);

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, videoTexture);
                if (firstFrame) {
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, videoWidth, videoHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, videoBuffer);
                    firstFrame = false;
                } else GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, videoWidth, videoHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, videoBuffer);
                updateFrame = false;
            }
        } finally {
            lock.unlock();
        }
    }

    private int getHeightCenter(int offset) {
        return (height / 2) + offset;
    }

    private void draw(MatrixStack stack, String text, int height) {
        drawString(stack, Minecraft.getInstance().font, text, 5, height, 0xffffff);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pKeyCode != 256) return false;
        this.player.stop();
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        if (started) {
            started = false;
            Minecraft.getInstance().getSoundManager().resume();
            GlStateManager._deleteTexture(videoTexture);
            player.releaseAsync();
            LOGGER.info("Releasing");
        }
    }
}


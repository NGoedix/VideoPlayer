package com.github.NGoedix.watchvideo.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.lib720.caprica.vlcj.player.base.State;
import me.srrapero720.watermedia.api.WaterMediaAPI;
import me.srrapero720.watermedia.api.player.SyncVideoPlayer;
import me.srrapero720.watermedia.api.player.VideoPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fml.loading.FMLLoader;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

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

    public VideoScreen(String url, int volume) {
        super(new DummyContainer(), Objects.requireNonNull(Minecraft.getInstance().player).getInventory(), new TextComponent(""));

        Minecraft minecraft = Minecraft.getInstance();
        Minecraft.getInstance().getSoundManager().pause();

        this.player = new SyncVideoPlayer(null, minecraft, MemoryTracker::create);
        player.setVolume(volume);
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
                renderLoadingGif(pPoseStack);
                if (fadeLevel == 0) onClose();
                return;
            }
        }

        boolean playingState = player.isPlaying() && player.getRawPlayerState().equals(State.PLAYING);
        fadeLevel = (playingState) ? Math.max(fadeLevel - (pPartialTick / 8), 0.0f) : Math.min(fadeLevel + (pPartialTick / 16), 1.0f);

        // RENDER VIDEO
        if (playingState || player.isStopped() || player.isEnded()) {
            renderTexture(pPoseStack, videoTexture);
        }

        // BLACK SCREEN
        renderBlackBackground(pPoseStack);

        // RENDER GIF
        if (!player.isPlaying() || !player.getRawPlayerState().equals(State.PLAYING)) renderLoadingGif(pPoseStack);

        // DEBUG RENDERING
        if (!FMLLoader.isProduction()) {
            draw(pPoseStack, String.format("State: %s", player.getRawPlayerState().name()), getHeightCenter(-12));
            draw(pPoseStack, String.format("Time: %s (%s) / %s (%s)", FORMAT.format(new Date(player.getTime())), player.getTime(), FORMAT.format(new Date(player.getDuration())), player.getDuration()), getHeightCenter(0));
            draw(pPoseStack, String.format("Media Duration: %s (%s)", FORMAT.format(new Date(player.getMediaInfoDuration())), player.getMediaInfoDuration()), getHeightCenter(12));
        }
    }

    private void renderTexture(PoseStack stack, int texture) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GuiComponent.blit(stack, 0, 0, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);
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

    private void renderLoadingGif(PoseStack stack) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WaterMediaAPI.api_getTexture(WaterMediaAPI.img_getLoading(), tick, 1, true));

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
        if (hasShiftDown() && pKeyCode == 256) {
            this.player.stop();
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
        super.onClose();
        if (started) {
            started = false;
            Minecraft.getInstance().getSoundManager().resume();
            GlStateManager._deleteTexture(videoTexture);
            player.release();
        }
    }
}


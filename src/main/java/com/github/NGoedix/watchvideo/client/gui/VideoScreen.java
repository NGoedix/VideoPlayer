package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.media.CustomMediaEventListener;
import com.github.NGoedix.watchvideo.media.CustomMediaPlayerEventListener;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import nick1st.fancyvideo.api.DynamicResourceLocation;
import nick1st.fancyvideo.api.MediaPlayerHandler;
import nick1st.fancyvideo.api.mediaPlayer.MediaPlayerBase;
import org.jetbrains.annotations.NotNull;
import uk.co.caprica.vlcj.player.base.MediaPlayer;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    boolean init = false;
    boolean stopped = true;
    MediaPlayerBase mediaPlayer;

    @Override
    protected void init() {
        this.imageWidth = Minecraft.getInstance().screen.width;
        this.imageHeight = Minecraft.getInstance().screen.height;
        super.init();
    }

    public VideoScreen(String url) {
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getInventory() : null, new TextComponent(""));

        if (MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).providesAPI()) {
            Minecraft.getInstance().getSoundManager().pause();
            MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().media().prepare(url);
            MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().events().addMediaEventListener(new CustomMediaEventListener());
            MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().events().addMediaPlayerEventListener(new CustomMediaPlayerEventListener() {
                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                    if (!stopped) onClose();
                }
            });
            MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().audio().setVolume(200);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        mediaPlayer = (MediaPlayerBase) MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation());
        if (MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).providesAPI()) {
            if (!init) {
                VideoPlayer.LOGGER.info("!init success");
                mediaPlayer.api().controls().play();
                init = true;
                stopped = false;
            }
            // Generic Render Code for Screens
            int width = Minecraft.getInstance().screen.width;
            int height = Minecraft.getInstance().screen.height;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, mediaPlayer.renderToResourceLocation());

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(pPoseStack, 0, 0, 0.0F, 0.0F, width, height, width, height);
        } else {
            // Generic Render Code for Screens
            int width = Minecraft.getInstance().screen.width;
            int height = Minecraft.getInstance().screen.height;

            int width2;

            if (width <= height) {
                width2 = width / 3;
            } else {
                width2 = height / 2;
            }

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            DynamicResourceLocation dr = new DynamicResourceLocation(Reference.MOD_ID, "fallback");

            RenderSystem.setShaderTexture(0, dr);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(pPoseStack, 0, 0, 0.0F, 0.0F, width, height, width2, width2);
        }
        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (hasShiftDown() && pKeyCode == 256) {
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
        if (!stopped) {
            stopped = true;
            Minecraft.getInstance().getSoundManager().resume();
            mediaPlayer.api().controls().stop();
            super.onClose();
        }
    }
}


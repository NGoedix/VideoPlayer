package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.WatchVideo;
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
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.base.TitleDescription;

import java.util.List;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    boolean init = false;
    MediaPlayerBase mediaPlayer;

    public VideoScreen(String url) {
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getInventory() : null, new TextComponent(""));
        if (MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation()).providesAPI()) {
            Minecraft.getInstance().getSoundManager().pause();
            MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation()).api().media().prepare(url);
            MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation()).api().events().addMediaPlayerEventListener(new MediaPlayerEventListener() {
                @Override
                public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {}
                @Override
                public void opening(MediaPlayer mediaPlayer) {}
                @Override
                public void buffering(MediaPlayer mediaPlayer, float v) {}
                @Override
                public void playing(MediaPlayer mediaPlayer) {}
                @Override
                public void paused(MediaPlayer mediaPlayer) {}
                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                    onClose();
                }
                @Override
                public void forward(MediaPlayer mediaPlayer) {}
                @Override
                public void backward(MediaPlayer mediaPlayer) {}
                @Override
                public void stopping(MediaPlayer mediaPlayer) {}
                @Override
                public void finished(MediaPlayer mediaPlayer) {}
                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long l) {}
                @Override
                public void positionChanged(MediaPlayer mediaPlayer, float v) {}
                @Override
                public void seekableChanged(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void pausableChanged(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void titleListChanged(MediaPlayer mediaPlayer) {}
                @Override
                public void titleSelectionChanged(MediaPlayer mediaPlayer, TitleDescription titleDescription, int i) {}
                @Override
                public void snapshotTaken(MediaPlayer mediaPlayer, String s) {}
                @Override
                public void lengthChanged(MediaPlayer mediaPlayer, long l) {}
                @Override
                public void videoOutput(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {}
                @Override
                public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {}
                @Override
                public void elementaryStreamUpdated(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {}
                @Override
                public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, String s, String s1) {}
                @Override
                public void corked(MediaPlayer mediaPlayer, boolean b) {}
                @Override
                public void muted(MediaPlayer mediaPlayer, boolean b) {}
                @Override
                public void volumeChanged(MediaPlayer mediaPlayer, float v) {}
                @Override
                public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {}
                @Override
                public void chapterChanged(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void programAdded(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void programDeleted(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void programUpdated(MediaPlayer mediaPlayer, int i) {}
                @Override
                public void programSelected(MediaPlayer mediaPlayer, int i, int i1) {}
                @Override
                public void error(MediaPlayer mediaPlayer) {}
                @Override
                public void mediaPlayerReady(MediaPlayer mediaPlayer) {}
            });
            MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation()).api().audio().setVolume(200);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        mediaPlayer = (MediaPlayerBase) MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation());
        if (MediaPlayerHandler.getInstance().getMediaPlayer(WatchVideo.getResourceLocation()).providesAPI()) {
            if (!init) {
                mediaPlayer.api().controls().play();
                init = true;
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
            RenderSystem.setShaderTexture(0, new DynamicResourceLocation(Reference.MOD_ID, "fallback"));

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
        super.onClose();
        Minecraft.getInstance().getSoundManager().resume();
        mediaPlayer.api().controls().stop();
    }
}

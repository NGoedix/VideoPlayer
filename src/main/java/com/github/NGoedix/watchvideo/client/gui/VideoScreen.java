package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.media.CustomMediaPlayerEventListener;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.IDisplay;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import me.lib720.caprica.vlcj.player.base.MediaPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

public class VideoScreen extends AbstractContainerScreen<AbstractContainerMenu> {

    boolean playing = false;
    boolean stopped = true;
    VideoDisplayer mediaPlayer;

    private String url;
    private int tick;

    @OnlyIn(Dist.CLIENT)
    public IDisplay display;

    @OnlyIn(Dist.CLIENT)
    public TextureCache cache;

    @Override
    protected void init() {
        this.imageWidth = Minecraft.getInstance().screen.width;
        this.imageHeight = Minecraft.getInstance().screen.height;
        super.init();
    }

    public IDisplay requestDisplay() {
        if (cache == null || !cache.url.equals(url)) {
            cache = TextureCache.get(url);
            if (display != null)
                display.release();
            display = null;
        }
        if (!cache.isVideo() && (!cache.ready() || cache.getError() != null))
            return null;
        if (display != null)
            return display;
        return display = cache.createDisplay(null, url, 200 / 100f, 0, 0, false);
    }

    public VideoScreen(String url) {
        super(new DummyContainer(), Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getInventory() : null, new TextComponent(""));
        this.url = url;

        mediaPlayer = new VideoDisplayer(null, url, 100, 0, 0, false);
        Minecraft.getInstance().getSoundManager().pause();
        mediaPlayer.player.getRawPlayer().mediaPlayer().events().addMediaPlayerEventListener(new CustomMediaPlayerEventListener() {
            @Override
            public void stopped(MediaPlayer mediaPlayer) {
            }
        });
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {}

    @Override
    protected void renderBg(@NotNull PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        if (!playing) {
            VideoPlayer.LOGGER.info("!init success");
            mediaPlayer.player.play();
            playing = true;
            stopped = false;
        }
        IDisplay display = requestDisplay();
        if (display == null) return;

        display.prepare(url, 200 * Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER), 0, 0, true, false, tick);

        // Generic Render Code for Screens
        int width = Minecraft.getInstance().screen.width;
        int height = Minecraft.getInstance().screen.height;

        int texture = mediaPlayer.texture;
        if (texture == -1) return;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, mediaPlayer.texture());

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(pPoseStack, 0, 0, 0.0F, 0.0F, width, height, width, height);
        RenderSystem.disableBlend();
    }

    @Override
    protected void containerTick() {
        if (display != null)
            display.tick(url, 200, 1, 1, true, false, tick);
        if (playing)
            tick++;
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
            mediaPlayer.player.stop();
            super.onClose();
        }
    }
}


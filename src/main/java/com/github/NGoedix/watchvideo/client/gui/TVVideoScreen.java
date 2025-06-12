package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.control.*;
import com.github.NGoedix.watchvideo.network.packets.gui.ClosedScreenPacket;
import com.github.NGoedix.watchvideo.util.VideoRenderer;
import com.github.NGoedix.watchvideo.util.config.TVConfig;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import com.github.NGoedix.watchvideo.util.math.VideoDimensionInfo;
import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.math.MathAPI;

import java.awt.*;

import static com.github.NGoedix.watchvideo.client.gui.VideoScreen.*;

public class TVVideoScreen extends Screen {

    private ImageButtonHoverable playButton;
    private ImageButtonHoverable pauseButton;
    private ImageButtonHoverable stopButton;

    private CustomSlider timeSlider;

    private final TVBlockEntity be;
    private String url;
    private int volume;
    private long maxDuration;

    private final int videoWidth = 200;
    private final int videoHeight = 150;

    // GUI
    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    // Components useful for the GUI
    private EditBox urlBox;
    private CustomSlider volumeSlider;

    public TVVideoScreen(final BlockEntity be) {
        super(new TranslatableComponent("gui.tv_video_screen.title"));
        this.be = (TVBlockEntity) be;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        this.addRenderableWidget(this.urlBox = new EditBox(this.font, this.leftPos + 10, this.topPos + 165, this.imageWidth - 26, 20, new TextComponent("")));
        // Set the text to the url
        this.urlBox.setMaxLength(32767);
        this.urlBox.setValue(this.url == null ? "" : this.url);
        this.urlBox.setSuggestion(this.url == null || this.url.isEmpty() ? "https://youtube.com/watch?v=FUIcBBM5-xQ" : "");
        this.urlBox.setResponder(s -> {
            if (s != null && !s.isEmpty()) {
                this.urlBox.setSuggestion("");
                if (s.matches(TVConfig.URL_PATTERN) && (this.be.getTick() > 5 || this.url.isEmpty())) {
                    if (!this.url.equals(s)) {
                        this.url = s;
                        PacketHandler.sendToServer(new UrlPacket(this.be.getBlockPos(), this.url));

                        this.maxDuration = 0;
                        this.timeSlider.setValue(0);
                    }
                }
            } else {
                this.urlBox.setSuggestion("https://youtube.com/watch?v=FUIcBBM5-xQ");
            }
        });

        // Play button
        this.addRenderableWidget(this.playButton = new ImageButtonHoverable(this.leftPos + 10, this.topPos + 190, 20, 20, 0, 0, 0, PLAY_BUTTON_TEXTURE, PLAY_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (this.be.requestDisplay() != null && !this.url.isEmpty()) {
                this.playButton.visible = false;
                this.pauseButton.visible = true;

                PacketHandler.sendToServer(new PausePacket(this.be.getBlockPos(), false));
            }
        }));

        // Pause button
        this.addRenderableWidget(this.pauseButton = new ImageButtonHoverable(this.leftPos + 10, this.topPos + 190, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (this.be.requestDisplay() != null && !this.url.isEmpty()) {
                this.playButton.visible = true;
                this.pauseButton.visible = false;

                PacketHandler.sendToServer(new PausePacket(this.be.getBlockPos(), true));
            }
        }));

        this.playButton.visible = !this.be.isPlaying();
        this.pauseButton.visible = this.be.isPlaying();

        // Stop button
        this.addRenderableWidget(this.stopButton = new ImageButtonHoverable(this.leftPos + 32, this.topPos + 190, 20, 20, 0, 0, 0, STOP_BUTTON_TEXTURE, STOP_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (this.be.requestDisplay() != null && !this.url.isEmpty()) {
                this.playButton.visible = true;
                this.pauseButton.visible = false;

                this.timeSlider.setValue(0);

                PacketHandler.sendToServer(new StopPacket(this.be.getBlockPos()));
            }
        }));

        // Time slider
        this.addRenderableWidget(this.timeSlider = new CustomSlider(this.leftPos + 54, this.topPos + 200, 187, 10, null, 0 / 100f, true));
        this.timeSlider.setOnSlideListener(value -> {
            if (this.be.requestDisplay() == null) return;

            final long time = (long) ((value / 100D) * this.be.requestDisplay().getDuration());
            if (this.maxDuration != 0)
                PacketHandler.sendToServer(new TickPacket(this.be.getBlockPos(), MathAPI.msToTick(time)));
        });
        if (this.be.requestDisplay() != null) this.timeSlider.setValue((double) this.be.requestDisplay().getTime() / this.be.requestDisplay().getDuration());

        // Volume slider
        this.addRenderableWidget(this.volumeSlider = new CustomSlider(this.leftPos + 10, this.topPos + 215, this.imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.volume"), this.volume / 100f, false));
        this.volumeSlider.setOnSlideListener(value -> {
            this.be.setVolume((int) value);
            this.volume = (int) this.volumeSlider.getValue();

            PacketHandler.sendToServer(new VolumePacket(this.be.getBlockPos(), this.volume));
        });
        this.volumeSlider.setValue(this.volume / 100f);

        this.be.setVolume(this.volume);
    }

    @Override
    public void render(@NotNull final PoseStack pPoseStack, final int pMouseX, final int pMouseY, final float pPartialTick) {
        final Display display = this.be.requestDisplay();

        this.renderBackground(pPoseStack);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(pPoseStack, this.leftPos, this.topPos, 320, 320, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        // Draw black square
        GlStateManager._bindTexture(ImageAPI.blackPicture().texture(0));

        RenderSystem.setShaderTexture(0, ImageAPI.blackPicture().texture(0));
        blit(pPoseStack, this.leftPos + (this.imageWidth / 2) - (this.videoWidth / 2), this.topPos + 10, this.videoWidth, this.videoHeight, 0, 0, this.videoWidth, this.videoHeight, this.videoWidth, this.videoHeight);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        String maxTimeFormatted = "00:00";
        String actualTimeFormatted = "00:00";

        // Time slider if not live
        if (display != null && display.isReady()) {
            this.timeSlider.setActive(!display.isLive());

            if (this.maxDuration <= 0 && !display.isLive())
                this.maxDuration = display.getDuration();

            // If not live, calculate the time
            if (!display.isLive()) {
                final long durationSeconds = this.maxDuration / 1000;
                final long maxMinute = durationSeconds / 60;
                final long maxSeconds = durationSeconds % 60;
                long actualTime = MathAPI.tickToMs(this.be.getTick()) / 1000;

                // Check if actualTime exceeds maxDuration and reset to 0 if it does
                if (this.maxDuration > 0 && actualTime > durationSeconds) {
                    actualTime = actualTime % durationSeconds;
                }

                final long actualMinute = actualTime / 60;
                long actualSeconds = actualTime % 60;

                if (durationSeconds != 0)
                    this.timeSlider.setValue((double) actualTime / durationSeconds);

                maxTimeFormatted = String.format("%02d:%02d", maxMinute, maxSeconds);
                if (actualSeconds == -1) actualSeconds = 0;
                actualTimeFormatted = String.format("%02d:%02d", actualMinute, actualSeconds);
            }
        }

        this.font.draw(pPoseStack, new TranslatableComponent("gui.tv_video_screen.time", actualTimeFormatted, maxTimeFormatted), this.leftPos + 54, this.topPos + 190, 0xFFFFFF);

        this.renderVideo(pPoseStack);
    }

    public void renderVideo(final PoseStack pPoseStack) {
        if (this.url.isEmpty()) return;

        final Display display = this.be.requestDisplay();
        if (display == null) {
            RenderSystem.enableBlend();
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            VideoRenderer.renderTexture(pPoseStack, ImageAPI.loadingGif().texture(this.be.getTick(), 1, true), 1, 0, 0, this.width - 36, this.height - 36, 36, 36);
            RenderSystem.disableBlend();
            return;
        }

        // RENDER VIDEO
        if (display.isPlaying() || display.isStopped()) {
            if (display.getDimensions() == null) return; // Checking if video available

            RenderSystem.enableBlend();
            final int localLeftPos = this.leftPos + (this.imageWidth / 2) - (this.videoWidth / 2);
            final int localTopPos = this.topPos + 10;
            fill(pPoseStack, localLeftPos, localTopPos, this.leftPos + (this.imageWidth / 2) - (this.videoWidth / 2) + this.videoWidth, this.topPos + 10 + this.videoHeight, MathAPI.argb(255, 0, 0, 0));

            // Get dimension and get aspect ratio details
            final Dimension videoDimensions = display.getDimensions();
            final VideoDimensionInfo info = VideoMathUtil.calculateAspectRatio(this.videoWidth, this.videoHeight, (int) videoDimensions.getWidth(), (int) videoDimensions.getHeight());

            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            VideoRenderer.renderTexture(pPoseStack, this.be.requestDisplay().renderTexture(), 1, info.getOffsetX() + localLeftPos, info.getOffsetY() + localTopPos, 0, 0, info.getWidth(), info.getHeight());
            RenderSystem.disableBlend();
        }
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(new ClosedScreenPacket(this.be.getBlockPos()));
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

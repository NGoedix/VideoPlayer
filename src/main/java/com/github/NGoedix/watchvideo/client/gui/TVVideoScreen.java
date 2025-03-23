package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.UploadVideoUpdateMessage;
import com.github.NGoedix.watchvideo.util.VideoRenderer;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import com.github.NGoedix.watchvideo.util.math.VideoDimensionInfo;
import com.github.NGoedix.watchvideo.util.math.VideoMathUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.math.MathAPI;

import java.awt.*;

public class TVVideoScreen extends Screen {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/background.png");

    private static final ResourceLocation PLAY_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/play_button.png");
    private static final ResourceLocation PLAY_HOVER_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/play_button_hover.png");

    private static final ResourceLocation PAUSE_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/pause_button.png");
    private static final ResourceLocation PAUSE_HOVER_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/pause_button_hover.png");

    private static final ResourceLocation STOP_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/stop_button.png");
    private static final ResourceLocation STOP_HOVER_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/stop_button_hover.png");

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

    public TVVideoScreen(BlockEntity be) {
        super(new TranslatableComponent("gui.tv_video_screen.title"));
        this.be = (TVBlockEntity) be;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        String urlPattern = "(http|https)://(www\\.)?([\\w]+\\.)+[\\w]{2,63}/?[\\w\\-\\?\\=\\&\\%\\.\\/]*/?";

        addRenderableWidget(urlBox = new EditBox(font, leftPos + 10, topPos + 165, imageWidth - 26, 20, new TextComponent("")));
        // Set the text to the url
        urlBox.setMaxLength(32767);
        urlBox.setValue(url == null ? "" : url);
        urlBox.setSuggestion(url == null || url.isEmpty() ? "https://youtube.com/watch?v=FUIcBBM5-xQ" : "");
        urlBox.setResponder(s -> {
            if (s != null && !s.isEmpty()) {
                urlBox.setSuggestion("");
                if (s.matches(urlPattern) && (be.getTick() > 5 || url.isEmpty())) {
                    if (!url.equals(s)) {
                        be.setTick(0);
                        url = s;
                        PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, 0, true, false, false));
                        playButton.visible = false;
                        pauseButton.visible = true;
                        maxDuration = 0;
                        timeSlider.setValue(0);

                        if (be.requestDisplay() == null) return;
                        be.requestDisplay().stop();
                        be.requestDisplay().resume(0);
                    }
                }
            } else {
                urlBox.setSuggestion("https://youtube.com/watch?v=FUIcBBM5-xQ");
            }
        });

        // Play button
        addRenderableWidget(playButton = new ImageButtonHoverable(leftPos + 10, topPos + 190, 20, 20, 0, 0, 0, PLAY_BUTTON_TEXTURE, PLAY_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (be.requestDisplay() != null && !url.isEmpty()) {
                playButton.visible = false;
                pauseButton.visible = true;

                be.requestDisplay().resume(be.getTick());
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, be.getTick(), true, false, false));
            }
        }));

        // Pause button
        addRenderableWidget(pauseButton = new ImageButtonHoverable(leftPos + 10, topPos + 190, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (be.requestDisplay() != null && !url.isEmpty()) {
                playButton.visible = true;
                pauseButton.visible = false;

                be.requestDisplay().pause(be.getTick());
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, be.getTick(), false, false, false));
            }
        }));

        playButton.visible = !be.isPlaying();
        pauseButton.visible = be.isPlaying();

        // Stop button
        addRenderableWidget(stopButton = new ImageButtonHoverable(leftPos + 32, topPos + 190, 20, 20, 0, 0, 0, STOP_BUTTON_TEXTURE, STOP_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (be.requestDisplay() != null && !url.isEmpty()) {
                playButton.visible = true;
                pauseButton.visible = false;

                timeSlider.setValue(0);

                be.requestDisplay().stop();
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, 0, false, true, false));
            }
        }));

        // Time slider
        addRenderableWidget(timeSlider = new CustomSlider(leftPos + 54, topPos + 200, 187, 10, null, 0 / 100f, true));
        timeSlider.setOnSlideListener(value -> {
            if (be.requestDisplay() == null) return;

            long time = (long) ((value / 100D) * be.requestDisplay().getDuration());
            be.requestDisplay().seekTo(time);
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, MathAPI.msToTick(time), pauseButton.visible, false, false));
        });

        if (be.requestDisplay() != null) {
            timeSlider.setValue((double) be.requestDisplay().getTime() / be.requestDisplay().getDuration());
        }

        // Volume slider
        addRenderableWidget(volumeSlider = new CustomSlider(leftPos + 10, topPos + 215, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.volume"), volume / 100f, false));
        volumeSlider.setOnSlideListener(value -> {
            be.setVolume((int) value);
            volume = (int) volumeSlider.getValue();

            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, -1, pauseButton.visible, false, false));
        });
        volumeSlider.setValue(volume / 100f);

        be.setVolume(volume);
    }

    @Override
    public void render(@NotNull PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Display display = be.requestDisplay();

        renderBackground(pPoseStack);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(pPoseStack, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Draw black square
        GlStateManager._bindTexture(ImageAPI.blackPicture().texture(0));

        RenderSystem.setShaderTexture(0, ImageAPI.blackPicture().texture(0));
        blit(pPoseStack, leftPos + (imageWidth / 2) - (videoWidth / 2), topPos + 10, videoWidth, videoHeight, 0, 0, videoWidth, videoHeight, videoWidth, videoHeight);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        String maxTimeFormatted = "00:00";
        String actualTimeFormatted = "00:00";

        // Time slider if not live
        if (display != null && display.isReady()) {
            timeSlider.setActive(!display.isLive());

            if (maxDuration == 0 && !display.isLive())
                maxDuration = display.getDuration();

            // If not live, calculate the time
            if (!display.isLive()) {
                long durationSeconds = maxDuration / 1000;
                long maxMinute = durationSeconds / 60;
                long maxSeconds = durationSeconds % 60;

                long actualTime = MathAPI.tickToMs(be.getTick()) / 1000;
                long actualMinute = actualTime / 60;
                long actualSeconds = actualTime % 60;

                if (durationSeconds != 0)
                    timeSlider.setValue((double) actualTime / durationSeconds);

                maxTimeFormatted = String.format("%02d:%02d", maxMinute, maxSeconds);
                if (actualSeconds == -1) actualSeconds = 0;
                actualTimeFormatted = String.format("%02d:%02d", actualMinute, actualSeconds);
            }
        }

        font.draw(pPoseStack, new TranslatableComponent("gui.tv_video_screen.time", actualTimeFormatted, maxTimeFormatted), leftPos + 54, topPos + 190, 0xFFFFFF);

        renderVideo(pPoseStack);
    }

    public void renderVideo(PoseStack pPoseStack) {
        if (url.isEmpty()) return;

        Display display = be.requestDisplay();
        if (display == null) {
            VideoRenderer.renderTexture(pPoseStack, ImageAPI.loadingGif().texture(be.getTick(), 1, true), 1, 0, 0,width - 36, height - 36, 36, 36);
            return;
        }

        // RENDER VIDEO
        if (display.isPlaying() || display.isStopped()) {
            if (display.getDimensions() == null) return; // Checking if video available

            RenderSystem.enableBlend();
            fill(pPoseStack, leftPos + (imageWidth / 2) - (videoWidth / 2), topPos + 10, leftPos + (imageWidth / 2) - (videoWidth / 2) + videoWidth, topPos + 10 + videoHeight, MathAPI.argb(255, 0, 0, 0));

            // Get dimension and get aspect ratio details
            Dimension videoDimensions = display.getDimensions();
            VideoDimensionInfo info = VideoMathUtil.calculateAspectRatio(videoWidth, videoHeight, (int) videoDimensions.getWidth(), (int) videoDimensions.getHeight());

            RenderSystem.enableBlend();
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GuiComponent.blit(pPoseStack, leftPos + (imageWidth / 2) - (videoWidth / 2) + info.getOffsetX(), topPos + 10 + info.getOffsetY(), 0.0F, 0.0F, info.getWidth(), info.getHeight(), info.getWidth(), info.getHeight());
            RenderSystem.disableBlend();
        }
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, -1, pauseButton.visible, false, true));
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.UploadVideoUpdateMessage;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.math.MathAPI;
import org.watermedia.api.player.videolan.VideoPlayer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

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

    private boolean canClick = true;

    public TVVideoScreen(BlockEntity be) {
        super(Component.translatable("gui.tv_video_screen.title"));
        this.be = (TVBlockEntity) be;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        String urlPattern = "(http|https)://(www\\.)?([\\w]+\\.)+[\\w]{2,63}/?[\\w\\-\\?\\=\\&\\%\\.\\/]*/?";

        addRenderableWidget(urlBox = new EditBox(font, leftPos + 10, topPos + 165, imageWidth - 26, 20, Component.literal("")));
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
            if (be.requestDisplay() != null && !url.isEmpty() && canClick) {
                canClick = false;
                playButton.visible = false;
                pauseButton.visible = true;

                if (be.requestDisplay() == null) return;
                be.requestDisplay().resume(be.getTick());
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, be.getTick(), true, false, false));
            }
        }));

        // Pause button
        addRenderableWidget(pauseButton = new ImageButtonHoverable(leftPos + 10, topPos + 190, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (be.requestDisplay() != null && !url.isEmpty() && canClick) {
                canClick = false;
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
                if (be.requestDisplay() == null) return;
                be.requestDisplay().stop();
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, 0, false, true, false));
            }
        }));

        // Time slider
        addRenderableWidget(timeSlider = new CustomSlider(leftPos + 54, topPos + 200, 187, 10, null, 0 / 100f, true));
        timeSlider.setOnSlideListener(value -> {
            if (be.requestDisplay() == null) return;
            if (be.requestDisplay() instanceof VideoDisplayer) {
                VideoPlayer player = (VideoPlayer) ((VideoDisplayer) be.requestDisplay()).player;
                if (player.isReady() && !player.isLive()) {
                    player.seekTo((int) ((value / 100D) * player.getDuration()));
                }
                PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, MathAPI.msToTick(player.getTime()), pauseButton.visible, false, false));
            }
        });

        if (be.requestDisplay() != null && be.requestDisplay() instanceof VideoDisplayer) {
            VideoPlayer player = (VideoPlayer) ((VideoDisplayer) be.requestDisplay()).player;
            timeSlider.setValue((double) player.getTime() / player.getDuration());
        }

        // Volume slider
        addRenderableWidget(volumeSlider = new CustomSlider(leftPos + 10, topPos + 215, imageWidth - 24, 20, Component.translatable("gui.tv_video_screen.volume"), volume / 100f, false));
        volumeSlider.setOnSlideListener(value -> {
            be.setVolume((int) value);
            volume = (int) volumeSlider.getValue();
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, -1, pauseButton.visible, false, false));
        });
        volumeSlider.setValue(volume / 100f);

        be.setVolume(volume);
    }

    @Override
    public void render(GuiGraphics context, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(context);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        context.blit(TEXTURE, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        // Draw black square
        GlStateManager._bindTexture(ImageAPI.blackPicture().texture(0));

        RenderSystem.setShaderTexture(0, ImageAPI.blackPicture().texture(0));
        drawTexture(context, ImageAPI.blackPicture().texture(0), leftPos + (imageWidth / 2) - (videoWidth / 2), topPos + 10, videoWidth, videoHeight, 0, 0, videoWidth, videoHeight);

        super.render(context, pMouseX, pMouseY, pPartialTick);

        String maxTimeFormatted = "00:00";
        String actualTimeFormatted = "00:00";

        if (be.requestDisplay() instanceof VideoDisplayer) {
            VideoPlayer player = (VideoPlayer) ((VideoDisplayer) be.requestDisplay()).player;

            if (player != null && player.isReady()) {
                timeSlider.setActive(!player.isLive());

                if (maxDuration == 0 && !player.isLive())
                    maxDuration = player.getDuration();
            }

            if (player != null && player.isReady() && !player.isLive()) {
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

        context.drawString(font, Component.translatable("gui.tv_video_screen.time", actualTimeFormatted, maxTimeFormatted), leftPos + 54, topPos + 190, 0xFFFFFF);

        renderVideo(context);

        canClick = true;
    }

    public void renderVideo(GuiGraphics guiGraphics) {
        if (url.isEmpty()) return;

        if (be.requestDisplay() == null) {
            renderIcon(guiGraphics, ImageAPI.loadingGif());
            return;
        }

        boolean playingState = be.requestDisplay().isPlaying();

        // RENDER VIDEO
        if (playingState || be.requestDisplay().isStopped()) {
            if (be.requestDisplay().getDimensions() == null) return; // Checking if video available

            int textureId = be.requestDisplay().getRenderTexture();

            if (textureId == -1) return;

            RenderSystem.enableBlend();
            guiGraphics.fill(leftPos + (imageWidth / 2) - (videoWidth / 2), topPos + 10, leftPos + (imageWidth / 2) - (videoWidth / 2) + videoWidth, topPos + 10 + videoHeight, MathAPI.argb(255, 0, 0, 0));
            RenderSystem.disableBlend();

            RenderSystem.bindTexture(textureId);
            RenderSystem.setShaderTexture(0, textureId);

            // Get video dimensions
            Dimension videoDimensions = be.requestDisplay().getDimensions();
            double nativeVideoWidth = videoDimensions.getWidth();
            double nativeVideoHeight = videoDimensions.getHeight();

            // Calculate aspect ratios for both the screen and the video
            float screenAspectRatio = (float) videoWidth / videoHeight;
            float videoAspectRatio = (float) ((float) nativeVideoWidth / nativeVideoHeight);

            // New dimensions for rendering video texture
            int renderWidth, renderHeight;

            // If video's aspect ratio is greater than screen's, it means video's width needs to be scaled down to screen's width
            if(videoAspectRatio > screenAspectRatio) {
                renderWidth = videoWidth;
                renderHeight = (int) (videoWidth / videoAspectRatio);
            } else {
                renderWidth = (int) (videoHeight * videoAspectRatio);
                renderHeight = videoHeight;
            }

            int xOffset = (videoWidth - renderWidth) / 2; // xOffset for centering the video
            int yOffset = (videoHeight - renderHeight) / 2; // yOffset for centering the video

            RenderSystem.enableBlend();
            RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            drawTexture(guiGraphics, textureId, leftPos + (imageWidth / 2) - (videoWidth / 2) + xOffset, topPos + 10 + yOffset, renderWidth, renderHeight, 0.0f, 0.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
    }

    private void renderIcon(GuiGraphics stack, ImageRenderer image) {
        RenderSystem.enableBlend();
        RenderSystem.bindTexture(image.texture(be.getTick(), 1, true));
        drawTexture(stack, image.texture(be.getTick(), 1, true), leftPos + (imageWidth / 2) - (videoWidth / 2), topPos + 10, videoWidth, videoHeight, 0.0f, 0.0f, 1.0f, 1.0f);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        RenderSystem.disableBlend();
    }

    private void drawTexture(GuiGraphics guiGraphics, int texture, int x, int y, int width, int height, float uMin, float vMin, float uMax, float vMax) {
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferBuilder.vertex(matrix4f, x, y + height, 0).uv(uMin, vMax).endVertex();   // Bottom-left
        bufferBuilder.vertex(matrix4f, x + width, y + height, 0).uv(uMax, vMax).endVertex();  // Bottom-right
        bufferBuilder.vertex(matrix4f, x + width, y, 0).uv(uMax, vMin).endVertex();  // Top-right
        bufferBuilder.vertex(matrix4f, x, y, 0).uv(uMin, vMin).endVertex();   // Top-left

        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, -1, pauseButton.visible, false, true));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

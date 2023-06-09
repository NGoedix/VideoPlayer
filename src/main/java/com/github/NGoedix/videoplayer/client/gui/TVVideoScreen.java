package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Constants;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.client.gui.components.CustomSlider;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class TVVideoScreen extends Screen {


    private static final Identifier TEXTURE = new Identifier(Constants.MOD_ID, "textures/gui/background.png");

    private final BlockEntity be;
    private String url;
    private final int tick;
    private int volume;
    private final boolean loop;


    // GUI
    private int imageWidth = 256;
    private int imageHeight = 256;
    private int leftPos;
    private int topPos;

    // Components useful for the GUI
    private TextFieldWidget urlBox;
    private CustomSlider volumeSlider;


    private boolean changed;


    public TVVideoScreen(BlockEntity be, String url, int tick, int volume, boolean loop) {
        super(Text.translatable("gui.tv_video_screen.title"));
        this.be = be;
        this.url = url;
        this.tick = tick;
        this.volume = volume;
        this.loop = loop;
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        MinecraftClient.getInstance().keyboard.setRepeatEvents(true);

        addDrawableChild(urlBox = new TextFieldWidget(textRenderer, leftPos + 10, topPos + 30, imageWidth - 26, 20, Text.of("")));
        // Set the text to the url
        urlBox.setMaxLength(32767);
        urlBox.setText(url == null ? "" : url);

        // Play button
        addDrawableChild(new ButtonWidget(leftPos + 10, topPos + 80, imageWidth - 24, 20, Text.translatable("gui.tv_video_screen.play"), button -> {
            sendUpdate(be.getPos(), url, volume, true, true, true);
        }));

        // Pause button
        addDrawableChild(new ButtonWidget(leftPos + 10, topPos + 105, imageWidth - 24, 20, Text.translatable("gui.tv_video_screen.pause"), button -> {
            sendUpdate(be.getPos(), url, volume, true, false, false);
        }));

        // Stop button
        addDrawableChild(new ButtonWidget(leftPos + 10, topPos + 130, imageWidth - 24, 20, Text.translatable("gui.tv_video_screen.stop"), button -> {
            sendUpdate(be.getPos(), url, volume, true, false, true);
        }));

        // Save button
        addDrawableChild(new ButtonWidget(leftPos + 10, topPos + 220, imageWidth - 24, 20, Text.translatable("gui.tv_video_screen.save"), button -> {
            int tempVolume = volumeSlider.getValue();
            String tempUrl = urlBox.getText();

            this.url = tempUrl;
            this.volume = tempVolume;

            // Cast the block entity to the correct type and set the volume
            ((TVBlockEntity) be).setVolume(tempVolume);

            changed = true;
            sendUpdate(be.getPos(), tempUrl, tempVolume, true, true, false);
        }));

        // Volume slider
        addDrawableChild(volumeSlider = new CustomSlider(leftPos + 10, topPos + 155, imageWidth - 24, 20, Text.translatable("gui.tv_video_screen.volume"), volume / 100f));
    }

    public void sendUpdate(BlockPos pos, String url, int volume, boolean loop, boolean isPlaying, boolean reset) {
        PacketHandler.sendC2SUpdateVideo(pos, url, volume, loop, isPlaying, reset);
    }

    @Override
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem._setShaderTexture(0, TEXTURE);
        drawTexture(pPoseStack, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        textRenderer.draw(pPoseStack, Text.translatable("gui.tv_video_screen.url_text"), width / 2 - textRenderer.getWidth(Text.translatable("gui.tv_video_screen.url_text")) / 2, topPos + 16, 0xFFFFFF);
    }

    @Override
    public void removed() {
        if (!changed)
            sendUpdate(be.getPos(), url, -1, true, true, false);
        MinecraftClient.getInstance().keyboard.setRepeatEvents(false);
    }



    @Override
    public boolean shouldPause() {
        return false;
    }
}

package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.UploadVideoUpdateMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class TVVideoScreen extends Screen {


    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/background.png");

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
    private EditBox urlBox;
    private CustomSlider volumeSlider;


    private boolean changed;


    public TVVideoScreen(BlockEntity be, String url, int tick, int volume, boolean loop) {
        super(new TranslatableComponent("gui.tv_video_screen.title"));
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

        minecraft.keyboardHandler.setSendRepeatsToGui(true);

        addRenderableWidget(urlBox = new EditBox(font, leftPos + 10, topPos + 30, imageWidth - 26, 20, new TextComponent("")));
        // Set the text to the url
        urlBox.setMaxLength(32767);
        urlBox.setValue(url == null ? "" : url);

        // Play button
        Button playButton;
        addRenderableWidget(playButton = new Button(leftPos + 10, topPos + 80, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.play"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, true, true));
        }));

        // Pause button
        Button pauseButton;
        addRenderableWidget(pauseButton = new Button(leftPos + 10, topPos + 105, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.pause"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, false, false));
        }));

        // Stop button
        Button stopButton;
        addRenderableWidget(stopButton = new Button(leftPos + 10, topPos + 130, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.stop"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, false, true));
        }));

        // Save button
        Button saveButton;
        addRenderableWidget(saveButton = new Button(leftPos + 10, topPos + 220, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.save"), button -> {
            int tempVolume = volumeSlider.getValue();
            String tempUrl = urlBox.getValue();

            this.url = tempUrl;
            this.volume = tempVolume;

            // Cast the block entity to the correct type and set the volume
            ((TVBlockEntity) be).setVolume(tempVolume);

            changed = true;
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), tempUrl, tempVolume, true, true, false));
        }));

        // Volume slider
        addRenderableWidget(volumeSlider = new CustomSlider(leftPos + 10, topPos + 155, imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.volume"), volume / 100f));
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem._setShaderTexture(0, TEXTURE);
        blit(pPoseStack, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        font.draw(pPoseStack, new TranslatableComponent("gui.tv_video_screen.url_text"), width / 2 - font.width(new TranslatableComponent("gui.tv_video_screen.url_text")) / 2, topPos + 16, 0xFFFFFF);

    }

    @Override
    public void removed() {
        if (!changed)
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, -1, true, true, false));
        minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

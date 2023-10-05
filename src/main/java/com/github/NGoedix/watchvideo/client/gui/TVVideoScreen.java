package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.message.UploadVideoUpdateMessage;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TVVideoScreen extends Screen {


    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/background.png");

    private final TileEntity be;
    private String url;
    private int volume;


    // GUI
    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    // Components useful for the GUI
    private TextFieldWidget urlBox;
    private CustomSlider volumeSlider;

    private boolean changed;


    public TVVideoScreen(TileEntity be, String url, int volume) {
        super(new TranslationTextComponent("gui.tv_video_screen.title"));
        this.be = be;
        this.url = url;
        this.volume = volume;
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        addButton(urlBox = new TextFieldWidget(font, leftPos + 10, topPos + 30, imageWidth - 26, 20, new StringTextComponent("")));
        // Set the text to the url
        urlBox.setMaxLength(32767);
        urlBox.setValue(url == null ? "" : url);

        // Play button
        addButton(new Button(leftPos + 10, topPos + 80, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.play"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, true, false));
        }));

        // Pause button
        addButton(new Button(leftPos + 10, topPos + 105, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.pause"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, false, false));
        }));

        // Stop button
        addButton(new Button(leftPos + 10, topPos + 130, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.stop"), button -> {
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, volume, true, false, true));
        }));

        // Save button
        addButton(new Button(leftPos + 10, topPos + 220, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.save"), button -> {
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
        addButton(volumeSlider = new CustomSlider(leftPos + 10, topPos + 155, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.volume"), volume / 100f));

        // Cast the block entity to the correct type and set the volume
        ((TVBlockEntity) be).setVolume(volume);
    }

    @Override
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(pPoseStack, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);
        // x, y, u, v, uWidth, vHeight, width, height, tileWidth, tileHeight

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        font.draw(pPoseStack, new TranslationTextComponent("gui.tv_video_screen.url_text"), width / 2f - font.width(new TranslationTextComponent("gui.tv_video_screen.url_text")) / 2f, topPos + 16, 0xFFFFFF);
    }

    @Override
    public void removed() {
        if (!changed)
            PacketHandler.sendToServer(new UploadVideoUpdateMessage(be.getBlockPos(), url, -1, true, true, false));
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

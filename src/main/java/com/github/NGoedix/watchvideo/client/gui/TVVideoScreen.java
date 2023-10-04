package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomGuiSlider;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.messages.UploadVideoUpdateMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class TVVideoScreen extends GuiScreen {

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
    private GuiTextField urlBox;
    private CustomGuiSlider volumeSlider;
    private GuiButton playButton, pauseButton, stopButton, saveButton;

    private boolean changed;

    public TVVideoScreen(TileEntity be, String url, int volume) {
        super();
        this.be = be;
        this.url = url;
        this.volume = volume;

        Reference.LOGGER.debug("TV Gui opened");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TEXTURE);
        drawScaledCustomSizeModalRect(leftPos, topPos , 0, 0, imageWidth, imageHeight, 320, 320, imageWidth, imageHeight);

        super.drawScreen(mouseX, mouseY, partialTicks);

        if (urlBox != null)
            urlBox.drawTextBox();

        if (fontRenderer != null)
            fontRenderer.drawString(I18n.format("gui.tv_video_screen.url_text"), (int) (width / 2f - fontRenderer.getStringWidth(I18n.format("gui.tv_video_screen.url_text")) / 2f), topPos + 16, 0xFFFFFF);
    }

    @Override
    public void initGui() {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        urlBox = new GuiTextField(0, fontRenderer, leftPos + 10, topPos + 30, imageWidth - 26, 20);
        // Set the text to the url
        urlBox.setMaxStringLength(32767);
        urlBox.setText(url == null ? "" : url);

        // Play button
        addButton(playButton = new GuiButton(1, leftPos + 10, topPos + 80, imageWidth - 24, 20, I18n.format("gui.tv_video_screen.play")));

        // Pause button
        addButton(pauseButton = new GuiButton(2, leftPos + 10, topPos + 105, imageWidth - 24, 20, I18n.format("gui.tv_video_screen.pause")));

        // Stop button
        addButton(stopButton = new GuiButton(3, leftPos + 10, topPos + 130, imageWidth - 24, 20, I18n.format("gui.tv_video_screen.stop")));

        // Save button
        addButton(saveButton = new GuiButton(4, leftPos + 10, topPos + 220, imageWidth - 24, 20, I18n.format("gui.tv_video_screen.save")));

        // Volume slider
        addButton(volumeSlider = new CustomGuiSlider(5, leftPos + 10, topPos + 155, imageWidth - 24, 20, I18n.format("gui.tv_video_screen.volume"), volume, 0, 100, 1));

        // Cast the block entity to the correct type and set the volume
        ((TVBlockEntity) be).setVolume(volume);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        if (!changed)
            PacketHandler.INSTANCE.sendToServer(new UploadVideoUpdateMessage(be.getPos(), url, -1, true, true, false));
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        urlBox.mouseClicked(mouseX, mouseY, mouseButton);
        volumeSlider.mousePressed(mc, mouseX, mouseY);
    }


    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if (button == playButton) {
            PacketHandler.INSTANCE.sendToServer(new UploadVideoUpdateMessage(be.getPos(), url, volume, true, true, false));
        }
        if (button == pauseButton) {
            PacketHandler.INSTANCE.sendToServer(new UploadVideoUpdateMessage(be.getPos(), url, volume, true, false, false));
        }
        if (button == stopButton) {
            PacketHandler.INSTANCE.sendToServer(new UploadVideoUpdateMessage(be.getPos(), url, volume, true, false, true));
        }
        if (button == saveButton) {
            int tempVolume = (int) volumeSlider.getValue();

            Reference.LOGGER.debug("Volume: " + volumeSlider.getValue());

            String tempUrl = urlBox.getText();

            this.url = tempUrl;
            this.volume = tempVolume;

            // Cast the block entity to the correct type and set the volume
            ((TVBlockEntity) be).setVolume(tempVolume);

            changed = true;
            PacketHandler.INSTANCE.sendToServer(new UploadVideoUpdateMessage(be.getPos(), tempUrl, tempVolume, true, true, false));
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        urlBox.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        volumeSlider.mouseReleased(mouseX, mouseY);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }


}

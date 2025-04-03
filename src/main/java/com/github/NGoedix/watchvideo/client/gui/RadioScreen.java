package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.watchvideo.client.gui.components.ScrollingStringList;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.gui.ClosedScreenPacket;
import com.github.NGoedix.watchvideo.network.packets.control.PausePacket;
import com.github.NGoedix.watchvideo.network.packets.control.UrlPacket;
import com.github.NGoedix.watchvideo.network.packets.control.VolumePacket;
import com.github.NGoedix.watchvideo.util.RadioStreams;
import com.github.NGoedix.watchvideo.util.config.TVConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.*;

public class RadioScreen extends Screen {

    // Textures
    private static final ResourceLocation TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/background.png");

    private static final ResourceLocation PLAY_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/play_button.png");
    private static final ResourceLocation PLAY_HOVER_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/play_button_hover.png");

    private static final ResourceLocation PAUSE_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/pause_button.png");
    private static final ResourceLocation PAUSE_HOVER_BUTTON_TEXTURE = new ResourceLocation(Reference.MOD_ID, "textures/gui/pause_button_hover.png");

    private ScrollingStringList countryList, stationList;
    private TextFieldWidget urlField;
    private CustomSlider volumeSlider;
    private ImageButtonHoverable playButton;
    private ImageButtonHoverable pauseButton;

    // Control
    private final RadioBlockEntity be;
    private String url;
    private int volume;
    private boolean ready = false;

    // GUI
    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    public RadioScreen(TileEntity be) {
        super(new TranslationTextComponent("gui.radio_screen.title"));
        this.be = (RadioBlockEntity) be;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        List<String> sortedCountries = new ArrayList<>(RadioStreams.getRadioStreams().keySet());
        Collections.sort(sortedCountries);

        // Country list
        addButton(countryList = new ScrollingStringList((width / 2) - 177, height / 2 - 3, 100, 248, sortedCountries));
        countryList.setSelected("Custom");
        countryList.setPlayerSlotClickListener(text -> {
            if (urlField != null)
                urlField.setEditable(text.equals("Custom"));
            if (stationList != null)
                stationList.updateEntries(getStationNamesForCountry(text, RadioStreams.getRadioStreams()));
        });
        countryList.setSelected(getCountryFromStationUrl(url));

        // Custom URL callback
        addButton(urlField = new TextFieldWidget(font, leftPos + 12, height / 2 - 30, imageWidth - 28, 20, new StringTextComponent("")));
        urlField.setResponder(text -> {
            if (!ready) return;
            if (countryList.getSelectedText().equals("Custom")) {
                if (text.matches(TVConfig.URL_PATTERN))
                    PacketHandler.sendToServer(new UrlPacket(be.getBlockPos(), text));
            }
        });
        urlField.setEditable(countryList.getSelectedText().equals("Custom"));
        urlField.setMaxLength(32767);
        urlField.setValue(url);

        // Station of country
        addButton(stationList = new ScrollingStringList((width / 2) + 172, height / 2 - 3, 100, 248, getStationNamesForCountry(countryList.getSelectedText(), RadioStreams.getRadioStreams())));
        stationList.setPlayerSlotClickListener(text -> {
            if (!ready) return;

            if (text != null && !text.isEmpty()) {
                urlField.setValue(getStationUrlFromStation(text));
                PacketHandler.sendToServer(new UrlPacket(be.getBlockPos(), getStationUrlFromStation(text)));
            }
        });
        stationList.setSelected(getStationFromStationUrl(url));
        stationList.updateEntries(getStationNamesForCountry(countryList.getSelectedText(), RadioStreams.getRadioStreams()));

        // Volume slider
        addButton(volumeSlider = new CustomSlider(leftPos + 10, height / 2 - 5, imageWidth - 24, 20, new TranslationTextComponent("gui.tv_video_screen.volume"), volume / 100f, false));
        volumeSlider.setOnSlideListener(value -> {
            if (!ready) return;

            volume = (int) volumeSlider.getValue();
            PacketHandler.sendToServer(new VolumePacket(be.getBlockPos(), volume));
        });
        volumeSlider.setValue(volume / 100f);

        // Buttons
        addButton(playButton = new ImageButtonHoverable(width / 2 - 10, topPos + 150, 20, 20, 0, 0, 0, PLAY_BUTTON_TEXTURE, PLAY_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!ready) return;

            if (!urlField.getValue().isEmpty()) {
                playButton.visible = false;
                pauseButton.visible = true;

                if (be.requestDisplay() == null) return;
                PacketHandler.sendToServer(new PausePacket(be.getBlockPos(), false));
            }
        }));
        playButton.visible = !be.isPlaying();

        addButton(pauseButton = new ImageButtonHoverable(width / 2 - 10, topPos + 150, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!ready) return;

            if (!urlField.getValue().isEmpty()) {
                playButton.visible = true;
                pauseButton.visible = false;

                if (be.requestDisplay() == null) return;
                PacketHandler.sendToServer(new PausePacket(be.getBlockPos(), true));
            }
        }));
        pauseButton.visible = be.isPlaying();
    }

    private String getCountryFromStationUrl(String url) {
        for (Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getStreamLink().equals(url)) {
                    return entry.getKey();
                }
            }
        }
        return "Custom";
    }

    private String getStationFromStationUrl(String url) {
        for (Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getStreamLink().equals(url)) {
                    return station.getRadioName();
                }
            }
        }
        return null;
    }

    private List<String> getStationNamesForCountry(String country, Map<String, List<RadioStreams.RadioStream>> radioStreamsByCountry) {
        List<String> stationNames = new ArrayList<>(9);

        if (country == null) {
            for (int i = 0; i < 8; i++)
                stationNames.add("");
            return stationNames;
        }

        List<RadioStreams.RadioStream> stations = radioStreamsByCountry.get(country);
        if (stations != null) {
            for (RadioStreams.RadioStream station : stations) {
                stationNames.add(station.getRadioName());
            }
        }

        while (stationNames.size() < 8)
            stationNames.add("");

        return stationNames;
    }

    private String getStationUrlFromStation(String stationUrl) {
        for (Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getRadioName().equals(stationUrl)) {
                    return station.getStreamLink();
                }
            }
        }
        return "";
    }

    @Override
    public void render(MatrixStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        if (!ready) ready = true;
        renderBackground(pPoseStack);

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(TEXTURE);
        blit(pPoseStack, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        font.draw(pPoseStack, "Radio Player (by Goedix)", (width / 2f) - 62, height / 2f - 100, 0xFFFFFF);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(new ClosedScreenPacket(be != null ? be.getBlockPos() : null));
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

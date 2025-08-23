package com.github.NGoedix.videoplayer.client.gui;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.videoplayer.client.gui.components.CustomSlider;
import com.github.NGoedix.videoplayer.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.videoplayer.client.gui.components.ScrollingStringList;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.RadioStreams;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RadioScreen extends Screen {

    // Textures
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/background.png");

    private static final ResourceLocation PLAY_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/play_button.png");
    private static final ResourceLocation PLAY_HOVER_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/play_button_hover.png");

    private static final ResourceLocation PAUSE_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/pause_button.png");
    private static final ResourceLocation PAUSE_HOVER_BUTTON_TEXTURE = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/gui/pause_button_hover.png");

    private ScrollingStringList countryList, stationList;
    private EditBox urlField;
    private CustomSlider volumeSlider;
    private ImageButtonHoverable playButton;
    private ImageButtonHoverable pauseButton;

    // Control
    private final RadioBlockEntity be;
    private final ItemStack item;
    private String url;
    private int volume;
    private boolean ready = false;

    // GUI
    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    private boolean canClick = true;

    public RadioScreen(BlockEntity be) {
        super(Component.translatable("gui.radio_screen.title"));
        this.be = (RadioBlockEntity) be;
        this.item = null;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    public RadioScreen(ItemStack item) {
        super(Component.translatable("gui.radio_screen.title"));
        this.be = null;
        this.item = item;

//        this.url = HandRadioItem.getUrl(item);
//        this.volume = HandRadioItem.getVolume(item);
    }

    @Override
    protected void init() {
        super.init();

        leftPos = (width - imageWidth) / 2;
        topPos = (height - imageHeight) / 2;

        List<String> sortedCountries = new ArrayList<>(RadioStreams.getRadioStreams().keySet());
        Collections.sort(sortedCountries);

        addRenderableWidget(countryList = new ScrollingStringList((width / 2) - 177, height / 2 - 3, 100, 248, sortedCountries));
        countryList.setSelected("Custom");
        countryList.setPlayerSlotClickListener(text -> {
            if (urlField != null)
                urlField.setEditable(text.equals("Custom"));
            if (stationList != null)
                stationList.updateEntries(getStationNamesForCountry(text, RadioStreams.getRadioStreams()));
        });
        countryList.setSelected(getCountryFromStationUrl(url));

        // Expresión regular para validar una URL
        String urlPattern = "(http|https)://(www\\.)?([\\w]+\\.)+[\\w]{2,63}/?[\\w\\-\\?\\=\\&\\%\\.\\/]*/?";

        addRenderableWidget(urlField = new EditBox(font, leftPos + 12, height / 2 - 30, imageWidth - 28, 20, Component.literal("")));
        urlField.setResponder(text -> {
            if (!ready) return;
            if (countryList.getSelectedText().equals("Custom")) {
                if (text.matches(urlPattern))
                    sendUpdate(urlField.getValue(), volume, true, 0, false);
            }
        });
        urlField.setEditable(countryList.getSelectedText().equals("Custom"));
        urlField.setMaxLength(32767);
        urlField.setValue(url);

        addRenderableWidget(stationList = new ScrollingStringList((width / 2) + 172, height / 2 - 3, 100, 248, getStationNamesForCountry(countryList.getSelectedText(), RadioStreams.getRadioStreams())));
        stationList.setPlayerSlotClickListener(text -> {
            if (!ready) return;

            if (text != null && !text.isEmpty()) {
                urlField.setValue(getStationUrlFromStation(text));
                sendUpdate(urlField.getValue(), volume, pauseButton.visible, 0, false);
            }
        });
        stationList.setSelected(getStationFromStationUrl(url));
        stationList.updateEntries(getStationNamesForCountry(countryList.getSelectedText(), RadioStreams.getRadioStreams()));

        // Volume slider
        addRenderableWidget(volumeSlider = new CustomSlider(leftPos + 10, height / 2 - 5, imageWidth - 24, 20, Component.translatable("gui.tv_video_screen.volume"), volume / 100f, false));
        volumeSlider.setOnSlideListener(value -> {
            if (!ready) return;

            if (be != null)
                be.setVolume((int) value);
//            else
//                HandRadioItem.setVolume(item, (int) value);
            volume = (int) volumeSlider.getValue();

            sendUpdate(urlField.getValue(), volume, pauseButton.visible, -1, false);
        });
        volumeSlider.setValue(volume / 100f);

        // Buttons
        addRenderableWidget(playButton = new ImageButtonHoverable(width / 2 - 10, topPos + 150, 20, 20, 0, 0, 0, PLAY_BUTTON_TEXTURE, PLAY_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!ready) return;

            if (!urlField.getValue().isEmpty() && canClick) {
                canClick = false;
                playButton.visible = false;
                pauseButton.visible = true;

                if (be != null) {
                    if (be.requestDisplay() == null) return;
                    be.requestDisplay().resume(be.getTick());
                } else {
//                    if (HandRadioItem.requestDisplay(item) == null) return;
//                    HandRadioItem.requestDisplay(item).resume(-1);
                }

                sendUpdate(urlField.getValue(), volume, true, 0, false);
            }
        }));
        playButton.visible = be != null ? !be.isPlaying() : url.isEmpty();

        addRenderableWidget(pauseButton = new ImageButtonHoverable(width / 2 - 10, topPos + 150, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!ready) return;

            if (!urlField.getValue().isEmpty() && canClick) {
                canClick = false;
                playButton.visible = true;
                pauseButton.visible = false;

                if (be != null) {
                    if (be.requestDisplay() == null) return;
                    be.requestDisplay().pause(be.getTick());
                } else {
//                    if (HandRadioItem.requestDisplay(item) == null) return;
//                    HandRadioItem.requestDisplay(item).pause(-1);
                }
                sendUpdate(urlField.getValue(), volume, false, 0, false);
            }
        }));
        pauseButton.visible = be != null ? be.isPlaying() : !url.isEmpty();
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
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (!ready) ready = true;
        renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem._setShaderTexture(0, TEXTURE);
        pGuiGraphics.blit(TEXTURE, leftPos, topPos, 320, 320, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

        pGuiGraphics.drawString(font, "Radio Player (by Goedix)", (int) ((width / 2f) - 62), (int) (height / 2f - 100), 0xFFFFFF);

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        canClick = true;
    }

    private void sendUpdate(String url, int volume, boolean isPlaying, int tick, boolean exit) {
        if (be != null) {
            PacketHandler.sendC2SRadioUpdateMessage(be.getBlockPos(), url, volume, tick == -1 ? -1 : be.getTick(), isPlaying, exit);
        } else {
//            PacketHandler.sendToServer(new UploadRadioUpdateMessage(item, url, volume, -1, isPlaying, exit));
        }
    }

    @Override
    public void removed() {
        sendUpdate(urlField.getValue(), volume, pauseButton.visible, -1, true);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

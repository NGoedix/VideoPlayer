package com.github.NGoedix.watchvideo.client.gui;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.watchvideo.client.gui.components.CustomSlider;
import com.github.NGoedix.watchvideo.client.gui.components.ImageButtonHoverable;
import com.github.NGoedix.watchvideo.client.gui.components.ScrollingStringList;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.network.packets.control.PausePacket;
import com.github.NGoedix.watchvideo.network.packets.control.UrlPacket;
import com.github.NGoedix.watchvideo.network.packets.control.VolumePacket;
import com.github.NGoedix.watchvideo.network.packets.gui.ClosedScreenPacket;
import com.github.NGoedix.watchvideo.util.RadioStreams;
import com.github.NGoedix.watchvideo.util.config.TVConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.NGoedix.watchvideo.client.gui.VideoScreen.*;

public class RadioScreen extends Screen {

    private ScrollingStringList countryList, stationList;
    private EditBox urlField;
    private CustomSlider volumeSlider;
    private ImageButtonHoverable playButton;
    private ImageButtonHoverable pauseButton;

    // Control
    private final RadioBlockEntity be;
    private final String url;
    private int volume;
    private boolean ready = false;

    // GUI
    private final int imageWidth = 256;
    private final int imageHeight = 256;
    private int leftPos;
    private int topPos;

    public RadioScreen(final BlockEntity be) {
        super(new TranslatableComponent("gui.radio_screen.title"));
        this.be = (RadioBlockEntity) be;
        this.url = this.be.getUrl();
        this.volume = this.be.getVolume();
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);

        final List<String> sortedCountries = new ArrayList<>(RadioStreams.getRadioStreams().keySet());
        Collections.sort(sortedCountries);

        // Country list
        this.addRenderableWidget(this.countryList = new ScrollingStringList((this.width / 2) - 177, this.height / 2 - 3, 100, 248, sortedCountries));
        this.countryList.setSelected("Custom");
        this.countryList.setPlayerSlotClickListener(text -> {
            if (this.urlField != null)
                this.urlField.setEditable(text.equals("Custom"));
            if (this.stationList != null)
                this.stationList.updateEntries(this.getStationNamesForCountry(text, RadioStreams.getRadioStreams()));
        });
        this.countryList.setSelected(this.getCountryFromStationUrl(this.url));

        // Custom URL callback
        this.addRenderableWidget(this.urlField = new EditBox(this.font, this.leftPos + 12, this.height / 2 - 30, this.imageWidth - 28, 20, new TextComponent("")));
        this.urlField.setResponder(text -> {
            if (!this.ready) return;

            if (this.countryList.getSelectedText().equals("Custom")) {
                if (text.matches(TVConfig.URL_PATTERN))
                    PacketHandler.sendToServer(new UrlPacket(this.be.getBlockPos(), text));
            }
        });
        this.urlField.setEditable(this.countryList.getSelectedText().equals("Custom"));
        this.urlField.setMaxLength(32767);
        this.urlField.setValue(this.url);

        // Station of country
        this.addRenderableWidget(this.stationList = new ScrollingStringList((this.width / 2) + 172, this.height / 2 - 3, 100, 248, this.getStationNamesForCountry(this.countryList.getSelectedText(), RadioStreams.getRadioStreams())));
        this.stationList.setPlayerSlotClickListener(text -> {
            if (!this.ready) return;

            if (text != null && !text.isEmpty()) {
                this.urlField.setValue(this.getStationUrlFromStation(text));
                PacketHandler.sendToServer(new UrlPacket(this.be.getBlockPos(), this.getStationUrlFromStation(text)));
            }
        });
        this.stationList.setSelected(this.getStationFromStationUrl(this.url));
        this.stationList.updateEntries(this.getStationNamesForCountry(this.countryList.getSelectedText(), RadioStreams.getRadioStreams()));

        // Volume slider
        this.addRenderableWidget(this.volumeSlider = new CustomSlider(this.leftPos + 10, this.height / 2 - 5, this.imageWidth - 24, 20, new TranslatableComponent("gui.tv_video_screen.volume"), this.volume / 100f, false));
        this.volumeSlider.setOnSlideListener(value -> {
            if (!this.ready) return;

            this.volume = (int) this.volumeSlider.getValue();
            PacketHandler.sendToServer(new VolumePacket(this.be.getBlockPos(), this.volume));
        });
        this.volumeSlider.setValue(this.volume / 100f);

        // Buttons
        this.addRenderableWidget(this.playButton = new ImageButtonHoverable(this.width / 2 - 10, this.topPos + 150, 20, 20, 0, 0, 0, PLAY_BUTTON_TEXTURE, PLAY_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!this.ready) return;

            if (!this.urlField.getValue().isEmpty()) {
                this.playButton.visible = false;
                this.pauseButton.visible = true;

                if (this.be.requestDisplay() == null) return;
                PacketHandler.sendToServer(new PausePacket(this.be.getBlockPos(), false));
            }
        }));
        this.playButton.visible = this.be != null ? !this.be.isPlaying() : this.url.isEmpty();

        this.addRenderableWidget(this.pauseButton = new ImageButtonHoverable(this.width / 2 - 10, this.topPos + 150, 20, 20, 0, 0, 0, PAUSE_BUTTON_TEXTURE, PAUSE_HOVER_BUTTON_TEXTURE, 20, 20, button -> {
            if (!this.ready) return;

            if (!this.urlField.getValue().isEmpty()) {
                this.playButton.visible = true;
                this.pauseButton.visible = false;

                if (this.be.requestDisplay() == null) return;
                PacketHandler.sendToServer(new PausePacket(this.be.getBlockPos(), true));
            }
        }));
        this.pauseButton.visible = this.be != null ? this.be.isPlaying() : !this.url.isEmpty();
    }

    private String getCountryFromStationUrl(final String url) {
        for (final Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (final RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getStreamLink().equals(url)) {
                    return entry.getKey();
                }
            }
        }
        return "Custom";
    }

    private String getStationFromStationUrl(final String url) {
        for (final Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (final RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getStreamLink().equals(url)) {
                    return station.getRadioName();
                }
            }
        }
        return null;
    }

    private List<String> getStationNamesForCountry(final String country, final Map<String, List<RadioStreams.RadioStream>> radioStreamsByCountry) {
        final List<String> stationNames = new ArrayList<>(9);

        if (country == null) {
            for (int i = 0; i < 8; i++)
                stationNames.add("");
            return stationNames;
        }

        final List<RadioStreams.RadioStream> stations = radioStreamsByCountry.get(country);
        if (stations != null) {
            for (final RadioStreams.RadioStream station : stations) {
                stationNames.add(station.getRadioName());
            }
        }

        while (stationNames.size() < 8)
            stationNames.add("");

        return stationNames;
    }

    private String getStationUrlFromStation(final String stationUrl) {
        for (final Map.Entry<String, List<RadioStreams.RadioStream>> entry : RadioStreams.getRadioStreams().entrySet()) {
            for (final RadioStreams.RadioStream station : entry.getValue()) {
                if (station.getRadioName().equals(stationUrl)) {
                    return station.getStreamLink();
                }
            }
        }
        return "";
    }

    @Override
    public void render(final PoseStack pPoseStack, final int pMouseX, final int pMouseY, final float pPartialTick) {
        if (!this.ready) this.ready = true;
        this.renderBackground(pPoseStack);

        RenderSystem.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem._setShaderTexture(0, TEXTURE);
        blit(pPoseStack, this.leftPos, this.topPos, 320, 320, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);

        this.font.draw(pPoseStack, "Radio Player (by Goedix)", (this.width / 2f) - 62, this.height / 2f - 100, 0xFFFFFF);

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    public void removed() {
        PacketHandler.sendToServer(new ClosedScreenPacket(this.be != null ? this.be.getBlockPos() : null));
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

package com.github.NGoedix.videoplayer.util.displayers;

import net.minecraft.client.Minecraft;
import org.watermedia.api.player.videolan.VideoPlayer;

public class VideoScreenDisplay {

    private final VideoPlayer player;
    private final String url;
    private final int volume;
    private final int position;
    private final int optionInMode;
    private final int optionInSecs;
    private final int optionOutMode;
    private final int optionOutSecs;

    public VideoScreenDisplay(String url, int volume, int position, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        player = new VideoPlayer(null, Minecraft.getInstance());
        this.url = url;
        this.volume = volume;
        this.position = position;
        this.optionInMode = optionInMode;
        this.optionInSecs = optionInSecs;
        this.optionOutMode = optionOutMode;
        this.optionOutSecs = optionOutSecs;
    }

    public String getUrl() {
        return url;
    }

    public int getPosition() {
        return position;
    }

    public VideoPlayer getPlayer() {
        return player;
    }

    public int getVolume() {
        return volume;
    }

    public int getOptionInMode() {
        return optionInMode;
    }

    public int getOptionInSecs() {
        return optionInSecs;
    }

    public int getOptionOutMode() {
        return optionOutMode;
    }

    public int getOptionOutSecs() {
        return optionOutSecs;
    }
}

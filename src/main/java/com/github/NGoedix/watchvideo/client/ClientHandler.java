package com.github.NGoedix.watchvideo.client;

import com.github.NGoedix.watchvideo.client.gui.VideoScreen;
import net.minecraft.client.Minecraft;

public class ClientHandler {
    public static void openVideo(String url) {
        Minecraft.getInstance().setScreen(new VideoScreen(url));
    }
}

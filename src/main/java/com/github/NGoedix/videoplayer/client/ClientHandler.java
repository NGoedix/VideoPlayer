package com.github.NGoedix.videoplayer.client;

import com.github.NGoedix.videoplayer.client.render.VideoScreen;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.FancyEvents;
import com.github.NGoedix.videoplayer.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("Initializing Client");

        FancyEvents fancyEvents = new FancyEvents();
        fancyEvents.register();

        PacketHandler.registerClient();
    }

    public static void openVideo(MinecraftClient client, String url){
        client.execute(() -> client.setScreen(new VideoScreen(url)));
    }
}

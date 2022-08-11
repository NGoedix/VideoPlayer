package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nick1st.fancyvideo.api.DynamicResourceLocation;
import nick1st.fancyvideo.api.eventbus.EventException;
import nick1st.fancyvideo.api.eventbus.FancyVideoEvent;
import nick1st.fancyvideo.api.eventbus.FancyVideoEventBus;
import nick1st.fancyvideo.api.eventbus.event.PlayerRegistryEvent;
import nick1st.fancyvideo.api.mediaPlayer.SimpleMediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.NGoedix.watchvideo.common.CommonHandler;

@Mod(Reference.MOD_ID)
public class WatchVideo {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    static DynamicResourceLocation resourceLocation;

    public WatchVideo() {
        LOGGER.info("Initializing mod...");
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);
        try {
            FancyVideoEventBus.getInstance().registerEvent(this);
        } catch(EventException.EventRegistryException | EventException.UnauthorizedRegistryException e) {
            LOGGER.warn("A fatal API error occurred!");
        }

        eventBus.addListener(this::onCommonSetup);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonHandler::setup);
    }

    @FancyVideoEvent
    @SuppressWarnings("unused")
    public void init(PlayerRegistryEvent.AddPlayerEvent event) {
        resourceLocation = new DynamicResourceLocation(Reference.MOD_ID, "video");
        event.handler().registerPlayerOnFreeResLoc(resourceLocation, SimpleMediaPlayer.class);
        if (event.handler().getMediaPlayer(resourceLocation).providesAPI()) {
            LOGGER.info("Correctly setup");
        } else {
            LOGGER.warn("Running in NO_LIBRARY_MODE");
        }
    }

    public static DynamicResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}

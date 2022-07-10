package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.commands.PlayVideoCommand;
import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nick1st.fancyvideo.api.DynamicResourceLocation;
import nick1st.fancyvideo.api.eventbus.FancyVideoEvent;
import nick1st.fancyvideo.api.eventbus.event.PlayerRegistryEvent;
import nick1st.fancyvideo.api.mediaPlayer.SimpleMediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.github.NGoedix.watchvideo.client.ClientHandler;
import com.github.NGoedix.watchvideo.common.CommonHandler;

@Mod(Reference.MOD_ID)
public class WatchVideo {

    public static final Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);

    static DynamicResourceLocation resourceLocation;

    public WatchVideo() {
        LOGGER.info("Initializing mod...");
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ClientHandler::setup);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonHandler::setup);
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event){
        LOGGER.info("Registering command...");
        PlayVideoCommand.register(event.getDispatcher());
    }

    @FancyVideoEvent
    @SuppressWarnings("unused")
    public void init(PlayerRegistryEvent.AddPlayerEvent event) {
        resourceLocation = new DynamicResourceLocation(Reference.MOD_ID, "example");
        event.handler().registerPlayerOnFreeResLoc(resourceLocation, SimpleMediaPlayer.class);
        if (event.handler().getMediaPlayer(resourceLocation).providesAPI()) {
            LOGGER.info("Correctly setup");
//            event.handler().getMediaPlayer(resourceLocation).api().media().prepare("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
//            event.handler().getMediaPlayer(resourceLocation).api().audio().setVolume(200);
        } else {
            LOGGER.warn("Running in NO_LIBRARY_MODE");
        }
    }

    public static DynamicResourceLocation getResourceLocation() {
        return resourceLocation;
    }
}

package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.client.render.TVBlockRenderer;
import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import com.github.NGoedix.watchvideo.common.CommonHandler;
import com.github.NGoedix.watchvideo.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import nick1st.fancyvideo.api.DynamicResourceLocation;
import nick1st.fancyvideo.api.eventbus.EventException;
import nick1st.fancyvideo.api.eventbus.FancyVideoEvent;
import nick1st.fancyvideo.api.eventbus.FancyVideoEventBus;
import nick1st.fancyvideo.api.eventbus.event.PlayerRegistryEvent;
import nick1st.fancyvideo.api.mediaPlayer.SimpleMediaPlayer;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MOD_ID)
public class VideoPlayer
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    static DynamicResourceLocation resourceLocation;

    public VideoPlayer()
    {
        LOGGER.info("Initializing mod...");
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);

        try {
            FancyVideoEventBus.getInstance().registerEvent(this);
        } catch(EventException.EventRegistryException | EventException.UnauthorizedRegistryException e) {
            LOGGER.warn("A fatal API error occurred!");
        }

        // Register the commonSetup method for modloading
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModBlockEntities.register(eventBus);

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onClientSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(CommonHandler::setup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TV_BLOCK.get(), RenderType.cutout());
        BlockEntityRenderers.register(ModBlockEntities.TV_BLOCK_ENTITY.get(), TVBlockRenderer::new);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
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

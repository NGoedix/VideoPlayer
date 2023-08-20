package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import com.github.NGoedix.watchvideo.common.CommonHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MOD_ID)
public class WatchVideo
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public WatchVideo() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(CommonHandler::setup);
    }
}

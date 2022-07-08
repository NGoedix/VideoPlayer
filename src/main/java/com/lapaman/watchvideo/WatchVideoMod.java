package com.lapaman.watchvideo;

import com.lapaman.watchvideo.commands.VideoCommand;
import com.lapaman.watchvideo.network.PacketHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class WatchVideoMod
{

    private Logger logger;

    private static WatchVideoMod watchVideoMod;

    @SidedProxy(clientSide = Reference.PROXY_CLIENT, serverSide = Reference.PROXY_SERVER)
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        watchVideoMod = this;
        logger = event.getModLog();
        logger.info("Pre-initializing WatchVideoMod");
        PacketHandler.init();
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("Initializing WatchVideoMod");
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        logger.info("Post-initializing WatchVideoMod");
        proxy.postInit();
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new VideoCommand());
    }

    public Logger getLogger() {
        return logger;
    }

    public static WatchVideoMod getWatchVideoMod() {
        return watchVideoMod;
    }
}

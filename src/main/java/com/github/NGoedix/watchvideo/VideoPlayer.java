package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.entity.TVBlockEntity;
import com.github.NGoedix.watchvideo.network.PacketHandler;
import com.github.NGoedix.watchvideo.proxy.CommonProxy;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.github.NGoedix.watchvideo.util.handlers.RegistryHandler;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(modid = Reference.MOD_ID, name = Reference.NAME, version = Reference.VERSION, dependencies = "after:watermedia")
public class VideoPlayer
{

    public static CreativeTabs videoplayerTab = new VideoPlayerModTab("videoplayer_tab");

    @Mod.Instance
    public static VideoPlayer instance;

    @SideOnly(Side.CLIENT)
    public static ImageRenderer IMG_PAUSED;

    @SideOnly(Side.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    @SidedProxy(clientSide = Reference.CLIENT, serverSide = Reference.COMMON)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        Reference.LOGGER = event.getModLog();
        RegistryHandler.preInitRegistries(event);
        PacketHandler.registerMessages();
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TVBlockEntity.class, new ResourceLocation(Reference.MOD_ID, "TVBlockEntity"));
    }

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MissingModsException exception = new MissingModsException(Reference.MOD_ID, Reference.NAME);
            exception.addMissingMod(new DefaultArtifactVersion("[2.0,2.1)"), null, true);
            if (!Loader.instance().getIndexedModList().containsKey("watermedia")) throw exception;
        }
    }

    @Mod.EventHandler
    public static void serverInit(FMLServerStartingEvent event) {
        RegistryHandler.serverRegistries(event);
    }
}

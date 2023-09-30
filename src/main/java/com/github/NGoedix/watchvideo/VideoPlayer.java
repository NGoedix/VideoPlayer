package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.client.render.TVBlockRenderer;
import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import com.github.NGoedix.watchvideo.commands.arguments.SymbolStringArgumentSerializer;
import com.github.NGoedix.watchvideo.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.watchvideo.common.CommonHandler;
import com.github.NGoedix.watchvideo.item.ModItems;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import com.mojang.logging.LogUtils;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.api.image.ImageRenderer;
import me.srrapero720.watermedia.core.tools.JarTool;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent.Unload;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MOD_ID)
public class VideoPlayer
{

    private static ImageRenderer IMG_PAUSED;

    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    public VideoPlayer()
    {
        Reference.LOGGER.info("Initializing mod...");
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);

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
        ArgumentTypeInfos.registerByClass(SymbolStringArgumentType.class, new SymbolStringArgumentSerializer());
        event.enqueueWork(CommonHandler::setup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TV_BLOCK.get(), RenderType.cutout());
        BlockEntityRenderers.register(ModBlockEntities.TV_BLOCK_ENTITY.get(), TVBlockRenderer::new);

        IMG_PAUSED = ImageAPI.renderer(JarTool.readImage(VideoPlayer.class.getClassLoader(), "/pictures/paused.png"), true);
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {

        @SubscribeEvent
        public static void onRenderTickEvent(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                TextureCache.renderTick();
            }
        }

        @SubscribeEvent
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                TextureCache.clientTick();
                VideoDisplayer.tick();
            }
        }

        @SubscribeEvent
        public static void onUnloadingLevel(Unload unload) {
            if (unload.getLevel() != null && unload.getLevel().isClientSide()) {
                TextureCache.unload();
                VideoDisplayer.unload();
            }
        }
    }
}

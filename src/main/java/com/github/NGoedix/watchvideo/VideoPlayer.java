package com.github.NGoedix.watchvideo;

import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.block.entity.ModBlockEntities;
import com.github.NGoedix.watchvideo.client.render.TVBlockRenderer;
import com.github.NGoedix.watchvideo.commands.RegisterCommands;
import com.github.NGoedix.watchvideo.commands.arguments.SymbolStringArgumentSerializer;
import com.github.NGoedix.watchvideo.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.watchvideo.item.ModItems;
import com.github.NGoedix.watchvideo.util.RadioStreams;
import com.github.NGoedix.watchvideo.util.displayers.Display;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import com.github.NGoedix.watchvideo.common.CommonHandler;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.core.tools.JarTool;

@Mod(Reference.MOD_ID)
public class VideoPlayer {

    @OnlyIn(Dist.CLIENT)
    private static ImageRenderer IMG_PAUSED;

    @OnlyIn(Dist.CLIENT)
    private static ImageRenderer IMG_STEP10;

    @OnlyIn(Dist.CLIENT)
    private static ImageRenderer IMG_STEP5;

    @OnlyIn(Dist.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    @OnlyIn(Dist.CLIENT)
    public static ImageRenderer step10Image() { return IMG_STEP10; }

    @OnlyIn(Dist.CLIENT)
    public static ImageRenderer step5Image() { return IMG_STEP5; }

    public VideoPlayer() {
        Reference.LOGGER.info("Initializing mod...");
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModItems.register(eventBus);

        eventBus.addListener(this::onCommonSetup);
        eventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(RegisterCommands.class);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        if (VideoPlayerUtils.isInstalled("mr_stellarity", "stellarity")) {
            throw new VideoPlayerUtils.UnsupportedModException("mr_stellarity (Stellarity)", "breaks picture rendering, overwrites Minecraft core shaders and isn't possible work around that");
        }

        RadioStreams.prepareRadios();

        ItemBlockRenderTypes.setRenderLayer(ModBlocks.TV_BLOCK.get(), RenderType.cutout());
        BlockEntityRenderers.register(ModBlockEntities.TV_BLOCK_ENTITY.get(), TVBlockRenderer::new);

        IMG_PAUSED = ImageAPI.renderer(JarTool.readImage("/pictures/paused.png"), true);
        IMG_STEP10 = ImageAPI.renderer(JarTool.readImage("/pictures/step10.png"), true);
        IMG_STEP5 = ImageAPI.renderer(JarTool.readImage("/pictures/step5.png"), true);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        ArgumentTypes.register("brigadier:symbol_string", SymbolStringArgumentType.class, new SymbolStringArgumentSerializer());
        event.enqueueWork(CommonHandler::setup);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
//        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
//            ClientHandler.gui.renderOverlay(event.getMatrixStack());
//        }
    }

    @Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static final class Events {
        @SubscribeEvent
        public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                Display.tick();
            }
        }

        @SubscribeEvent
        public static void onUnloadingLevel(WorldEvent.Unload unload) {
            if (unload.getWorld() != null && unload.getWorld().isClientSide()) {
                Display.unload();
            }
        }
    }
}

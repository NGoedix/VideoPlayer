package com.github.NGoedix.watchvideo.util.handlers;

import com.github.NGoedix.watchvideo.Reference;
import com.github.NGoedix.watchvideo.VideoPlayer;
import com.github.NGoedix.watchvideo.block.ModBlocks;
import com.github.NGoedix.watchvideo.block.entity.TVBlockEntity;
import com.github.NGoedix.watchvideo.client.renderer.TVBlockRenderer;
import com.github.NGoedix.watchvideo.command.PlayVideoCommand;
import com.github.NGoedix.watchvideo.item.ModItems;
import com.github.NGoedix.watchvideo.util.IHasModel;
import com.github.NGoedix.watchvideo.util.cache.TextureCache;
import com.github.NGoedix.watchvideo.util.displayers.VideoDisplayer;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.core.tools.JarTool;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.BlockRedstoneLight;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class RegistryHandler {

    public static void preInitRegistries(FMLPreInitializationEvent event)
    {
//        EntityInit.registerEntities();
    }

    @SubscribeEvent
    public static void onItemRegister(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ModItems.ITEMS.toArray(new Item[0]));

        if (FMLCommonHandler.instance().getSide().isClient())
            initClient();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        VideoPlayer.IMG_PAUSED = ImageAPI.renderer(JarTool.readImage(RegistryHandler.class.getClassLoader(), "/pictures/paused.png"), true);
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(ModBlocks.TV_BLOCK), stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.TV_BLOCK), 0,  new ModelResourceLocation(new ResourceLocation(Reference.MOD_ID + ":tv_block"), "inventory"));
        ClientRegistry.bindTileEntitySpecialRenderer(TVBlockEntity.class, new TVBlockRenderer());
    }

    @SubscribeEvent
    public static void onBlockRegister(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(ModBlocks.BLOCKS.toArray(new Block[0]));
    }


    public static void serverRegistries(FMLServerStartingEvent event) {
        event.registerServerCommand(new PlayVideoCommand());
    }

    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for (Item item : ModItems.ITEMS) {
            if (item instanceof IHasModel) {
                ((IHasModel) item).registerModels();
            }
        }

        for (Block block : ModBlocks.BLOCKS) {
            if (block instanceof IHasModel) {
                ((IHasModel) block).registerModels();
            }
        }
    }

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
    public static void onUnloadingLevel(WorldEvent.Unload unload) {
        if (unload.getWorld() != null && unload.getWorld().isRemote) {
            TextureCache.unload();
            VideoDisplayer.unload();
        }
    }
}

package com.github.NGoedix.videoplayer.client;

import com.github.NGoedix.videoplayer.VideoPlayer;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.client.gui.TVVideoScreen;
import com.github.NGoedix.videoplayer.client.gui.VideoScreen;
import com.github.NGoedix.videoplayer.client.render.TVBlockRenderer;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentSerializer;
import com.github.NGoedix.videoplayer.commands.arguments.SymbolStringArgumentType;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.Constants;
import me.srrapero720.watermedia.api.image.ImageAPI;
import me.srrapero720.watermedia.core.tools.JarTool;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.mixin.command.ArgumentTypesAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("Initializing Client");

        PacketHandler.registerS2CPackets();
        BlockEntityRendererRegistry.register(ModBlockEntities.TV_BLOCK_ENTITY, TVBlockRenderer::new);

        VideoPlayer.IMG_PAUSED = ImageAPI.renderer(JarTool.readImage(VideoPlayer.class.getClassLoader(), "/pictures/paused.png"), true);

        ArgumentTypesAccessor.fabric_getClassMap().put(SymbolStringArgumentType.class, new SymbolStringArgumentSerializer());
    }

    public static void openVideo(MinecraftClient client, String url, int volume, boolean controlBlocked) {
        client.execute(() -> client.setScreen(new VideoScreen(url, volume, controlBlocked)));
    }

    public static void manageVideo(MinecraftClient client, BlockPos pos, boolean playing, int tick) {
        client.execute(() -> {
            BlockEntity be = client.getInstance().world.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setPlaying(playing);
                tv.setTick(tick);
                if (tv.display != null) {
                    if (playing)
                        tv.display.resume(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
                    else
                        tv.display.pause(tv.getUrl(), tv.volume, tv.minDistance, tv.maxDistance, tv.isPlaying(), tv.loop, tv.getTick());
                }
            }
        });
    }

    public static void openVideoGUI(MinecraftClient client, BlockPos pos, String url, int tick, int volume, boolean loop) {
        client.execute(() -> {
            BlockEntity be = client.world.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setTick(tick);
                tv.volume = volume;
                tv.loop = loop;
                client.setScreen(new TVVideoScreen(be, url, tick, volume, loop));
            }
        });

    }
}

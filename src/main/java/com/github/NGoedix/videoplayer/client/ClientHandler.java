package com.github.NGoedix.videoplayer.client;

import com.github.NGoedix.videoplayer.Reference;
import com.github.NGoedix.videoplayer.VideoPlayerUtils;
import com.github.NGoedix.videoplayer.block.entity.ModBlockEntities;
import com.github.NGoedix.videoplayer.block.entity.custom.RadioBlockEntity;
import com.github.NGoedix.videoplayer.block.entity.custom.TVBlockEntity;
import com.github.NGoedix.videoplayer.client.gui.RadioScreen;
import com.github.NGoedix.videoplayer.client.gui.TVVideoScreen;
import com.github.NGoedix.videoplayer.client.gui.VideoScreen;
import com.github.NGoedix.videoplayer.client.render.TVBlockRenderer;
import com.github.NGoedix.videoplayer.network.PacketHandler;
import com.github.NGoedix.videoplayer.util.RadioStreams;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.watermedia.api.image.ImageAPI;
import org.watermedia.api.image.ImageRenderer;
import org.watermedia.api.player.videolan.MusicPlayer;
import org.watermedia.core.tools.JarTool;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class ClientHandler implements ClientModInitializer {

    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_PAUSED;

    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_STEP10;

    @Environment(EnvType.CLIENT)
    private static ImageRenderer IMG_STEP5;

    @Environment(EnvType.CLIENT)
    public static ImageRenderer pausedImage() { return IMG_PAUSED; }

    @Environment(EnvType.CLIENT)
    public static ImageRenderer step10Image() { return IMG_STEP10; }

    @Environment(EnvType.CLIENT)
    public static ImageRenderer step5Image() { return IMG_STEP5; }

    private static final List<MusicPlayer> musicPlayers = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        Reference.LOGGER.info("Initializing Client");

        if (VideoPlayerUtils.isInstalled("mr_stellarity", "stellarity")) {
            throw new VideoPlayerUtils.UnsupportedModException("mr_stellarity (Stellarity)", "breaks picture rendering, overwrites Minecraft core shaders and isn't possible work around that");
        }

        RadioStreams.prepareRadios();

        PacketHandler.registerS2CPackets();
        BlockEntityRendererRegistry.register(ModBlockEntities.TV_BLOCK_ENTITY, TVBlockRenderer::new);

        IMG_PAUSED = ImageAPI.renderer(JarTool.readImage("/pictures/paused.png"), true);
        IMG_STEP10 = ImageAPI.renderer(JarTool.readImage("/pictures/step10.png"), true);
        IMG_STEP5 = ImageAPI.renderer(JarTool.readImage("/pictures/step5.png"), true);
    }

    public static void openVideo(Minecraft client, String url, int volume, boolean isControlBlocked, boolean canSkip) {
        client.execute(() -> {
            Minecraft.getInstance().setScreen(new VideoScreen(url, volume, isControlBlocked, canSkip, false));
        });
    }

    public static void openVideo(Minecraft client, String url, int volume, boolean isControlBlocked, boolean canSkip, int optionInMode, int optionInSecs, int optionOutMode, int optionOutSecs) {
        client.execute(() -> {
            Minecraft.getInstance().setScreen(new VideoScreen(url, volume, isControlBlocked, canSkip, optionInMode, optionInSecs, optionOutMode, optionOutSecs));
        });
    }

    public static void openRadioGUI(Minecraft client, BlockPos pos, String url, int volume, boolean isPlaying) {
        client.execute(() -> {
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof RadioBlockEntity) {
                RadioBlockEntity tv = (RadioBlockEntity) be;
                tv.setUrl(url);
                tv.setVolume(volume);
                tv.setPlaying(isPlaying);
                Minecraft.getInstance().setScreen(new RadioScreen(be));
            }
        });
    }

    public static void stopVideoIfExists(Minecraft client) {
        client.execute(() -> {
            if (Minecraft.getInstance().screen instanceof VideoScreen screen) {
                screen.onClose();
            }
        });
    }

    public static void playMusic(Minecraft client, String url, int volume) {
        client.execute(() -> {
            // Until any callback in SyncMusicPlayer I will check if the music is playing when added other music player
            for (MusicPlayer musicPlayer : musicPlayers) {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                    musicPlayer.release();
                    musicPlayers.remove(musicPlayer);
                }
            }

            // Add the new player
            MusicPlayer musicPlayer = new MusicPlayer();
            musicPlayers.add(musicPlayer);
            musicPlayer.setVolume(volume);
            musicPlayer.start(URI.create(url));
        });
    }

    public static void stopMusicIfPlaying(Minecraft client) {
        client.execute(() -> {
            for (MusicPlayer musicPlayer : musicPlayers) {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.stop();
                    musicPlayer.release();
                    musicPlayers.remove(musicPlayer);
                }
            }
        });
    }

    public static void openVideoGUI(Minecraft client, BlockPos pos, String url, int volume, int tick, boolean isPlaying) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setTick(tick);
                tv.setVolume(volume);
                tv.setPlaying(isPlaying);
                client.setScreen(new TVVideoScreen(be));
            }
        });
    }

    public static void manageVideo(Minecraft client, String url, BlockPos pos, boolean playing, int tick) {
        client.execute(() -> {
            BlockEntity be = client.level.getBlockEntity(pos);
            if (be instanceof TVBlockEntity tv) {
                tv.setUrl(url);
                tv.setPlaying(playing);
                if (tv.getTick() - 40 > tick || tv.getTick() + 40 < tick)
                    tv.setTick(tick);
                if (tv.requestDisplay() != null) {
                    if (playing)
                        tv.requestDisplay().resume(tv.getTick());
                    else
                        tv.requestDisplay().pause(tv.getTick());
                }
            }
        });
    }

    public static void manageRadio(Minecraft client, String url, BlockPos pos, boolean playing) {
        client.execute(() -> {
            BlockEntity be = Minecraft.getInstance().level.getBlockEntity(pos);
            if (be instanceof RadioBlockEntity tv) {
                tv.setUrl(url);
                tv.setPlaying(playing);
                tv.notifyPlayer();
            }
        });
    }
}

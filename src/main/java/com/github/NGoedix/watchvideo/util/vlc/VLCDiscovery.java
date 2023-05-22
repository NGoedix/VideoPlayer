package com.github.NGoedix.watchvideo.util.vlc;

import com.github.NGoedix.watchvideo.VideoPlayer;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;

import java.lang.reflect.Field;

public class VLCDiscovery {

    private static volatile boolean loaded = false;
    private static volatile boolean startedLoading = false;
    private static volatile boolean successful = false;
    private static volatile NativeDiscovery discovery;
    public static volatile MediaPlayerFactory factory;
    private static Field searchPaths;
    private static Field libraries;

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isLoadedOrRequest() {
        if (loaded)
            return true;
        if (!startedLoading) {
            startedLoading = true;
            new Thread(VLCDiscovery::load).start();
        }
        return false;
    }

    public static boolean isAvailable() {
        return successful;
    }

    public static synchronized boolean load() {
        if (loaded)
            return successful;
        try {

            WindowsNativeDiscoveryStrategyFixed windows = new WindowsNativeDiscoveryStrategyFixed();
            discovery = new NativeDiscovery(new LinuxNativeDiscoveryStrategyFixed(), new MacOsNativeDiscoveryStrategyFixed(), windows) {

                @Override
                protected void onFound(String path, NativeDiscoveryStrategy strategy) {
                    super.onFound(path, strategy);
                }

                @Override
                protected void onFailed(String path, NativeDiscoveryStrategy strategy) {
                    VideoPlayer.LOGGER.info("Failed to load VLC in '{}' stop searching", path);
                    super.onFailed(path, strategy);
                }

                @Override
                protected void onNotFound() {
                    VideoPlayer.LOGGER.info("Could not find VLC in any of the given paths");
                    super.onNotFound();
                }

            };
            successful = discovery.discover();
            loaded = true;
            if (successful) {
                factory = new MediaPlayerFactory("--quiet");
                VideoPlayer.LOGGER.info("Loaded VLC in '{}'", discovery.discoveredPath());
                Runtime.getRuntime().addShutdownHook(new Thread(() -> factory.release()));
            } else
                VideoPlayer.LOGGER.info("Failed to load VLC");
        } catch (Exception e) {
            e.printStackTrace();
            loaded = true;
            successful = false;
            VideoPlayer.LOGGER.error("Failed to load VLC");
        }
        return successful;
    }

}

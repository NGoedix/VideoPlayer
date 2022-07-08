package com.lapaman.watchvideo.util;

import akka.dispatch.sysmsg.Watch;
import com.lapaman.watchvideo.ClientProxy;
import com.lapaman.watchvideo.WatchVideoMod;

import java.awt.image.BufferedImage;
import java.io.File;

public class VideoManager {

    private static boolean startedVideo;

    private static String filename;
    private static double fps;
    private static double duration;
    private static File video;

    private static BufferedImage frame;

    public static void setUp(File video) {
        ClientProxy.openGUI();

        new Thread(() -> {
            WatchVideoMod.getWatchVideoMod().getLogger().info("Setting up the video...");
            VideoManager.filename = video.getName();
            VideoManager.duration = VideoTools.getSecondsDuration(video);
            VideoManager.fps = VideoTools.getFps(video);
            VideoManager.video = video;
            VideoManager.frame = VideoTools.getFrame(video);
            startedVideo = true;
        }).start();
    }

    public static File getVideo() {
        return video;
    }

    public static BufferedImage getFrame() {
        return frame;
    }

    public static boolean isStartedVideo() {
        return startedVideo;
    }
}

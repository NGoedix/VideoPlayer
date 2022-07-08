package com.lapaman.watchvideo.util;

import com.github.kokorin.jaffree.StreamType;
import com.github.kokorin.jaffree.ffmpeg.*;
import com.google.common.util.concurrent.AtomicDouble;
import net.minecraftforge.common.DimensionManager;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class VideoTools {

    public static File getVideo(String filename) {
        return new File(new File(DimensionManager.getCurrentSaveRootDirectory(), "resourcesLapaman"), filename);
    }

    public static double getSecondsDuration(File video) {
        final AtomicLong durationMillis = new AtomicLong();

        FFmpeg.atPath()
                .addInput(
                        UrlInput.fromPath(video.toPath())
                )
                .addOutput(new NullOutput())
                .setProgressListener(progress -> durationMillis.set(progress.getTimeMillis()))
                .execute();

        return (double) (durationMillis.get() / 1000);
    }

    public static double getFps(File video) {
        final AtomicDouble fps = new AtomicDouble();
        FFmpeg.atPath()
                .addInput(
                        UrlInput.fromPath(video.toPath())
                )
                .addOutput(new NullOutput())
                .setProgressListener(progress -> fps.set(progress.getFps()))
                .execute();

        return fps.get();
    }

    public static File getAudio(File video) {
        FFmpeg.atPath()
                .addArguments("-i", "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4")
                .addArgument(System.getenv("TMP") + video.getName() + ".mp3")
                .execute();
        return new File(System.getenv("TMP") + video.getName() + ".mp3");
    }

    public static BufferedImage getFrame(File video) {
        final BufferedImage[] image = {null};

        FFmpeg.atPath()
                .addInput(UrlInput
                        .fromPath(video.toPath())
                )
                .addOutput(FrameOutput
                        .withConsumer(
                                new FrameConsumer() {
                                    @Override
                                    public void consumeStreams(List<Stream> streams) {}

                                    @Override
                                    public void consume(Frame frame) {
                                        // End of Stream
                                        if (frame == null) {
                                            return;
                                        }
                                        image[0] = frame.getImage();
                                    }
                                }
                        )
                        // No more then 1 frames
                        .setFrameCount(StreamType.VIDEO, 1L)
                        .setPosition(1000)
                        // Disable all streams except video
                        .disableStream(StreamType.AUDIO)
                        .disableStream(StreamType.SUBTITLE)
                        .disableStream(StreamType.DATA)
                )
                .execute();

        return image[0];
    }
}

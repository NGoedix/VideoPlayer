package com.lapaman.watchvideo.util;

import com.lapaman.watchvideo.WatchVideoMod;
import com.lapaman.watchvideo.network.PacketHandler;
import com.lapaman.watchvideo.network.message.MessageVideo;
import net.minecraftforge.common.DimensionManager;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.*;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;

import java.io.*;

public class VideoTools {

    public static File getVideo(String filename) {
        return new File(new File(DimensionManager.getCurrentSaveRootDirectory(), "resourcesLapaman"), filename);
    }

    public static void sendImagesFromVideo(File video, double frameRate) {
        try {
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(video));
            Picture picture;
            while (null != (picture = grab.getNativeFrame()))
                PacketHandler.INSTANCE.sendToAll(new MessageVideo(video.getName(), org.jcodec.javase.scale.AWTUtil.toBufferedImage(picture), frameRate));
        } catch (IOException | JCodecException e) {
            e.printStackTrace();
        }
    }

    public static void sendVideo(String name) {
        try {
            File video = VideoTools.getVideo(name);

            Format f = JCodecUtil.detectFormat(video);
            Demuxer d = JCodecUtil.createDemuxer(f, video.getAbsolutePath());
            DemuxerTrack vt = d.getVideoTracks().get(0);
            DemuxerTrackMeta dtm = vt.getMeta();

            double frameRate = dtm.getTotalFrames() / dtm.getTotalDuration();

            WatchVideoMod.getWatchVideoMod().getLogger().info("Sending video " + name + "... Total of " + dtm.getTotalFrames() + " frames and frame rate of " + frameRate + " fps.");
            VideoTools.sendImagesFromVideo(video, frameRate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

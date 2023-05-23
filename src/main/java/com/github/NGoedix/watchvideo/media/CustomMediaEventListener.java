package com.github.NGoedix.watchvideo.media;

import com.github.NGoedix.watchvideo.VideoPlayer;
import nick1st.fancyvideo.api.MediaPlayerHandler;
import uk.co.caprica.vlcj.media.*;
import uk.co.caprica.vlcj.medialist.MediaList;

import java.util.List;

public class CustomMediaEventListener implements MediaEventListener {
    @Override
    public void mediaMetaChanged(Media media, Meta meta) {

    }

    @Override
    public void mediaSubItemAdded(Media media, MediaRef newChild) {
        VideoPlayer.LOGGER.info("item added");
        MediaList mediaList = MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().media().subitems().newMediaList();
        for (String mrl : mediaList.media().mrls()) {
            VideoPlayer.LOGGER.info("mrl=" + mrl);
        }
        mediaList.release();
    }

    @Override
    public void mediaDurationChanged(Media media, long l) {

    }

    @Override
    public void mediaParsedChanged(Media media, MediaParsedStatus mediaParsedStatus) {

    }

    @Override
    public void mediaSubItemTreeAdded(Media media, MediaRef item) {
        VideoPlayer.LOGGER.info("item tree added");
        MediaList mediaList = MediaPlayerHandler.getInstance().getMediaPlayer(VideoPlayer.getResourceLocation()).api().media().subitems().newMediaList();
        for (String mrl : mediaList.media().mrls()) {
            VideoPlayer.LOGGER.info("mrl=" + mrl);
        }
        mediaList.release();
    }

    @Override
    public void mediaThumbnailGenerated(Media media, Picture picture) {

    }

    @Override
    public void mediaAttachedThumbnailsFound(Media media, List<Picture> list) {

    }
}

package com.github.NGoedix.watchvideo.media;


import me.lib720.caprica.vlcj.media.MediaRef;
import me.lib720.caprica.vlcj.media.TrackType;
import me.lib720.caprica.vlcj.player.base.MediaPlayer;
import me.lib720.caprica.vlcj.player.base.MediaPlayerEventListener;
import me.lib720.caprica.vlcj.player.base.TitleDescription;

public abstract class CustomMediaPlayerEventListener implements MediaPlayerEventListener {
    @Override
    public void mediaChanged(MediaPlayer mediaPlayer, MediaRef mediaRef) {

    }

    @Override
    public void positionChanged(MediaPlayer mediaPlayer, double v) {

    }

    @Override
    public void recordChanged(MediaPlayer mediaPlayer, boolean b, String s) {

    }

    @Override
    public void opening(MediaPlayer mediaPlayer) {

    }

    @Override
    public void buffering(MediaPlayer mediaPlayer, float v) {

    }

    @Override
    public void playing(MediaPlayer mediaPlayer) {

    }

    @Override
    public void paused(MediaPlayer mediaPlayer) {

    }

    @Override
    public abstract void stopped(MediaPlayer mediaPlayer);

    @Override
    public void forward(MediaPlayer mediaPlayer) {

    }

    @Override
    public void backward(MediaPlayer mediaPlayer) {

    }

    @Override
    public void stopping(MediaPlayer mediaPlayer) {

    }

    @Override
    public void finished(MediaPlayer mediaPlayer) {

    }

    @Override
    public void timeChanged(MediaPlayer mediaPlayer, long l) {

    }

    @Override
    public void seekableChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void pausableChanged(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void titleListChanged(MediaPlayer mediaPlayer) {

    }

    @Override
    public void titleSelectionChanged(MediaPlayer mediaPlayer, TitleDescription titleDescription, int i) {

    }

    @Override
    public void snapshotTaken(MediaPlayer mediaPlayer, String s) {

    }

    @Override
    public void lengthChanged(MediaPlayer mediaPlayer, long l) {

    }

    @Override
    public void videoOutput(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void elementaryStreamAdded(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {

    }

    @Override
    public void elementaryStreamDeleted(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {

    }

    @Override
    public void elementaryStreamUpdated(MediaPlayer mediaPlayer, TrackType trackType, int i, String s) {

    }

    @Override
    public void elementaryStreamSelected(MediaPlayer mediaPlayer, TrackType trackType, String s, String s1) {

    }

    @Override
    public void corked(MediaPlayer mediaPlayer, boolean b) {

    }

    @Override
    public void muted(MediaPlayer mediaPlayer, boolean b) {

    }

    @Override
    public void volumeChanged(MediaPlayer mediaPlayer, float v) {

    }

    @Override
    public void audioDeviceChanged(MediaPlayer mediaPlayer, String s) {

    }

    @Override
    public void chapterChanged(MediaPlayer mediaPlayer, int i) {

    }


    @Override
    public void programAdded(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void programDeleted(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void programUpdated(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void programSelected(MediaPlayer mediaPlayer, int i, int i1) {

    }

    @Override
    public void error(MediaPlayer mediaPlayer) {

    }

    @Override
    public void mediaPlayerReady(MediaPlayer mediaPlayer) {

    }
}

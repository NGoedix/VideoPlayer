package com.github.NGoedix.watchvideo.util.displayers;

import java.awt.*;

public interface IDisplay {

    int prepare(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    void tick(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    default int maxTick() {
        return 0;
    }

    void pause(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    void resume(String url, float volume, float minDistance, float maxDistance, boolean playing, boolean loop, int tick);

    void release();

    Dimension getDimensions();
}

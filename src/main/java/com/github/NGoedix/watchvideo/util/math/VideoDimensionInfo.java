package com.github.NGoedix.watchvideo.util.math;

public class VideoDimensionInfo {

    private final int width;
    private final int height;
    private final int offsetX;
    private final int offsetY;

    public VideoDimensionInfo(int width, int height, int offsetX, int offsetY) {
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getOffsetX() { return offsetX; }
    public int getOffsetY() { return offsetY; }
}

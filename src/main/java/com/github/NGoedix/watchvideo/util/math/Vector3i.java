package com.github.NGoedix.watchvideo.util.math;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Vector3i {
    private int x;
    private int y;
    private int z;

    public Vector3i(int pX, int pY, int pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}

package com.github.NGoedix.watchvideo.util.math;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Quaternion;

@SideOnly(Side.CLIENT)
public class Vector3f {
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    private float x;
    private float y;
    private float z;

    public Vector3f() {
    }

    public Vector3f(float pX, float pY, float pZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
    }
}

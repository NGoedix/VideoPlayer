package com.github.NGoedix.watchvideo.util.math;


import net.minecraft.client.renderer.Vector3d;
import net.minecraft.util.math.Vec3i;

public class Vec3d extends VecNd<Vec3d> {
    public double x;
    public double y;
    public double z;

    public Vec3d(Vector3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vec3d(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vec3d(double x, double y, double z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    @Override
    public double get(int dim) {
        switch (dim) {
            case 0:
                return x;
            case 1:
                return y;
            case 2:
                return z;
            default:
                return 0;
        }
    }

    @Override
    public double get(Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
            default:
                return 0;
        }
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(Axis axis, double value) {
        switch (axis) {
            case X:
                this.x = value;
                break;
            case Y:
                this.y = value;
                break;
            case Z:
                this.z = value;
                break;
        }
    }

    @Override
    public void set(int dim, double value) {
        switch (dim) {
            case 0:
                this.x = value;
                break;
            case 1:
                this.y = value;
                break;
            case 2:
                this.z = value;
                break;
        }
    }

    @Override
    public int dimensions() {
        return 3;
    }

    @Override
    public void add(Vec3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec3d)
            return ((Vec3d) obj).x == x && ((Vec3d) obj).y == y && ((Vec3d) obj).z == z;
        return false;
    }
    @Override
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public double distance(Vector3d vec) {
        return this.distance(vec.x, vec.y, vec.z);
    }

    public double distance(double x, double y, double z) {
        double posX = this.x - x;
        double posY = this.y - y;
        double posZ = this.z - z;
        return Math.sqrt(posX * posX + posY * posY + posZ * posZ);
    }
}

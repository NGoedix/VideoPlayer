package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class Vec3d extends VecNd<Vec3d> {
    public double x;
    public double y;
    public double z;


    public Vec3d(Vec3i vec) {
        this(vec.getX(), vec.getY(), vec.getZ());
    }

    public Vec3d(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3d vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public double get(int dim) {
        return switch (dim) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> 0.0;
        };
    }

    public double get(Axis axis) {
        return switch (axis) {
            case X -> this.x;
            case Y -> this.y;
            case Z -> this.z;
        };
    }

    public int dimensions() {
        return 3;
    }

    public void add(Vec3d vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3d)) {
            return false;
        } else {
            return ((Vec3d)obj).x == this.x && ((Vec3d)obj).y == this.y && ((Vec3d)obj).z == this.z;
        }
    }

    public double distance(Vec3 vec) {
        return this.distance(vec.x, vec.y, vec.z);
    }

    public double distance(double x, double y, double z) {
        double posX = this.x - x;
        double posY = this.y - y;
        double posZ = this.z - z;
        return Math.sqrt(posX * posX + posY * posY + posZ * posZ);
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }
}

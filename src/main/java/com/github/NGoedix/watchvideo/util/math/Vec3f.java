package com.github.NGoedix.watchvideo.util.math;

public class Vec3f extends VecNf<Vec3f> {
    public float x;
    public float y;
    public float z;


    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vec3f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public float get(Axis axis) {
        return switch (axis) {
            case X -> this.x;
            case Y -> this.y;
            case Z -> this.z;
        };
    }

    public float get(int dim) {
        return switch (dim) {
            case 0 -> this.x;
            case 1 -> this.y;
            case 2 -> this.z;
            default -> 0.0F;
        };
    }

    public int dimensions() {
        return 3;
    }

    public void add(Vec3f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3f)) {
            return false;
        } else {
            return ((Vec3f)obj).x == this.x && ((Vec3f)obj).y == this.y && ((Vec3f)obj).z == this.z;
        }
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public float dot(Vec3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }
}

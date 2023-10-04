package com.github.NGoedix.watchvideo.util.math;

public class Vec3f extends VecNf<Vec3f> {
    public float x;
    public float y;
    public float z;

    public Vec3f(float x, float y, float z) {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(Vec3f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    @Override
    public float get(Axis axis) {
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

    @Override
    public float get(int dim) {
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

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void set(int dim, float value) {
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
    public void set(Axis axis, float value) {
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
    public int dimensions() {
        return 3;
    }

    @Override
    public void add(Vec3f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vec3f)
            return ((Vec3f) obj).x == x && ((Vec3f) obj).y == y && ((Vec3f) obj).z == z;
        return false;
    }

    @Override
    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }
}

package com.github.NGoedix.watchvideo.util.math;


import org.joml.Vector3d;
import org.joml.Vector3f;

public class Vec3f extends VecNf<Vec3f> {
    public float x;
    public float y;
    public float z;

    public Vec3f() {
    }

    public Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3f(Vec3f vec) {
        super(vec);
    }

    public Vec3f(Vector3f vec) {
        this(vec.x(), vec.y(), vec.z());
    }

    public Vector3d toVanilla() {
        return new Vector3d((double)this.x, (double)this.y, (double)this.z);
    }

    public void set(Vec3f vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }

    public float get(Axis axis) {
        switch (axis) {
            case X:
                return this.x;
            case Y:
                return this.y;
            case Z:
                return this.z;
            default:
                return 0.0F;
        }
    }

    public float get(int dim) {
        switch (dim) {
            case 0:
                return this.x;
            case 1:
                return this.y;
            case 2:
                return this.z;
            default:
                return 0.0F;
        }
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

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
        }

    }

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
        }

    }

    public int dimensions() {
        return 3;
    }

    public Vec3f copy() {
        return new Vec3f(this.x, this.y, this.z);
    }

    public void add(Vec3f vec) {
        this.x += vec.x;
        this.y += vec.y;
        this.z += vec.z;
    }

    public void sub(Vec3f vec) {
        this.x -= vec.x;
        this.y -= vec.y;
        this.z -= vec.z;
    }

    public void scale(double scale) {
        this.x = (float)((double)this.x * scale);
        this.y = (float)((double)this.y * scale);
        this.z = (float)((double)this.z * scale);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vec3f)) {
            return false;
        } else {
            return ((Vec3f)obj).x == this.x && ((Vec3f)obj).y == this.y && ((Vec3f)obj).z == this.z;
        }
    }

    public boolean epsilonEquals(Vec3f var1, float var2) {
        float var3 = this.x - var1.x;
        if (Float.isNaN(var3)) {
            return false;
        } else if ((var3 < 0.0F ? -var3 : var3) > var2) {
            return false;
        } else {
            var3 = this.y - var1.y;
            if (Float.isNaN(var3)) {
                return false;
            } else if ((var3 < 0.0F ? -var3 : var3) > var2) {
                return false;
            } else {
                var3 = this.z - var1.z;
                if (Float.isNaN(var3)) {
                    return false;
                } else {
                    return (var3 < 0.0F ? -var3 : var3) <= var2;
                }
            }
        }
    }

    public double distance(Vec3f vec) {
        float x = this.x - vec.x;
        float y = this.y - vec.y;
        float z = this.z - vec.z;
        return Math.sqrt((double)(x * x + y * y + z * z));
    }

    public double distanceSqr(Vec3f vec) {
        float x = this.x - vec.x;
        float y = this.y - vec.y;
        float z = this.z - vec.z;
        return (double)(x * x + y * y + z * z);
    }

    public double length() {
        return Math.sqrt((double)(this.x * this.x + this.y * this.y + this.z * this.z));
    }

    public double lengthSquared() {
        return (double)(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double angle(Vec3f vec) {
        double vDot = (double)this.dot(vec) / (this.length() * vec.length());
        if (vDot < -1.0) {
            vDot = -1.0;
        }

        if (vDot > 1.0) {
            vDot = 1.0;
        }

        return Math.acos(vDot);
    }

    public void cross(Vec3f vec1, Vec3f vec2) {
        this.x = vec1.y * vec2.z - vec1.z * vec2.y;
        this.y = vec2.x * vec1.z - vec2.z * vec1.x;
        this.z = vec1.x * vec2.y - vec1.y * vec2.x;
    }

    public float dot(Vec3f vec) {
        return this.x * vec.x + this.y * vec.y + this.z * vec.z;
    }
}

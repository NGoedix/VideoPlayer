package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

public class AlignedBox {
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    public AlignedBox() {
        this(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public AlignedBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public void add(float x, float y, float z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;
    }

    public void add(Vector3d vec) {
        add((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void add(Vector3i vec) {
        add(vec.getX(), vec.getY(), vec.getZ());
    }

    @Override
    public String toString() {
        return "cube[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public void set(float x, float y, float z, float x2, float y2, float z2) {
        this.minX = Math.min(x, x2);
        this.minY = Math.min(y, y2);
        this.minZ = Math.min(z, z2);
        this.maxX = Math.max(x, x2);
        this.maxY = Math.max(y, y2);
        this.maxZ = Math.max(z, z2);
    }

    public float get(Facing facing) {
        switch (facing) {
            case EAST:
                return maxX;
            case WEST:
                return minX;
            case UP:
                return maxY;
            case DOWN:
                return minY;
            case SOUTH:
                return maxZ;
            case NORTH:
                return minZ;

        }
        return 0;
    }

    public void setMin(Axis axis, float value) {
        switch (axis) {
            case X:
                minX = value;
                break;
            case Y:
                minY = value;
                break;
            case Z:
                minZ = value;
                break;
        }
    }

    public float getMin(Axis axis) {
        switch (axis) {
            case X:
                return minX;
            case Y:
                return minY;
            case Z:
                return minZ;
        }
        return 0;
    }

    public void setMax(Axis axis, float value) {
        switch (axis) {
            case X:
                maxX = value;
                break;
            case Y:
                maxY = value;
                break;
            case Z:
                maxZ = value;
                break;
        }
    }

    public float getMax(Axis axis) {
        switch (axis) {
            case X:
                return maxX;
            case Y:
                return maxY;
            case Z:
                return maxZ;
        }
        return 0;
    }

    public void grow(Axis axis, float value) {
        value /= 2;
        setMin(axis, getMin(axis) - value);
        setMax(axis, getMax(axis) + value);
    }
}
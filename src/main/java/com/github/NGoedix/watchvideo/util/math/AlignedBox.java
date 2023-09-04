package com.github.NGoedix.watchvideo.util.math;

import com.mojang.math.Vector3d;
import net.minecraft.core.Vec3i;

public class AlignedBox {
    public float minX;
    public float minY;
    public float minZ;
    public float maxX;
    public float maxY;
    public float maxZ;

    public AlignedBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public AlignedBox() {
        this(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
        this.add((float)vec.x, (float)vec.y, (float)vec.z);
    }

    public void add(Vec3i vec) {
        this.add((float)vec.getX(), (float)vec.getY(), (float)vec.getZ());
    }

    public String toString() {
        return "cube[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public float get(Facing facing) {
        return switch (facing) {
            case EAST -> this.maxX;
            case WEST -> this.minX;
            case UP -> this.maxY;
            case DOWN -> this.minY;
            case SOUTH -> this.maxZ;
            case NORTH -> this.minZ;
        };
    }

    public void setMin(Axis axis, float value) {
        switch (axis) {
            case X -> this.minX = value;
            case Y -> this.minY = value;
            case Z -> this.minZ = value;
        }

    }

    public float getMin(Axis axis) {
        return switch (axis) {
            case X -> this.minX;
            case Y -> this.minY;
            case Z -> this.minZ;
        };
    }

    public void setMax(Axis axis, float value) {
        switch (axis) {
            case X -> this.maxX = value;
            case Y -> this.maxY = value;
            case Z -> this.maxZ = value;
        }

    }

    public float getMax(Axis axis) {
        return switch (axis) {
            case X -> this.maxX;
            case Y -> this.maxY;
            case Z -> this.maxZ;
        };
    }

    public void grow(Axis axis, float value) {
        value /= 2.0F;
        this.setMin(axis, this.getMin(axis) - value);
        this.setMax(axis, this.getMax(axis) + value);
    }
}
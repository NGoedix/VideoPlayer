package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

public enum Facing {

    DOWN(Axis.Y, false, new Vec3i(0, -1, 0), -1) {

        @Override
        public Facing opposite() {
            return Facing.UP;
        }

        @Override
        public double get(AABB bb) {
            return bb.minY;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.YN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinY(value);
        }

    },
    UP(Axis.Y, true, new Vec3i(0, 1, 0), -1) {

        @Override
        public Facing opposite() {
            return Facing.DOWN;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxY;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.YP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxY(value);
        }

    },
    NORTH(Axis.Z, false, new Vec3i(0, 0, -1), 2) {

        @Override
        public Facing opposite() {
            return SOUTH;
        }

        @Override
        public double get(AABB bb) {
            return bb.minZ;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.ZN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinZ(value);
        }

    },
    SOUTH(Axis.Z, true, new Vec3i(0, 0, 1), 0) {

        @Override
        public Facing opposite() {
            return Facing.NORTH;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxZ;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.ZP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxZ(value);
        }

    },
    WEST(Axis.X, false, new Vec3i(-1, 0, 0), 1) {

        @Override
        public Facing opposite() {
            return Facing.EAST;
        }

        @Override
        public double get(AABB bb) {
            return bb.minX;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.XN;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMinX(value);
        }

    },
    EAST(Axis.X, true, new Vec3i(1, 0, 0), 3) {

        @Override
        public Facing opposite() {
            return Facing.WEST;
        }

        @Override
        public double get(AABB bb) {
            return bb.maxX;
        }

        @Override
        public com.mojang.math.Axis rotation() {
            return com.mojang.math.Axis.XP;
        }

        @Override
        public AABB set(AABB bb, double value) {
            return bb.setMaxX(value);
        }
    };

    public static final Facing[] VALUES = new Facing[] { DOWN, UP, NORTH, SOUTH, WEST, EAST };

    public static Facing get(int index) {
        return VALUES[index];
    }

    public static Facing get(Direction direction) {
        if (direction == null)
            return null;
        return switch (direction) {
            case DOWN -> Facing.DOWN;
            case UP -> Facing.UP;
            case NORTH -> Facing.NORTH;
            case SOUTH -> Facing.SOUTH;
            case WEST -> Facing.WEST;
            case EAST -> Facing.EAST;
        };
    }

    public static Facing get(Axis axis, boolean positive) {
        return switch (axis) {
            case X -> positive ? Facing.EAST : Facing.WEST;
            case Y -> positive ? Facing.UP : Facing.DOWN;
            case Z -> positive ? Facing.SOUTH : Facing.NORTH;
        };
    }

    public final String name;
    public final Axis axis;
    public final boolean positive;
    public final Vec3i normal;
    public final int horizontalIndex;

    Facing(Axis axis, boolean positive, Vec3i normal, int horizontalIndex) {
        this.name = name().toLowerCase();
        this.axis = axis;
        this.positive = positive;
        this.normal = normal;
        this.horizontalIndex = horizontalIndex;
    }

    public abstract Facing opposite();

    public Axis one() {
        return axis.one();
    }

    public Axis two() {
        return axis.two();
    }

    public abstract double get(AABB bb);

    public abstract AABB set(AABB bb, double value);

    public abstract com.mojang.math.Axis rotation();

}

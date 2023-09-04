package com.github.NGoedix.watchvideo.util.math;

import com.mojang.math.Vector3f;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

public enum Facing {
    DOWN(Axis.Y, false, new Vec3i(0, -1, 0), -1) {
        public Facing opposite() {
            return Facing.UP;
        }

        public double get(AABB bb) {
            return bb.minY;
        }

        public Vector3f rotation() {
            return Vector3f.YN;
        }
    },
    UP(Axis.Y, true, new Vec3i(0, 1, 0), -1) {
        public Facing opposite() {
            return Facing.DOWN;
        }

        public double get(AABB bb) {
            return bb.maxY;
        }

        public Vector3f rotation() {
            return Vector3f.YP;
        }
    },
    NORTH(Axis.Z, false, new Vec3i(0, 0, -1), 2) {
        public Facing opposite() {
            return SOUTH;
        }

        public double get(AABB bb) {
            return bb.minZ;
        }

        public Vector3f rotation() {
            return Vector3f.ZN;
        }
    },
    SOUTH(Axis.Z, true, new Vec3i(0, 0, 1), 0) {
        public Facing opposite() {
            return Facing.NORTH;
        }

        public double get(AABB bb) {
            return bb.maxZ;
        }

        public Vector3f rotation() {
            return Vector3f.ZP;
        }
    },
    WEST(Axis.X, false, new Vec3i(-1, 0, 0), 1) {
        public Facing opposite() {
            return Facing.EAST;
        }

        public double get(AABB bb) {
            return bb.minX;
        }

        public Vector3f rotation() {
            return Vector3f.XN;
        }
    },
    EAST(Axis.X, true, new Vec3i(1, 0, 0), 3) {
        public Facing opposite() {
            return Facing.WEST;
        }

        public double get(AABB bb) {
            return bb.maxX;
        }

        public Vector3f rotation() {
            return Vector3f.XP;
        }
    };


    public final String name = this.name().toLowerCase();
    public final Axis axis;
    public final boolean positive;
    public final Vec3i normal;
    public final int horizontalIndex;

    public static Facing get(int index) {
        return switch (index) {
            case 0 -> DOWN;
            case 1 -> UP;
            case 2 -> NORTH;
            case 3 -> SOUTH;
            case 4 -> WEST;
            case 5 -> EAST;
            default -> throw new IllegalArgumentException();
        };
    }

    public static Facing get(Direction direction) {
        if (direction == null) {
            return null;
        } else {

            return switch (direction) {
                case DOWN -> DOWN;
                case UP -> UP;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };
        }
    }

    public static Facing get(Axis axis, boolean positive) {
        return switch (axis) {
            case X -> positive ? EAST : WEST;
            case Y -> positive ? UP : DOWN;
            case Z -> positive ? SOUTH : NORTH;
        };
    }

    Facing(Axis axis, boolean positive, Vec3i normal, int horizontalIndex) {
        this.axis = axis;
        this.positive = positive;
        this.normal = normal;
        this.horizontalIndex = horizontalIndex;
    }

    public abstract Facing opposite();

    public Axis one() {
        return this.axis.one();
    }

    public Axis two() {
        return this.axis.two();
    }


    public abstract double get(AABB var1);

    public abstract Vector3f rotation();
}

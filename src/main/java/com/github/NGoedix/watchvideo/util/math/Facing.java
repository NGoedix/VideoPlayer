package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.util.EnumFacing;

public enum Facing {

    DOWN(Axis.Y, false, new Vector3i(0, -1, 0)) {
        @Override
        public Facing opposite() {
            return Facing.UP;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.YN;
        }
    },
    UP(Axis.Y, true, new Vector3i(0, 1, 0)) {
        @Override
        public Facing opposite() {
            return Facing.DOWN;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.YP;
        }
    },
    NORTH(Axis.Z, false, new Vector3i(0, 0, -1)) {
        @Override
        public Facing opposite() {
            return SOUTH;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.ZN;
        }
    },
    SOUTH(Axis.Z, true, new Vector3i(0, 0, 1)) {
        @Override
        public Facing opposite() {
            return Facing.NORTH;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.ZP;
        }
    },
    WEST(Axis.X, false, new Vector3i(-1, 0, 0)) {
        @Override
        public Facing opposite() {
            return Facing.EAST;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.XN;
        }
    },
    EAST(Axis.X, true, new Vector3i(1, 0, 0)) {
        @Override
        public Facing opposite() {
            return Facing.WEST;
        }
        @Override
        public Vector3f rotation() {
            return Vector3f.XP;
        }
    };

    public static Facing get(int index) {
        switch (index) {
            case 0:
                return Facing.DOWN;
            case 1:
                return Facing.UP;
            case 2:
                return Facing.NORTH;
            case 3:
                return Facing.SOUTH;
            case 4:
                return Facing.WEST;
            case 5:
                return Facing.EAST;
        }
        throw new IllegalArgumentException();
    }

    public static Facing get(EnumFacing direction) {
        if (direction == null) {
            return null;
        } else {
            Facing res = null;
            switch (direction) {
                case DOWN:
                    res = DOWN;
                    break;
                case UP:
                    res = UP;
                    break;
                case NORTH:
                    res = NORTH;
                    break;
                case SOUTH:
                    res = SOUTH;
                    break;
                case WEST:
                    res = WEST;
                    break;
                case EAST:
                    res = EAST;
                    break;
            }

            return res;
        }
    }

    public static Facing get(Axis axis, boolean positive) {
        switch (axis) {
            case X:
                return positive ? Facing.EAST : Facing.WEST;
            case Y:
                return positive ? Facing.UP : Facing.DOWN;
            case Z:
                return positive ? Facing.SOUTH : Facing.NORTH;
        }
        throw new IllegalArgumentException();
    }

    public final String name;
    public final Axis axis;
    public final boolean positive;
    public final Vector3i normal;

    Facing(Axis axis, boolean positive, Vector3i normal) {
        this.name = name().toLowerCase();
        this.axis = axis;
        this.positive = positive;
        this.normal = normal;
    }

    public abstract Facing opposite();

    public Axis one() {
        return axis.one();
    }

    public Axis two() {
        return axis.two();
    }
    public abstract Vector3f rotation();
}

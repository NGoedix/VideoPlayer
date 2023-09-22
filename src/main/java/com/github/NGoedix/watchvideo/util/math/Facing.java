package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;

public enum Facing {

    DOWN(Axis.Y, false, new Vector3i(0, -1, 0)) {
        @Override
        public Facing opposite() {
            return Facing.UP;
        }

        @Override
        public Direction toVanilla() {
            return Direction.DOWN;
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
        public Direction toVanilla() {
            return Direction.UP;
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
        public Direction toVanilla() {
            return Direction.NORTH;
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
        public Direction toVanilla() {
            return Direction.SOUTH;
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
        public Direction toVanilla() {
            return Direction.WEST;
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
        public Direction toVanilla() {
            return Direction.EAST;
        }

        @Override
        public Vector3f rotation() {
            return Vector3f.XP;
        }
    };

    public static final String[] FACING_NAMES = new String[] { "down", "up", "north", "south", "west", "east" };
    public static final String[] HORIZONTAL_FACING_NAMES = new String[] { "north", "south", "west", "east" };

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

    public static Facing get(Direction direction) {
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
            };

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

    public int offset() {
        return positive ? 1 : -1;
    }

    public abstract Facing opposite();

    public abstract Direction toVanilla();

    public Axis one() {
        return axis.one();
    }

    public Axis two() {
        return axis.two();
    }

    public Axis getUAxisFromFacing() {
        switch (axis) {
            case X:
                return Axis.Z;
            case Y:
                return Axis.X;
            case Z:
                return Axis.X;
        }
        return null;
    }

    public Axis getVAxisFromFacing() {
        switch (axis) {
            case X:
                return Axis.Y;
            case Y:
                return Axis.Z;
            case Z:
                return Axis.Y;
        }
        return null;
    }

    public float getUFromFacing(float x, float y, float z) {
        switch (axis) {
            case X:
                return z;
            case Y:
                return x;
            case Z:
                return x;
        }
        return 0;
    }

    public float getVFromFacing(float x, float y, float z) {
        switch (axis) {
            case X:
                return y;
            case Y:
                return z;
            case Z:
                return y;
        }
        return 0;
    }

    public abstract Vector3f rotation();
}

package com.github.NGoedix.videoplayer.util.math;


import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3i;

public enum Facing {
    DOWN(Axis.Y, false, new Vec3i(0, -1, 0), -1) {
        public Facing opposite() {
            return Facing.UP;
        }

        public double get(Box bb) {
            return bb.minY;
        }

        public RotationAxis rotation() {
            return RotationAxis.NEGATIVE_Y;
        }
    },
    UP(Axis.Y, true, new Vec3i(0, 1, 0), -1) {
        public Facing opposite() {
            return Facing.DOWN;
        }

        public double get(Box bb) {
            return bb.maxY;
        }

        public RotationAxis rotation() {
            return RotationAxis.POSITIVE_Y;
        }
    },
    NORTH(Axis.Z, false, new Vec3i(0, 0, -1), 2) {
        public Facing opposite() {
            return SOUTH;
        }

        public double get(Box bb) {
            return bb.minZ;
        }

        public RotationAxis rotation() {
            return RotationAxis.NEGATIVE_Z;
        }
    },
    SOUTH(Axis.Z, true, new Vec3i(0, 0, 1), 0) {
        public Facing opposite() {
            return Facing.NORTH;
        }

        public double get(Box bb) {
            return bb.maxZ;
        }

        public RotationAxis rotation() {
            return RotationAxis.POSITIVE_Z;
        }
    },
    WEST(Axis.X, false, new Vec3i(-1, 0, 0), 1) {
        public Facing opposite() {
            return Facing.EAST;
        }

        public double get(Box bb) {
            return bb.minX;
        }

        public RotationAxis rotation() {
            return RotationAxis.NEGATIVE_X;
        }
    },
    EAST(Axis.X, true, new Vec3i(1, 0, 0), 3) {
        public Facing opposite() {
            return Facing.WEST;
        }

        public double get(Box bb) {
            return bb.maxX;
        }

        public RotationAxis rotation() {
            return RotationAxis.POSITIVE_X;
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
            Facing var10000 = switch (direction) {
                case DOWN -> DOWN;
                case UP -> UP;
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
            };

            return var10000;
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


    public abstract double get(Box var1);

    public abstract RotationAxis rotation();
}

package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.world.phys.AABB;

public enum BoxCorner {
    EUN(Facing.EAST, Facing.UP, Facing.NORTH),
    EUS(Facing.EAST, Facing.UP, Facing.SOUTH),
    EDN(Facing.EAST, Facing.DOWN, Facing.NORTH),
    EDS(Facing.EAST, Facing.DOWN, Facing.SOUTH),
    WUN(Facing.WEST, Facing.UP, Facing.NORTH),
    WUS(Facing.WEST, Facing.UP, Facing.SOUTH),
    WDN(Facing.WEST, Facing.DOWN, Facing.NORTH),
    WDS(Facing.WEST, Facing.DOWN, Facing.SOUTH);

    public final Facing x;
    public final Facing y;
    public final Facing z;
    public BoxCorner neighborOne;
    public BoxCorner neighborTwo;
    public BoxCorner neighborThree;

    BoxCorner(Facing x, Facing y, Facing z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private void init() {
        this.neighborOne = getCorner(this.x.opposite(), this.y, this.z);
        this.neighborTwo = getCorner(this.x, this.y.opposite(), this.z);
        this.neighborThree = getCorner(this.x, this.y, this.z.opposite());
    }

    public boolean isFacing(Facing facing) {
        return this.getFacing(facing.axis) == facing;
    }

    public Facing getFacing(Axis axis) {
        return switch (axis) {
            case X -> this.x;
            case Y -> this.y;
            case Z -> this.z;
        };
    }

    public Vec3d get(AABB bb) {
        return new Vec3d(this.x.get(bb), this.y.get(bb), this.z.get(bb));
    }

    public static BoxCorner getCorner(Facing x, Facing y, Facing z) {
        BoxCorner[] var3 = values();

        for (BoxCorner corner : var3) {
            if (corner.x == x && corner.y == y && corner.z == z) {
                return corner;
            }
        }

        return null;
    }

    static {
        BoxCorner[] var0 = values();

        for (BoxCorner corner : var0) {
            corner.init();
        }
    }
}
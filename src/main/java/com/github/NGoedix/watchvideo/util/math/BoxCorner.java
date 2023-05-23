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
    public static final BoxCorner[][] FACING_CORNERS = new BoxCorner[][]{{EDN, EDS, WDN, WDS}, {EUN, EUS, WUN, WUS}, {EUN, EDN, WUN, WDN}, {EUS, EDS, WUS, WDS}, {WUN, WUS, WDN, WDS}, {EUN, EUS, EDN, EDS}};

    private BoxCorner(Facing x, Facing y, Facing z) {
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

    public boolean isFacingPositive(Axis axis) {
        return this.getFacing(axis).positive;
    }

    public Facing getFacing(Axis axis) {
        switch (axis) {
            case X:
                return this.x;
            case Y:
                return this.y;
            case Z:
                return this.z;
            default:
                throw new RuntimeException("null axis not permitted");
        }
    }

    public BoxCorner mirror(Axis axis) {
        switch (axis) {
            case X:
                return getCorner(this.x.opposite(), this.y, this.z);
            case Y:
                return getCorner(this.x, this.y.opposite(), this.z);
            case Z:
                return getCorner(this.x, this.y, this.z.opposite());
            default:
                throw new RuntimeException("null axis not permitted");
        }
    }

    public Vec3d get(AABB bb) {
        return new Vec3d(this.x.get(bb), this.y.get(bb), this.z.get(bb));
    }

    public static BoxCorner getCornerUnsorted(Facing facing, Facing facing2, Facing facing3) {
        return getCorner(facing.axis != Axis.X ? (facing2.axis != Axis.X ? facing3 : facing2) : facing, facing.axis != Axis.Y ? (facing2.axis != Axis.Y ? facing3 : facing2) : facing, facing.axis != Axis.Z ? (facing2.axis != Axis.Z ? facing3 : facing2) : facing);
    }

    public static BoxCorner getCorner(Facing x, Facing y, Facing z) {
        BoxCorner[] var3 = values();
        int var4 = var3.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            BoxCorner corner = var3[var5];
            if (corner.x == x && corner.y == y && corner.z == z) {
                return corner;
            }
        }

        return null;
    }

    public static BoxCorner[] faceCorners(Facing facing) {
        return FACING_CORNERS[facing.ordinal()];
    }

    static {
        BoxCorner[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            BoxCorner corner = var0[var2];
            corner.init();
        }

    }
}
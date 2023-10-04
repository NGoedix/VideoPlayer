package com.github.NGoedix.watchvideo.util.math;

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
        neighborOne = getCorner(x.opposite(), y, z);
        neighborTwo = getCorner(x, y.opposite(), z);
        neighborThree = getCorner(x, y, z.opposite());
    }

    public boolean isFacing(Facing facing) {
        return getFacing(facing.axis) == facing;
    }

    public Facing getFacing(Axis axis) {
        switch (axis) {
            case X:
                return x;
            case Y:
                return y;
            case Z:
                return z;
        }
        throw new RuntimeException("null axis not permitted");
    }
    public static BoxCorner getCorner(Facing x, Facing y, Facing z) {
        for (BoxCorner corner : BoxCorner.values()) {
            if (corner.x == x && corner.y == y && corner.z == z)
                return corner;
        }
        return null;
    }

    static {
        for (BoxCorner corner : BoxCorner.values())
            corner.init();
    }
}
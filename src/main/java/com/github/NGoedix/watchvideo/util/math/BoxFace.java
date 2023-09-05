package com.github.NGoedix.watchvideo.util.math;

public enum BoxFace {
    EAST(Facing.EAST, new BoxCorner[]{BoxCorner.EUS, BoxCorner.EDS, BoxCorner.EDN, BoxCorner.EUN}, Facing.NORTH, Facing.DOWN),
    WEST(Facing.WEST, new BoxCorner[]{BoxCorner.WUN, BoxCorner.WDN, BoxCorner.WDS, BoxCorner.WUS}, Facing.SOUTH, Facing.DOWN),
    UP(Facing.UP, new BoxCorner[]{BoxCorner.WUN, BoxCorner.WUS, BoxCorner.EUS, BoxCorner.EUN}, Facing.EAST, Facing.SOUTH),
    DOWN(Facing.DOWN, new BoxCorner[]{BoxCorner.WDS, BoxCorner.WDN, BoxCorner.EDN, BoxCorner.EDS}, Facing.EAST, Facing.NORTH),
    SOUTH(Facing.SOUTH, new BoxCorner[]{BoxCorner.WUS, BoxCorner.WDS, BoxCorner.EDS, BoxCorner.EUS}, Facing.EAST, Facing.DOWN),
    NORTH(Facing.NORTH, new BoxCorner[]{BoxCorner.EUN, BoxCorner.EDN, BoxCorner.WDN, BoxCorner.WUN}, Facing.WEST, Facing.DOWN);

    public final Facing facing;
    public final BoxCorner[] corners;
    private final Facing texU;
    private final Facing texV;

    BoxFace(Facing facing, BoxCorner[] corners, Facing texU, Facing texV) {
        this.facing = facing;
        this.corners = corners;
        this.texU = texU;
        this.texV = texV;
    }

    public Facing getTexU() {
        return this.texU;
    }

    public Facing getTexV() {
        return this.texV;
    }

    public Vec3d first(Vec3d[] corners) {
        return corners[this.corners[0].ordinal()];
    }

    public static BoxFace get(Facing facing) {
        return switch (facing) {
            case EAST -> EAST;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
            case SOUTH -> SOUTH;
            case NORTH -> NORTH;
        };
    }

    public static BoxFace get(Axis axis, boolean direction) {
        return switch (axis) {
            case X -> direction ? EAST : WEST;
            case Y -> direction ? UP : DOWN;
            case Z -> direction ? SOUTH : NORTH;
        };
    }
}


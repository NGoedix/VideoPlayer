package com.github.NGoedix.watchvideo.util.math;

public enum Rotation {
    
    X_CLOCKWISE(Axis.X, true) {
    },
    
    X_COUNTER_CLOCKWISE(Axis.X, false) {
    },
    
    Y_CLOCKWISE(Axis.Y, true) {
    },
    Y_COUNTER_CLOCKWISE(Axis.Y, false) {
    },
    
    Z_CLOCKWISE(Axis.Z, true) {
    },
    Z_COUNTER_CLOCKWISE(Axis.Z, false) {
    };
    
    public final Axis axis;
    public final int direction;
    public final boolean clockwise;
    
    Rotation(Axis axis, boolean clockwise) {
        this.axis = axis;
        this.clockwise = clockwise;
        this.direction = clockwise ? 1 : -1;
    }
}

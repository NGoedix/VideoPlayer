package com.github.NGoedix.watchvideo.util.math;

public abstract class VecNf<T extends VecNf> {
    public VecNf() {
    }

    public VecNf(T vec) {
        this.set(vec);
    }

    public abstract void set(T var1);

    public float get(Axis axis) {
        return this.get(axis.ordinal());
    }

    public void set(Axis axis, float value) {
        this.set(axis.ordinal(), value);
    }

    public abstract float get(int var1);

    public abstract void set(int var1, float var2);

    public abstract int dimensions();

    public abstract T copy();

    public abstract void add(T var1);

    public void add(T origin, T vec) {
        this.set(origin);
        this.add(vec);
    }

    public abstract void sub(T var1);

    public void sub(T origin, T vec) {
        this.set(origin);
        this.sub(vec);
    }

    public abstract void scale(double var1);

    public void invert() {
        this.scale(-1.0);
    }

    public abstract boolean equals(Object var1);

    public abstract boolean epsilonEquals(T var1, float var2);

    public boolean epsilonEquals(T vec) {
        return this.epsilonEquals(vec, 1.0E-4F);
    }

    public abstract double distance(T var1);

    public abstract double distanceSqr(T var1);

    public abstract double length();

    public abstract double lengthSquared();

    public void normalize() {
        this.scale(1.0 / this.length());
    }

    public abstract double angle(T var1);

    public abstract float dot(T var1);

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for(int i = 0; i < this.dimensions(); ++i) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(this.get(i));
        }

        builder.append("]");
        return builder.toString();
    }
}

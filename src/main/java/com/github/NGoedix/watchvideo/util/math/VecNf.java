package com.github.NGoedix.watchvideo.util.math;

public abstract class VecNf<T extends VecNf> {
    public VecNf() {
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

    public abstract void add(T var1);

    public void add(T origin, T vec) {
        this.set(origin);
        this.add(vec);
    }

    public abstract boolean equals(Object var1);

    public abstract double length();

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

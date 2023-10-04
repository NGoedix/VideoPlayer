package com.github.NGoedix.watchvideo.util.math;

public abstract class VecNd<T extends VecNd> {
    public VecNd() {

    }

    public abstract void set(T vec);

    public double get(Axis axis) {
        return get(axis.ordinal());
    }

    public void set(Axis axis, double value) {
        set(axis.ordinal(), value);
    }

    public abstract double get(int dim);

    public abstract void set(int dim, double value);

    public abstract int dimensions();

    public abstract void add(T vec);

    public void add(T origin, T vec) {
        set(origin);
        add(vec);
    }

    @Override
    public abstract boolean equals(Object obj);

    public abstract double length();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < dimensions(); i++) {
            if (i > 0)
                builder.append(",");
            builder.append(get(i));
        }
        builder.append("]");
        return builder.toString();
    }
}
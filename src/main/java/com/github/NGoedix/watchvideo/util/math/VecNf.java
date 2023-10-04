package com.github.NGoedix.watchvideo.util.math;

public abstract class VecNf<T extends VecNf> {
    public VecNf() {

    }

    public abstract void set(T vec);

    public float get(Axis axis) {
        return get(axis.ordinal());
    }

    public void set(Axis axis, float value) {
        set(axis.ordinal(), value);
    }

    public abstract float get(int dim);

    public abstract void set(int dim, float value);

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

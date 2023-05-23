package com.github.NGoedix.watchvideo.util.math;

public abstract class VecNd<T extends VecNd> {
    public VecNd() {
    }

    public VecNd(T vec) {
        this.set(vec);
    }

    public abstract void set(T var1);

    public abstract double get(int var1);

    public abstract void set(int var1, double var2);

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

    public abstract boolean epsilonEquals(T var1, double var2);

    public boolean epsilonEquals(T vec) {
        return this.epsilonEquals(vec, 9.999999747378752E-5);
    }

    public abstract double distance(T var1);

    public abstract double distanceSqr(T var1);

    public abstract double length();

    public abstract double lengthSquared();

    public void normalize() {
        this.scale(1.0 / this.length());
    }

    public abstract double angle(T var1);

    public abstract double dot(T var1);

    public long[] toLong() {
        long[] array = new long[this.dimensions()];

        for(int i = 0; i < array.length; ++i) {
            array[i] = Double.doubleToRawLongBits(this.get(i));
        }

        return array;
    }

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
package com.github.NGoedix.watchvideo.util.math;

import net.minecraft.util.Direction;

public enum Axis {
    X {
        public double get(double x, double y, double z) {
            return x;
        }

        public float get(float x, float y, float z) {
            return x;
        }

        public int get(int x, int y, int z) {
            return x;
        }

        public <T> T get(T x, T y, T z) {
            return x;
        }

        public Axis one() {
            return Axis.Y;
        }

        public Axis two() {
            return Axis.Z;
        }
    },
    Y {
        public double get(double x, double y, double z) {
            return y;
        }

        public float get(float x, float y, float z) {
            return y;
        }

        public int get(int x, int y, int z) {
            return y;
        }

        public <T> T get(T x, T y, T z) {
            return y;
        }

        public Axis one() {
            return Axis.Z;
        }

        public Axis two() {
            return Axis.X;
        }
    },
    Z {
        public double get(double x, double y, double z) {
            return z;
        }

        public float get(float x, float y, float z) {
            return z;
        }

        public int get(int x, int y, int z) {
            return z;
        }

        public <T> T get(T x, T y, T z) {
            return z;
        }

        public Axis one() {
            return Axis.X;
        }

        public Axis two() {
            return Axis.Y;
        }
    };

    public static Axis get(Direction.Axis axis) {
        switch (axis) {
            case X:
                return Axis.X;
            case Y:
                return Axis.Y;
            case Z:
                return Axis.Z;
        }
        throw new IllegalArgumentException();
    }

    public abstract Axis one();

    public abstract Axis two();

    public abstract double get(double var1, double var3, double var5);

    public abstract float get(float var1, float var2, float var3);

    public abstract int get(int var1, int var2, int var3);

    public abstract <T> T get(T var1, T var2, T var3);
}
package com.github.NGoedix.videoplayer.util;

public class ByteUtil {
    public static byte setBitAndShift(boolean value, int shift) {
        return (byte) ((value ? 1 : 0) << shift);
    }
}

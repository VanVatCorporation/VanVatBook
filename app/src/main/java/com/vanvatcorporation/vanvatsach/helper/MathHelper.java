package com.vanvatcorporation.vanvatsach.helper;

public class MathHelper {
    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }
}

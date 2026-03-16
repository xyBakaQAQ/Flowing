package com.xybaka.flowing.util;

public final class ColorUtil {
    private ColorUtil() {
    }

    public static int rgba(int red, int green, int blue, int alpha) {
        return (clamp(alpha) << 24) | (clamp(red) << 16) | (clamp(green) << 8) | clamp(blue);
    }

    public static int rgb(int red, int green, int blue) {
        return rgba(red, green, blue, 255);
    }

    public static int withAlpha(int color, int alpha) {
        return (clamp(alpha) << 24) | (color & 0x00FFFFFF);
    }

    public static int getAlpha(int color) {
        return color >> 24 & 0xFF;
    }

    public static int getRed(int color) {
        return color >> 16 & 0xFF;
    }

    public static int getGreen(int color) {
        return color >> 8 & 0xFF;
    }

    public static int getBlue(int color) {
        return color & 0xFF;
    }

    public static int interpolate(int startColor, int endColor, double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        int alpha = interpolateChannel(getAlpha(startColor), getAlpha(endColor), clamped);
        int red = interpolateChannel(getRed(startColor), getRed(endColor), clamped);
        int green = interpolateChannel(getGreen(startColor), getGreen(endColor), clamped);
        int blue = interpolateChannel(getBlue(startColor), getBlue(endColor), clamped);
        return rgba(red, green, blue, alpha);
    }

    public static int rainbow(long delay, float saturation, float brightness) {
        float hue = ((System.currentTimeMillis() + delay) % 3600L) / 3600.0F;
        return java.awt.Color.HSBtoRGB(hue, clamp01(saturation), clamp01(brightness));
    }

    private static int interpolateChannel(int start, int end, double progress) {
        return (int) Math.round(start + (end - start) * progress);
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }
}

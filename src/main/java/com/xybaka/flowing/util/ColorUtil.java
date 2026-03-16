package com.xybaka.flowing.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;

public final class ColorUtil {
    public static final int white = rgb(255, 255, 255);
    public static final int black = rgb(0, 0, 0);
    public static final int gray = rgb(128, 128, 128);
    public static final int lightGray = rgb(192, 192, 192);
    public static final int darkGray = rgb(64, 64, 64);
    public static final int red = rgb(255, 85, 85);
    public static final int orange = rgb(255, 170, 0);
    public static final int yellow = rgb(255, 255, 85);
    public static final int green = rgb(85, 255, 85);
    public static final int aqua = rgb(85, 255, 255);
    public static final int cyan = rgb(85, 205, 252);
    public static final int blue = rgb(85, 85, 255);
    public static final int pink = rgb(255, 85, 255);

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

    public static int multiplyAlpha(int color, double multiplier) {
        return withAlpha(color, (int) Math.round(getAlpha(color) * Math.max(0.0D, Math.min(1.0D, multiplier))));
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

    public static int opaque(int color) {
        return color & 0x00FFFFFF;
    }

    public static TextColor toTextColor(int color) {
        return TextColor.fromRgb(opaque(color));
    }

    public static Style textStyle(int color) {
        return Style.EMPTY.withColor(toTextColor(color));
    }

    public static MutableText literal(String text, int color) {
        return Text.literal(text).setStyle(textStyle(color));
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

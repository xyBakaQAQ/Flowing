package com.xybaka.flowing.util;

import net.minecraft.client.MinecraftClient;

public final class WindowUtil {
    private WindowUtil() {
    }

    public static int getWindowX() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getWindowY() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }

    public static int getCenteredX(int width) {
        return (getWindowX() - width) / 2;
    }

    public static int getCenteredY(int height) {
        return (getWindowY() - height) / 2;
    }
}

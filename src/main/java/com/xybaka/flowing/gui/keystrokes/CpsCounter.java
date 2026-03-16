package com.xybaka.flowing.gui.keystrokes;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CpsCounter {
    private static final long WINDOW_MS = 1000L;
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> RIGHT_CLICKS = new ArrayDeque<>();

    private CpsCounter() {
    }

    public static void registerClick(int button) {
        long now = System.currentTimeMillis();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            LEFT_CLICKS.addLast(now);
            trim(LEFT_CLICKS, now);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            RIGHT_CLICKS.addLast(now);
            trim(RIGHT_CLICKS, now);
        }
    }

    public static int getLeftCps() {
        long now = System.currentTimeMillis();
        trim(LEFT_CLICKS, now);
        return LEFT_CLICKS.size();
    }

    public static int getRightCps() {
        long now = System.currentTimeMillis();
        trim(RIGHT_CLICKS, now);
        return RIGHT_CLICKS.size();
    }

    private static void trim(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > WINDOW_MS) {
            clicks.removeFirst();
        }
    }
}

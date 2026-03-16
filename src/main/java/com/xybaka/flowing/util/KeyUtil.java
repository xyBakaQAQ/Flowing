package com.xybaka.flowing.util;

import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class KeyUtil {
    private static final Map<Integer, String> KEY_NAMES = new HashMap<>();
    private static final Map<String, Integer> KEY_CODES = new HashMap<>();

    static {
        for (Field field : GLFW.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != int.class) {
                continue;
            }

            String name = field.getName();
            if (!name.startsWith("GLFW_KEY_")) {
                continue;
            }

            try {
                int code = field.getInt(null);
                KEY_NAMES.put(code, name);
                KEY_CODES.put(name, code);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private KeyUtil() {
    }

    public static String toConfigKey(int key) {
        return KEY_NAMES.getOrDefault(key, "GLFW_KEY_UNKNOWN");
    }

    public static int fromConfigKey(String keyName) {
        if (keyName == null || keyName.isBlank()) {
            return GLFW.GLFW_KEY_UNKNOWN;
        }

        Integer code = KEY_CODES.get(keyName.toUpperCase(Locale.ROOT));
        return code == null ? GLFW.GLFW_KEY_UNKNOWN : code;
    }

    public static String toDisplayName(int key) {
        return switch (key) {
            case GLFW.GLFW_KEY_UNKNOWN -> "NONE";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACK";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LWIN";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RWIN";
            case GLFW.GLFW_KEY_INSERT -> "INS";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_PAGE_UP -> "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PGDN";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS";
            default -> "K" + key;
        };
    }
}

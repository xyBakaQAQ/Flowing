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
        String configKey = toConfigKey(key);
        return configKey.startsWith("GLFW_KEY_") ? configKey.substring("GLFW_KEY_".length()) : configKey;
    }
}

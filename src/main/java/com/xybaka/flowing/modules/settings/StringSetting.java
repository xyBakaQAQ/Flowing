package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.modules.Module;

import java.util.Objects;

public final class StringSetting extends Setting {
    private final int maxLength;
    private String value;

    public StringSetting(String name, String defaultValue, int maxLength, Module parent) {
        super(name, parent);
        this.maxLength = Math.max(1, maxLength);
        setValue(defaultValue);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        String sanitized = Objects.requireNonNullElse(value, "");
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }

        this.value = sanitized;
        ConfigManager.requestSave();
    }

    public int getMaxLength() {
        return maxLength;
    }
}

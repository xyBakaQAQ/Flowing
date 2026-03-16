package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.modules.Module;

import java.util.Arrays;
import java.util.List;

public final class ModeSetting extends Setting {
    private final List<String> modes;
    private String value;

    public ModeSetting(String name, String defaultMode, Module parent, String... modes) {
        super(name, parent);
        this.modes = Arrays.asList(modes);
        setValue(defaultMode);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        if (!modes.contains(value)) {
            throw new IllegalArgumentException("Unknown mode: " + value);
        }

        this.value = value;
        ConfigManager.requestSave();
    }

    public List<String> getModes() {
        return modes;
    }

    public boolean is(String mode) {
        return value.equalsIgnoreCase(mode);
    }
}

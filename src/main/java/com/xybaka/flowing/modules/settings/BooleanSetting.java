package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.modules.Module;

public final class BooleanSetting extends Setting {
    private boolean value;

    public BooleanSetting(String name, boolean defaultValue, Module parent) {
        super(name, parent);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
        ConfigManager.requestSave();
    }

    public void toggle() {
        setValue(!value);
    }
}

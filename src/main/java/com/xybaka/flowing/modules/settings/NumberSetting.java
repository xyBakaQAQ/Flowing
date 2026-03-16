package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.modules.Module;

public final class NumberSetting extends Setting {
    private final double min;
    private final double max;
    private final double increment;
    private double value;

    public NumberSetting(String name, double defaultValue, double min, double max, double increment, Module parent) {
        super(name, parent);
        this.min = min;
        this.max = max;
        this.increment = increment;
        setValue(defaultValue);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        double clamped = Math.max(min, Math.min(max, value));
        this.value = snap(clamped);
        ConfigManager.requestSave();
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getIncrement() {
        return increment;
    }

    private double snap(double value) {
        if (increment <= 0.0D) {
            return value;
        }

        return Math.round(value / increment) * increment;
    }
}

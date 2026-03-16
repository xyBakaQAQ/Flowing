package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.util.ColorUtil;

public final class ColorSetting extends Setting {
    private final int red;
    private final int green;
    private final int blue;
    private int alpha;

    public ColorSetting(String name, int red, int green, int blue, int alpha, Module parent) {
        super(name, parent);
        this.red = clamp(red);
        this.green = clamp(green);
        this.blue = clamp(blue);
        setAlpha(alpha);
    }

    public int getColor() {
        return ColorUtil.rgba(red, green, blue, alpha);
    }

    public int getPreviewColor() {
        return ColorUtil.rgb(red, green, blue);
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = clamp(alpha);
        ConfigManager.requestSave();
    }

    public double getPercent() {
        return alpha / 255.0D;
    }

    public void setPercent(double percent) {
        setAlpha((int) Math.round(Math.max(0.0D, Math.min(1.0D, percent)) * 255.0D));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}

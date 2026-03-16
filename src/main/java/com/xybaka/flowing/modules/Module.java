package com.xybaka.flowing.modules;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.event.EventListener;
import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.gui.notification.NotificationManager;
import com.xybaka.flowing.gui.notification.NotificationType;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ColorSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;

public abstract class Module implements EventListener {
    private final String name;
    private final Category category;
    private int key;
    private boolean enabled;

    protected Module(String name, Category category, int key) {
        this(name, category, key, false);
    }

    protected Module(String name, Category category, int key, boolean defaultEnabled) {
        this.name = Objects.requireNonNull(name, "name");
        this.category = Objects.requireNonNull(category, "category");
        this.key = key;
        this.enabled = defaultEnabled;
    }

    public final String getName() {
        return name;
    }

    public final Category getCategory() {
        return category;
    }

    public final int getKey() {
        return key;
    }

    public final void setKey(int key) {
        this.key = key;
        ConfigManager.requestSave();
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void enable() {
        setEnabled(true);
    }

    public final void disable() {
        setEnabled(false);
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }

        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }

        if (!ConfigManager.isLoading() && shouldNotifyStateChange()) {
            NotificationManager.push(
                    getName(),
                    enabled ? "Enabled" : "Disabled",
                    enabled ? NotificationType.SUCCESS : NotificationType.ERROR
            );
        }

        ConfigManager.requestSave();
    }

    public final List<Setting> getSettings() {
        List<Setting> settings = new ArrayList<>();

        for (Field field : getClass().getDeclaredFields()) {
            if (!Setting.class.isAssignableFrom(field.getType())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Setting setting = (Setting) field.get(this);
                if (setting != null) {
                    settings.add(setting);
                }
            } catch (IllegalAccessException exception) {
                throw new RuntimeException("Failed to read setting field: " + field.getName(), exception);
            }
        }

        return Collections.unmodifiableList(settings);
    }

    public final boolean hasSettings() {
        return !getSettings().isEmpty();
    }

    protected final BooleanSetting bool(String name, boolean defaultValue) {
        return new BooleanSetting(name, defaultValue, this);
    }

    protected final ModeSetting mode(String name, String defaultMode, String... modes) {
        return new ModeSetting(name, defaultMode, this, modes);
    }

    protected final NumberSetting number(String name, double defaultValue, double min, double max, double increment) {
        return new NumberSetting(name, defaultValue, min, max, increment, this);
    }

    protected final ColorSetting color(String name, int red, int green, int blue, int alpha) {
        return new ColorSetting(name, red, green, blue, alpha, this);
    }

    @Override
    public void onKey(KeyboardEvent event) {
        if (event.getAction() == GLFW_PRESS && event.getKey() == key) {
            toggle();
        }
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    protected boolean shouldNotifyStateChange() {
        return true;
    }
}

package com.xybaka.flowing.gui.clickgui;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;
import com.xybaka.flowing.util.KeyUtil;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public final class ClickGuiManager {
    private static final ClickGuiManager INSTANCE = new ClickGuiManager();

    private static final Comparator<Module> MODULE_NAME_COMPARATOR =
            Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER);

    private final Set<Module> expandedModules = new HashSet<>();
    private Category selectedCategory = Category.RENDER;
    private Module bindingModule;
    private NumberSetting slidingSetting;
    private ModeSetting openModeSetting;

    private ClickGuiManager() {
    }

    public static ClickGuiManager getInstance() {
        return INSTANCE;
    }

    public List<Category> getCategories() {
        return Arrays.asList(Category.values());
    }

    public String getCategoryDisplayName(Category category) {
        return switch (category) {
            case COMBAT -> "Combat";
            case MOVEMENT -> "Movement";
            case PLAYER -> "Flayer";
            case RENDER -> "Render";
            case WORLD -> "World";
            case CLIENT -> "Client";
        };
    }

    public void syncVisibleState() {
        if (!isSettingVisible(openModeSetting)) {
            openModeSetting = null;
        }

        if (!isSettingVisible(slidingSetting)) {
            slidingSetting = null;
        }
    }

    public List<Module> getModules() {
        return ModuleManager.getModules().stream()
                .sorted(MODULE_NAME_COMPARATOR)
                .toList();
    }

    public List<Module> getModules(Category category) {
        return ModuleManager.getModulesByCategory(category).stream()
                .sorted(MODULE_NAME_COMPARATOR)
                .toList();
    }

    public List<Module> getVisibleModules() {
        return getModules(selectedCategory);
    }

    public Optional<Module> getModule(String name) {
        return ModuleManager.getModule(name);
    }

    public List<Setting> getSettings(Module module) {
        return module.getSettings();
    }

    public List<Setting> getVisibleSettings(Module module) {
        syncVisibleState();
        return module.getSettings().stream()
                .filter(Setting::isVisible)
                .toList();
    }

    public boolean hasSettings(Module module) {
        return !getVisibleSettings(module).isEmpty();
    }

    public boolean isEnabled(Module module) {
        return module.isEnabled();
    }

    public void toggle(Module module) {
        module.toggle();
    }

    public int getKey(Module module) {
        return module.getKey();
    }

    public void setKey(Module module, int key) {
        module.setKey(key);
    }

    public String getKeyName(Module module) {
        if (module.getKey() == GLFW_KEY_UNKNOWN) {
            return "";
        }
        return '[' + KeyUtil.toDisplayName(module.getKey()) + ']';
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void selectCategory(Category category) {
        selectedCategory = category;
        bindingModule = null;
        slidingSetting = null;
        openModeSetting = null;
    }

    public boolean isExpanded(Module module) {
        return expandedModules.contains(module);
    }

    public void toggleExpanded(Module module) {
        if (!hasSettings(module)) {
            return;
        }

        if (!expandedModules.add(module)) {
            expandedModules.remove(module);
        }
    }

    public Module getBindingModule() {
        return bindingModule;
    }

    public boolean isBinding(Module module) {
        return bindingModule == module;
    }

    public void beginBinding(Module module) {
        bindingModule = module;
        slidingSetting = null;
        openModeSetting = null;
    }

    public void clearBindingTarget() {
        bindingModule = null;
    }

    public void applyBinding(int key) {
        if (bindingModule == null) {
            return;
        }

        setKey(bindingModule, key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE ? org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN : key);
        bindingModule = null;
    }

    public boolean handleSettingLeftClick(Setting setting) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
            syncVisibleState();
            return true;
        }

        if (setting instanceof ModeSetting modeSetting) {
            openModeSetting = openModeSetting == modeSetting ? null : modeSetting;
            slidingSetting = null;
            syncVisibleState();
            return true;
        }

        if (setting instanceof NumberSetting numberSetting) {
            slidingSetting = numberSetting;
            syncVisibleState();
            return true;
        }

        return false;
    }

    public boolean handleSettingRightClick(Setting setting) {
        if (setting instanceof ModeSetting modeSetting) {
            openModeSetting = openModeSetting == modeSetting ? null : modeSetting;
            slidingSetting = null;
            syncVisibleState();
            return true;
        }

        return false;
    }

    public ModeSetting getOpenModeSetting() {
        return openModeSetting;
    }

    public boolean isModeListOpen(Setting setting) {
        return openModeSetting == setting;
    }

    public void chooseMode(ModeSetting setting, String mode) {
        setting.setValue(mode);
        openModeSetting = setting;
        syncVisibleState();
    }

    public void closeModeList() {
        openModeSetting = null;
    }

    public NumberSetting getSlidingSetting() {
        return slidingSetting;
    }

    public void stopSliding() {
        slidingSetting = null;
    }

    public void updateSliding(NumberSetting setting, double percent) {
        double clamped = Math.max(0.0D, Math.min(1.0D, percent));
        double value = setting.getMin() + (setting.getMax() - setting.getMin()) * clamped;
        setting.setValue(value);
        syncVisibleState();
    }

    public String getSettingLabel(Setting setting) {
        String displayName = getDisplaySettingName(setting);
        if (setting instanceof BooleanSetting booleanSetting) {
            return displayName + ": " + (booleanSetting.getValue() ? "ON" : "OFF");
        }

        if (setting instanceof ModeSetting modeSetting) {
            return displayName + ": " + modeSetting.getValue();
        }

        if (setting instanceof NumberSetting numberSetting) {
            return displayName + ": " + trimNumber(numberSetting.getValue());
        }

        return displayName;
    }

    public double getNumberPercent(NumberSetting setting) {
        double range = setting.getMax() - setting.getMin();
        if (range <= 0.0D) {
            return 0.0D;
        }

        return (setting.getValue() - setting.getMin()) / range;
    }

    private boolean isSettingVisible(Setting setting) {
        return setting != null && setting.isVisible();
    }

    private String trimNumber(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }

        return Double.toString(value);
    }

    private String getDisplaySettingName(Setting setting) {
        String name = setting.getName();
        if (name.startsWith("Attack ")) {
            return name.substring("Attack ".length());
        }
        if (name.startsWith("Vision ")) {
            return name.substring("Vision ".length());
        }
        return name;
    }
}

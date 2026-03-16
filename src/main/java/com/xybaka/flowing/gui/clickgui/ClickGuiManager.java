package com.xybaka.flowing.gui.clickgui;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

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
        return new ArrayList<>(module.getSettings());
    }

    public boolean hasSettings(Module module) {
        return module.hasSettings();
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
        int key = module.getKey();
        if (key == GLFW.GLFW_KEY_UNKNOWN) {
            return "NONE";
        }

        String keyName = GLFW.glfwGetKeyName(key, 0);
        if (keyName != null && !keyName.isBlank()) {
            return keyName.toUpperCase(Locale.ROOT);
        }

        return switch (key) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "L_SHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R_SHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "L_CTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R_CTRL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "L_ALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "R_ALT";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_ESCAPE -> "ESC";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGE_UP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGE_DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            default -> "KEY_" + key;
        };
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
        if (!module.hasSettings()) {
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

        setKey(bindingModule, key == GLFW.GLFW_KEY_ESCAPE ? GLFW.GLFW_KEY_UNKNOWN : key);
        bindingModule = null;
    }

    public boolean handleSettingLeftClick(Setting setting) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.toggle();
            openModeSetting = null;
            return true;
        }

        if (setting instanceof ModeSetting modeSetting) {
            openModeSetting = openModeSetting == modeSetting ? null : modeSetting;
            slidingSetting = null;
            return true;
        }

        if (setting instanceof NumberSetting numberSetting) {
            slidingSetting = numberSetting;
            openModeSetting = null;
            return true;
        }

        return false;
    }

    public boolean handleSettingRightClick(Setting setting) {
        if (setting instanceof ModeSetting modeSetting) {
            openModeSetting = openModeSetting == modeSetting ? null : modeSetting;
            slidingSetting = null;
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
        openModeSetting = null;
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
    }

    public String getSettingLabel(Setting setting) {
        if (setting instanceof BooleanSetting booleanSetting) {
            return setting.getName() + ": " + (booleanSetting.getValue() ? "ON" : "OFF");
        }

        if (setting instanceof ModeSetting modeSetting) {
            return setting.getName() + ": " + modeSetting.getValue() + " v";
        }

        if (setting instanceof NumberSetting numberSetting) {
            return setting.getName() + ": " + trimNumber(numberSetting.getValue());
        }

        return setting.getName();
    }

    public double getNumberPercent(NumberSetting setting) {
        double range = setting.getMax() - setting.getMin();
        if (range <= 0.0D) {
            return 0.0D;
        }

        return (setting.getValue() - setting.getMin()) / range;
    }

    private String trimNumber(double value) {
        if (value == Math.rint(value)) {
            return Integer.toString((int) value);
        }

        return Double.toString(value);
    }
}

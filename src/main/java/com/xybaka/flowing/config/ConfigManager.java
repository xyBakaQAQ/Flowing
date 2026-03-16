package com.xybaka.flowing.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xybaka.flowing.Flowing;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.modules.settings.Setting;
import com.xybaka.flowing.util.KeyUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getGameDir().resolve("Flowing");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("config.json");

    private static boolean loading;
    private static boolean initialized;

    private ConfigManager() {
    }

    public static void init() {
        load();
        initialized = true;
        save();
    }

    public static void requestSave() {
        if (initialized && !loading) {
            save();
        }
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) {
                return;
            }

            loading = true;
            try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!parsed.isJsonObject()) {
                    return;
                }

                JsonObject root = parsed.getAsJsonObject();
                JsonObject modulesObject = root.getAsJsonObject("modules");
                if (modulesObject == null) {
                    return;
                }

                for (Module module : ModuleManager.getModules()) {
                    JsonObject moduleObject = modulesObject.getAsJsonObject(module.getName());
                    if (moduleObject != null) {
                        applyModule(module, moduleObject);
                    }
                }
            }
        } catch (Exception exception) {
            Flowing.LOGGER.error("Failed to load config from {}", CONFIG_FILE, exception);
        } finally {
            loading = false;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);

            JsonObject root = new JsonObject();
            JsonObject modulesObject = new JsonObject();
            for (Module module : ModuleManager.getModules()) {
                modulesObject.add(module.getName(), serializeModule(module));
            }
            root.add("modules", modulesObject);

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(root, writer);
            }
        } catch (IOException exception) {
            Flowing.LOGGER.error("Failed to save config to {}", CONFIG_FILE, exception);
        }
    }

    private static JsonObject serializeModule(Module module) {
        JsonObject moduleObject = new JsonObject();
        moduleObject.addProperty("enabled", module.isEnabled());
        moduleObject.addProperty("key", KeyUtil.toConfigKey(module.getKey()));

        JsonObject settingsObject = new JsonObject();
        for (Setting setting : module.getSettings()) {
            if (setting instanceof BooleanSetting booleanSetting) {
                settingsObject.addProperty(setting.getName(), booleanSetting.getValue());
            } else if (setting instanceof ModeSetting modeSetting) {
                settingsObject.addProperty(setting.getName(), modeSetting.getValue());
            } else if (setting instanceof NumberSetting numberSetting) {
                settingsObject.addProperty(setting.getName(), numberSetting.getValue());
            }
        }

        moduleObject.add("settings", settingsObject);
        return moduleObject;
    }

    private static void applyModule(Module module, JsonObject moduleObject) {
        if (moduleObject.has("key")) {
            JsonElement keyElement = moduleObject.get("key");
            if (keyElement.isJsonPrimitive() && keyElement.getAsJsonPrimitive().isString()) {
                module.setKey(KeyUtil.fromConfigKey(keyElement.getAsString()));
            } else {
                module.setKey(keyElement.getAsInt());
            }
        }

        JsonObject settingsObject = moduleObject.getAsJsonObject("settings");
        if (settingsObject != null) {
            for (Setting setting : module.getSettings()) {
                JsonElement value = settingsObject.get(setting.getName());
                if (value != null) {
                    applySetting(setting, value);
                }
            }
        }

        if (moduleObject.has("enabled")) {
            module.setEnabled(moduleObject.get("enabled").getAsBoolean());
        }
    }

    private static void applySetting(Setting setting, JsonElement value) {
        if (setting instanceof BooleanSetting booleanSetting) {
            booleanSetting.setValue(value.getAsBoolean());
            return;
        }

        if (setting instanceof ModeSetting modeSetting) {
            modeSetting.setValue(value.getAsString());
            return;
        }

        if (setting instanceof NumberSetting numberSetting) {
            numberSetting.setValue(value.getAsDouble());
        }
    }
}

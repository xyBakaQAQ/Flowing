package com.xybaka.flowing.modules;

import com.xybaka.flowing.event.Event;
import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.client.ClickGUI;
import com.xybaka.flowing.modules.movement.Sprint;
import com.xybaka.flowing.modules.render.Camera;
import com.xybaka.flowing.modules.render.FullBright;
import com.xybaka.flowing.modules.render.HUD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public final class ModuleManager {
    private static ModuleManager instance;
    private final Map<Category, List<Module>> modulesByCategory;
    private final Map<String, Module> modulesByName;
    private final List<Module> allModules;

    private ModuleManager() {
        this.modulesByCategory = new EnumMap<>(Category.class);
        this.modulesByName = new HashMap<>();
        this.allModules = new ArrayList<>();

        for (Category category : Category.values()) {
            this.modulesByCategory.put(category, new ArrayList<>());
        }

        initializeModules();
    }

    public static void init() {
        getInstance();
    }

    public static ModuleManager getInstance() {
        if (instance == null) {
            instance = new ModuleManager();
        }
        return instance;
    }

    private void initializeModules() {
        // CLIENT
        registerModule(new ClickGUI());

        // MOVEMENT
        registerModule(new Sprint());

        // RENDER
        registerModule(new HUD());
        registerModule(new Camera());
        registerModule(new FullBright());
    }

    private void registerModule(Module module) {
        String key = normalize(module.getName());
        if (modulesByName.putIfAbsent(key, module) != null) {
            throw new IllegalArgumentException("Duplicate module name: " + module.getName());
        }

        modulesByCategory.get(module.getCategory()).add(module);
        allModules.add(module);
    }

    public static Optional<Module> getModule(String name) {
        return Optional.ofNullable(getInstance().modulesByName.get(normalize(name)));
    }

    public static Module getModuleByName(String name) {
        return getInstance().modulesByName.get(normalize(name));
    }

    public static <T extends Module> T getModule(Class<T> moduleClass) {
        for (Module module : getModules()) {
            if (moduleClass.isInstance(module)) {
                return moduleClass.cast(module);
            }
        }
        return null;
    }

    public static boolean isEnabled(Class<? extends Module> moduleClass) {
        Module module = getModule(moduleClass);
        return module != null && module.isEnabled();
    }

    public static List<Module> getModules() {
        return Collections.unmodifiableList(getInstance().allModules);
    }

    public static List<Module> getEnabledModules() {
        return getInstance().allModules.stream()
                .filter(Module::isEnabled)
                .toList();
    }

    public static List<Module> getModulesByCategory(Category category) {
        return Collections.unmodifiableList(getInstance().modulesByCategory.get(category));
    }

    public static void onTick() {
        post(new TickEvent());
    }

    public static void onKey(int key, int action) {
        if (key == GLFW_KEY_UNKNOWN) {
            return;
        }

        post(new KeyboardEvent(key, action));
    }

    public static void post(Event event) {
        boolean keyboardEvent = event instanceof KeyboardEvent;
        for (Module module : getModules()) {
            if (keyboardEvent || module.isEnabled()) {
                event.call(module);
            }
        }
    }

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}

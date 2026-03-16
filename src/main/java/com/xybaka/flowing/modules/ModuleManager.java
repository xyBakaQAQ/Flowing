package com.xybaka.flowing.modules;

import com.xybaka.flowing.modules.client.ClickGUI;
import com.xybaka.flowing.modules.client.Target;
import com.xybaka.flowing.modules.client.Teams;
import com.xybaka.flowing.modules.combat.Trigger;
import com.xybaka.flowing.modules.movement.Sprint;
import com.xybaka.flowing.modules.player.AutoTool;
import com.xybaka.flowing.modules.player.GhostHand;
import com.xybaka.flowing.modules.player.InventoryHelper;
import com.xybaka.flowing.modules.render.Camera;
import com.xybaka.flowing.modules.render.Cape;
import com.xybaka.flowing.modules.render.ESP;
import com.xybaka.flowing.modules.render.FullBright;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.modules.render.Keystrokes;
import com.xybaka.flowing.modules.render.NameTags;
import com.xybaka.flowing.modules.render.Scoreboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
        // Combat
        registerModule(new Trigger());

        // Movement
        registerModule(new Sprint());

        // Plyaer
        registerModule(new AutoTool());
        registerModule(new InventoryHelper());
        registerModule(new GhostHand());

        // Client
        registerModule(new ClickGUI());
        registerModule(new Target());
        registerModule(new Teams());

        // Render
        registerModule(new HUD());
        registerModule(new Keystrokes());
        registerModule(new NameTags());
        registerModule(new Scoreboard());
        registerModule(new Camera());
        registerModule(new Cape());
        registerModule(new ESP());
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

    private static String normalize(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}

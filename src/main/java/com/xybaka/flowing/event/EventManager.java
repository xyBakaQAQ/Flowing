package com.xybaka.flowing.event;

import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public final class EventManager {
    private EventManager() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> post(new TickEvent()));
    }

    public static void onKey(int key, int action) {
        if (key == GLFW_KEY_UNKNOWN) {
            return;
        }

        post(new KeyboardEvent(key, action));
    }

    public static void post(Event event) {
        boolean keyboardEvent = event instanceof KeyboardEvent;
        for (Module module : ModuleManager.getModules()) {
            if (keyboardEvent || module.isEnabled()) {
                event.call(module);
            }
        }
    }
}

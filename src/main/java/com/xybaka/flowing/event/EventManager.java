package com.xybaka.flowing.event;

import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.event.features.WorldRenderEvent;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

public final class EventManager {
    private static final TickEvent TICK_EVENT = new TickEvent();

    private EventManager() {
    }

    public static void init() {
        WorldRenderEvents.AFTER_ENTITIES.register(EventManager::onWorldRender);
    }

    public static void onTick() {
        post(TICK_EVENT);
    }

    public static void onKey(int key, int action) {
        if (key == GLFW_KEY_UNKNOWN) {
            return;
        }

        post(new KeyboardEvent(key, action));
    }

    public static void onWorldRender(WorldRenderContext context) {
        post(new WorldRenderEvent(context));
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

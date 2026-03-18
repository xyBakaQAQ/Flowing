package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import org.lwjgl.glfw.GLFW;

public final class KeyBinds extends Module {
    public KeyBinds() {
        super("KeyBinds", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}

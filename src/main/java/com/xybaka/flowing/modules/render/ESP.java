package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import org.lwjgl.glfw.GLFW;

public final class ESP extends Module {
    public ESP() {
        super("ESP", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldGlow() {
        return isEnabled();
    }
}

package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import org.lwjgl.glfw.GLFW;

public final class FullBright extends Module {
    public FullBright() {
        super("FullBright", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }
}

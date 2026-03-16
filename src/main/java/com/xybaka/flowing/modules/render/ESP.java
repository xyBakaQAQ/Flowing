package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import org.lwjgl.glfw.GLFW;

public final class ESP extends Module {
    private final ModeSetting mode = mode("Mode", "Glow", "Glow");
    private final BooleanSetting ignoreSelf = bool("IgnoreSelf", true);

    public ESP() {
        super("ESP", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldGlow() {
        return isEnabled() && mode.is("Glow");
    }

    public boolean shouldIgnoreSelf() {
        return ignoreSelf.getValue();
    }
}

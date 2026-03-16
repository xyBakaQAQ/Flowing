package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class Scoreboard extends Module {
    private final BooleanSetting hideNumbers = bool("Hide Numbers", false);

    public Scoreboard() {
        super("Scoreboard", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldHideNumbers() {
        return hideNumbers.getValue();
    }
}

package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class Keystrokes extends Module {
    private final BooleanSetting mouseButtons = bool("Mouse Buttons", true);
    private final BooleanSetting cps = bool("CPS", false)
            .visibleWhen(this::shouldRenderMouseButtons)
            .childOf(mouseButtons);
    private final BooleanSetting space = bool("Space", true);
    private final BooleanSetting shift = bool("Shift", false);

    public Keystrokes() {
        super("Keystrokes", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    public boolean shouldRenderMouseButtons() {
        return mouseButtons.getValue();
    }

    public boolean shouldRenderSpace() {
        return space.getValue();
    }

    public boolean shouldRenderShift() {
        return shift.getValue();
    }

    public boolean shouldRenderCps() {
        return cps.getValue();
    }
}

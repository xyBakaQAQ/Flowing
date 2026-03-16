package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ColorSetting;
import org.lwjgl.glfw.GLFW;

public final class Scoreboard extends Module {
    private final BooleanSetting hideNumbers = bool("Hide Numbers", false);
    private final BooleanSetting titleBackground = bool("Title Background", true);
    private final ColorSetting background = color("Background", 0, 0, 0, 102);

    public Scoreboard() {
        super("Scoreboard", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    public boolean shouldHideNumbers() {
        return hideNumbers.getValue();
    }

    public int getBackgroundColor() {
        return background.getColor();
    }

    public boolean shouldApplyBackgroundToTitle() {
        return titleBackground.getValue();
    }
}

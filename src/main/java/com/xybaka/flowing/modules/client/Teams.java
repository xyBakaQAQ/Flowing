package com.xybaka.flowing.modules.client;

import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

public final class Teams extends Module {
    private final BooleanSetting scoreboard = bool("Scoreboard", true);
    private final BooleanSetting color = bool("Color", true);

    public Teams() {
        super("Teams", Category.CLIENT, GLFW.GLFW_KEY_UNKNOWN);
    }

    public Integer getGlowColor(Entity entity) {
        if (!isEnabled() || entity == null) {
            return null;
        }

        if (scoreboard.getValue()) {
            Integer scoreboardColor = getScoreboardColor(entity);
            if (scoreboardColor != null) {
                return scoreboardColor;
            }
        }

        if (color.getValue()) {
            Integer displayColor = getDisplayColor(entity);
            if (displayColor != null) {
                return displayColor;
            }
        }

        return null;
    }

    private Integer getScoreboardColor(Entity entity) {
        AbstractTeam team = entity.getScoreboardTeam();
        if (team == null) {
            return null;
        }

        Formatting formatting = team.getColor();
        if (formatting == null) {
            return null;
        }

        Integer colorValue = formatting.getColorValue();
        return colorValue == null ? null : colorValue & 0x00FFFFFF;
    }

    private Integer getDisplayColor(Entity entity) {
        TextColor textColor = entity.getDisplayName().getStyle().getColor();
        return textColor == null ? null : textColor.getRgb() & 0x00FFFFFF;
    }
}

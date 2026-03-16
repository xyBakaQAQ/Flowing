package com.xybaka.flowing.modules.client;

import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class Target extends Module {
    private final BooleanSetting players = bool("Players", true);
    private final BooleanSetting mobs = bool("Mobs", true);
    private final BooleanSetting animals = bool("Animals", true);
    private final BooleanSetting invisible = bool("Invisible", false);
    private final BooleanSetting dead = bool("Dead", false);

    public Target() {
        super("Target", Category.CLIENT, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    @Override
    public void onKey(KeyboardEvent event) {
    }

    @Override
    protected void onDisable() {
        enable();
    }

    public boolean players() {
        return players.getValue();
    }

    public boolean mobs() {
        return mobs.getValue();
    }

    public boolean animals() {
        return animals.getValue();
    }

    public boolean invisible() {
        return invisible.getValue();
    }

    public boolean dead() {
        return dead.getValue();
    }
}

package com.xybaka.flowing.modules.client;

import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import org.lwjgl.glfw.GLFW;

public final class Target extends Module {
    private final BooleanSetting attackTarget = bool("AttackTarget", true);
    private final ModeSetting attackMode = mode("Mode", "Distance", "Distance", "Health", "FOV")
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);
    private final BooleanSetting attackPlayers = bool("Attack Players", true)
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);
    private final BooleanSetting attackMobs = bool("Attack Mobs", true)
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);
    private final BooleanSetting attackAnimals = bool("Attack Animals", true)
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);
    private final BooleanSetting attackInvisible = bool("Attack Invisible", false)
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);
    private final BooleanSetting attackDead = bool("Attack Dead", false)
            .visibleWhen(this::attackTarget)
            .childOf(attackTarget);

    private final BooleanSetting visionTarget = bool("VisionTarget", true);
    private final BooleanSetting visionPlayers = bool("Vision Players", true)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);
    private final BooleanSetting visionSelf = bool("Vision Self", false)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);
    private final BooleanSetting visionMobs = bool("Vision Mobs", true)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);
    private final BooleanSetting visionAnimals = bool("Vision Animals", true)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);
    private final BooleanSetting visionInvisible = bool("Vision Invisible", false)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);
    private final BooleanSetting visionDead = bool("Vision Dead", false)
            .visibleWhen(this::visionTarget)
            .childOf(visionTarget);

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

    public boolean attackTarget() {
        return attackTarget.getValue();
    }

    public String attackMode() {
        return attackMode.getValue();
    }

    public boolean attackPlayers() {
        return attackTarget() && attackPlayers.getValue();
    }

    public boolean attackMobs() {
        return attackTarget() && attackMobs.getValue();
    }

    public boolean attackAnimals() {
        return attackTarget() && attackAnimals.getValue();
    }

    public boolean attackInvisible() {
        return attackTarget() && attackInvisible.getValue();
    }

    public boolean attackDead() {
        return attackTarget() && attackDead.getValue();
    }

    public boolean visionTarget() {
        return visionTarget.getValue();
    }

    public boolean visionPlayers() {
        return visionTarget() && visionPlayers.getValue();
    }

    public boolean visionSelf() {
        return visionTarget() && visionSelf.getValue();
    }

    public boolean visionMobs() {
        return visionTarget() && visionMobs.getValue();
    }

    public boolean visionAnimals() {
        return visionTarget() && visionAnimals.getValue();
    }

    public boolean visionInvisible() {
        return visionTarget() && visionInvisible.getValue();
    }

    public boolean visionDead() {
        return visionTarget() && visionDead.getValue();
    }
}

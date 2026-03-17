package com.xybaka.flowing.modules.combat;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.InventoryUtils;
import com.xybaka.flowing.util.MathUtil;
import com.xybaka.flowing.util.TargetUtil;
import com.xybaka.flowing.util.rotation.RotationUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

public final class AimAssist extends Module {
    private final NumberSetting range = number("Range", 4.5D, 1.0D, 8.0D, 0.1D);
    private final NumberSetting fov = number("FOV", 90.0D, 5.0D, 180.0D, 1.0D);
    private final NumberSetting yawSpeed = number("Yaw Speed", 6.0D, 0.1D, 30.0D, 0.1D);
    private final NumberSetting pitchSpeed = number("Pitch Speed", 4.0D, 0.1D, 30.0D, 0.1D);
    private final BooleanSetting weaponOnly = bool("Weapon Only", true);
    private final BooleanSetting holdClick = bool("Hold Click", true);
    private final BooleanSetting adjustPitch = bool("Adjust Pitch", true);
    private final BooleanSetting onlyVisible = bool("Only Visible", true);

    public AimAssist() {
        super("AimAssist", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    public void onTick(TickEvent event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        if (holdClick.getValue() && !mc.options.attackKey.isPressed()) {
            return;
        }

        if (weaponOnly.getValue() && !InventoryUtils.isWeapon(player.getMainHandStack())) {
            return;
        }

        LivingEntity target = TargetUtil.getBestTarget(TargetUtil.Profile.ATTACK, range.getValue());
        if (target == null) {
            return;
        }

        if (onlyVisible.getValue() && !player.canSee(target)) {
            return;
        }

        RotationUtil.RotationDelta rotationDelta = RotationUtil.getRotationDelta(player, target);
        if (Math.abs(rotationDelta.yawDelta()) > fov.getValue()) {
            return;
        }

        float nextYaw = MathUtil.approach(
                player.getYaw(),
                player.getYaw() + rotationDelta.yawDelta(),
                (float) yawSpeed.getValue()
        );
        player.setYaw(nextYaw);

        if (adjustPitch.getValue()) {
            float nextPitch = MathUtil.approach(
                    player.getPitch(),
                    player.getPitch() + rotationDelta.pitchDelta(),
                    (float) pitchSpeed.getValue()
            );
            player.setPitch(MathUtil.clamp(nextPitch, -90.0F, 90.0F));
        }
    }
}

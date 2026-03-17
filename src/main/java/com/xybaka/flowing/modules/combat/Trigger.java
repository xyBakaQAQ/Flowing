package com.xybaka.flowing.modules.combat;

import com.xybaka.flowing.event.features.InputEvent;
import com.xybaka.flowing.mixin.Accessor.MinecraftClientAccessor;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.InventoryUtils;
import com.xybaka.flowing.util.TargetUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public final class Trigger extends Module {
    private static final float PERFECT_ATTACK_THRESHOLD = 1.0F;

    private final NumberSetting range = number("Range", 3.0D, 1.0D, 6.0D, 0.1D);
    private final NumberSetting chance = number("Chance", 100.0D, 0.0D, 100.0D, 1.0D);
    private final BooleanSetting weaponOnly = bool("Weapon Only", true);
    private final BooleanSetting perfectAttack = bool("Perfect Attack", false);
    private final BooleanSetting holdClick = bool("Hold Click", true)
            .visibleWhen(() -> !perfectAttack.getValue());
    private final NumberSetting minCps = number("Min CPS", 8.0D, 1.0D, 20.0D, 1.0D)
            .visibleWhen(() -> !perfectAttack.getValue());
    private final NumberSetting maxCps = number("Max CPS", 12.0D, 1.0D, 20.0D, 1.0D)
            .visibleWhen(() -> !perfectAttack.getValue());

    private long nextAttackAt;

    public Trigger() {
        super("Trigger", Category.COMBAT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    protected void onEnable() {
        nextAttackAt = 0L;
    }

    @Override
    public void onInput(InputEvent event) {
        handleInput(event.getClient());
    }

    public void handleInput(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            return;
        }

        if (client.currentScreen != null) {
            return;
        }

        if (!perfectAttack.getValue() && holdClick.getValue() && !client.options.attackKey.isPressed()) {
            return;
        }

        LivingEntity target = TargetUtil.getCrosshairTarget(TargetUtil.Profile.ATTACK, range.getValue());
        if (target == null) {
            return;
        }

        if (weaponOnly.getValue() && !InventoryUtils.isWeapon(player.getMainHandStack())) {
            return;
        }

        MinecraftClientAccessor accessor = (MinecraftClientAccessor) client;
        if (perfectAttack.getValue()) {
            tryPerfectAttack(player, accessor);
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextAttackAt) {
            return;
        }

        if (tryAttack(accessor)) {
            nextAttackAt = now + randomClickDelay();
        }
    }

    private void tryPerfectAttack(ClientPlayerEntity player, MinecraftClientAccessor accessor) {
        if (player.getAttackCooldownProgress(0.0F) < PERFECT_ATTACK_THRESHOLD) {
            return;
        }

        if (ThreadLocalRandom.current().nextDouble(100.0D) > chance.getValue()) {
            return;
        }

        accessor.flowing$doAttack();
    }

    private boolean tryAttack(MinecraftClientAccessor accessor) {
        if (ThreadLocalRandom.current().nextDouble(100.0D) > chance.getValue()) {
            return false;
        }

        return accessor.flowing$doAttack();
    }

    private long randomClickDelay() {
        int min = (int) Math.round(Math.min(minCps.getValue(), maxCps.getValue()));
        int max = (int) Math.round(Math.max(minCps.getValue(), maxCps.getValue()));
        int cps = ThreadLocalRandom.current().nextInt(min, max + 1);
        return Math.max(1L, Math.round(1000.0D / cps));
    }
}


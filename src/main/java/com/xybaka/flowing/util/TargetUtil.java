package com.xybaka.flowing.util;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.client.Target;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Comparator;
import java.util.stream.StreamSupport;

public final class TargetUtil {
    private TargetUtil() {
    }

    public static LivingEntity getTarget() {
        return getTarget(Double.MAX_VALUE);
    }

    public static LivingEntity getTarget(double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }

        double maxSquaredDistance = range * range;

        return StreamSupport.stream(client.world.getEntities().spliterator(), false)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(target -> isValidTarget(player, target, maxSquaredDistance))
                .min(Comparator.comparingDouble(player::squaredDistanceTo))
                .orElse(null);
    }

    public static boolean isValidTarget(LivingEntity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return false;
        }

        return isValidTarget(player, target, Double.MAX_VALUE);
    }

    public static boolean isValidTarget(LivingEntity target, double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return false;
        }

        return isValidTarget(player, target, range * range);
    }

    private static boolean isValidTarget(ClientPlayerEntity player, LivingEntity target, double maxSquaredDistance) {
        Target module = ModuleManager.getModule(Target.class);
        return target != null
                && target != player
                && isAllowedByDeadSetting(module, target)
                && isAllowedByInvisibleSetting(module, target)
                && !target.isSpectator()
                && player.squaredDistanceTo(target) <= maxSquaredDistance
                && matchesEnabledType(module, target);
    }

    private static boolean matchesEnabledType(Target module, LivingEntity target) {
        if (target instanceof PlayerEntity) {
            return module == null || module.players();
        }

        if (target instanceof HostileEntity) {
            return module == null || module.mobs();
        }

        if (target instanceof AnimalEntity) {
            return module == null || module.animals();
        }

        return false;
    }

    private static boolean isAllowedByInvisibleSetting(Target module, LivingEntity target) {
        return !target.isInvisible() || module == null || module.invisible();
    }

    private static boolean isAllowedByDeadSetting(Target module, LivingEntity target) {
        return target.isAlive() || module == null || module.dead();
    }
}

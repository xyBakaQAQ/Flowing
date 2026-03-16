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
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.StreamSupport;

public final class TargetUtil {
    private static final double TRACKED_VISION_RANGE = 6.0D;
    private static LivingEntity trackedVisionTarget;

    public enum Profile {
        ATTACK,
        VISION
    }

    private TargetUtil() {
    }

    public static LivingEntity getTarget() {
        return getTarget(Profile.ATTACK, Double.MAX_VALUE);
    }

    public static LivingEntity getTarget(double range) {
        return getTarget(Profile.ATTACK, range);
    }

    public static LivingEntity getTarget(Profile profile) {
        return getTarget(profile, Double.MAX_VALUE);
    }

    public static LivingEntity getTarget(Profile profile, double range) {
        LivingEntity crosshairTarget = getCrosshairTarget(profile, range);
        if (crosshairTarget != null) {
            return crosshairTarget;
        }

        return getBestTarget(profile, range);
    }

    public static LivingEntity getCrosshairTarget() {
        return getCrosshairTarget(Profile.VISION, Double.MAX_VALUE);
    }

    public static LivingEntity getCrosshairTarget(double range) {
        return getCrosshairTarget(Profile.VISION, range);
    }

    public static LivingEntity getCrosshairTarget(Profile profile) {
        return getCrosshairTarget(profile, Double.MAX_VALUE);
    }

    public static LivingEntity getCrosshairTarget(Profile profile, double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }

        if (!(client.crosshairTarget instanceof EntityHitResult entityHitResult)) {
            return null;
        }

        Entity entity = entityHitResult.getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }

        return isValidTarget(profile, player, livingEntity, range * range) ? livingEntity : null;
    }

    public static LivingEntity getTrackedVisionTarget() {
        return getTrackedVisionTarget(TRACKED_VISION_RANGE);
    }

    public static LivingEntity getTrackedVisionTarget(double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            trackedVisionTarget = null;
            return null;
        }

        LivingEntity crosshairTarget = getCrosshairTarget(Profile.VISION, range);
        if (crosshairTarget != null) {
            trackedVisionTarget = crosshairTarget;
            return trackedVisionTarget;
        }

        if (!isTrackedVisionTargetValid(player, trackedVisionTarget, range * range)) {
            trackedVisionTarget = null;
        }

        return trackedVisionTarget;
    }

    public static boolean isValidTarget(LivingEntity target) {
        return isValidTarget(Profile.ATTACK, target);
    }

    public static boolean isValidTarget(Profile profile, LivingEntity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return false;
        }

        return isValidTarget(profile, player, target, Double.MAX_VALUE);
    }

    public static boolean isValidTarget(LivingEntity target, double range) {
        return isValidTarget(Profile.ATTACK, target, range);
    }

    public static boolean isValidTarget(Profile profile, LivingEntity target, double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return false;
        }

        return isValidTarget(profile, player, target, range * range);
    }

    public static boolean isVisionRenderTarget(LivingEntity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || target == null) {
            return false;
        }

        Target module = ModuleManager.getModule(Target.class);
        if (target == player) {
            return module != null && module.visionSelf();
        }

        return isValidTarget(Profile.VISION, player, target, Double.MAX_VALUE);
    }

    public static boolean isVisionPlayerRenderTarget(PlayerEntity target) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || target == null || target.isSpectator()) {
            return false;
        }

        Target module = ModuleManager.getModule(Target.class);
        if (target == player) {
            return module != null && module.visionSelf();
        }

        return module != null
                && module.visionPlayers()
                && isAllowedByDeadSetting(Profile.VISION, module, target)
                && isAllowedByInvisibleSetting(Profile.VISION, module, target);
    }

    private static LivingEntity getBestTarget(Profile profile, double range) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return null;
        }

        double maxSquaredDistance = range * range;

        return StreamSupport.stream(client.world.getEntities().spliterator(), false)
                .filter(Objects::nonNull)
                .map(Entity.class::cast)
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .filter(target -> isValidTarget(profile, player, target, maxSquaredDistance))
                .min(getTargetComparator(profile, player))
                .orElse(null);
    }

    private static Comparator<LivingEntity> getTargetComparator(Profile profile, ClientPlayerEntity player) {
        if (profile != Profile.ATTACK) {
            return Comparator.comparingDouble(target -> player.squaredDistanceTo(target));
        }

        Target module = ModuleManager.getModule(Target.class);
        String mode = module == null ? "Distance" : module.attackMode();
        return switch (mode) {
            case "Health" -> Comparator.<LivingEntity>comparingDouble(LivingEntity::getHealth)
                    .thenComparingDouble(target -> player.squaredDistanceTo(target));
            case "FOV" -> Comparator.<LivingEntity>comparingDouble(target -> getFovDistance(player, target))
                    .thenComparingDouble(target -> player.squaredDistanceTo(target));
            default -> Comparator.comparingDouble(target -> player.squaredDistanceTo(target));
        };
    }

    private static double getFovDistance(ClientPlayerEntity player, LivingEntity target) {
        Vec3d look = player.getRotationVec(1.0F).normalize();
        Vec3d toTarget = target.getEyePos().subtract(player.getEyePos()).normalize();
        return 1.0D - look.dotProduct(toTarget);
    }

    private static boolean isValidTarget(Profile profile, ClientPlayerEntity player, LivingEntity target, double maxSquaredDistance) {
        Target module = ModuleManager.getModule(Target.class);
        return target != null
                && target != player
                && isAllowedByDeadSetting(profile, module, target)
                && isAllowedByInvisibleSetting(profile, module, target)
                && !target.isSpectator()
                && player.squaredDistanceTo(target) <= maxSquaredDistance
                && matchesEnabledType(profile, module, target);
    }

    private static boolean isTrackedVisionTargetValid(ClientPlayerEntity player, LivingEntity target, double maxSquaredDistance) {
        return target != null
                && !target.isRemoved()
                && target.getWorld() == player.getWorld()
                && target.isAlive()
                && isValidTarget(Profile.VISION, player, target, maxSquaredDistance);
    }

    private static boolean matchesEnabledType(Profile profile, Target module, LivingEntity target) {
        if (target instanceof PlayerEntity) {
            return module == null || (profile == Profile.ATTACK ? module.attackPlayers() : module.visionPlayers());
        }

        if (target instanceof HostileEntity) {
            return module == null || (profile == Profile.ATTACK ? module.attackMobs() : module.visionMobs());
        }

        if (target instanceof AnimalEntity) {
            return module == null || (profile == Profile.ATTACK ? module.attackAnimals() : module.visionAnimals());
        }

        return false;
    }

    private static boolean isAllowedByInvisibleSetting(Profile profile, Target module, LivingEntity target) {
        if (!target.isInvisible()) {
            return true;
        }

        if (module == null) {
            return true;
        }

        return profile == Profile.ATTACK ? module.attackInvisible() : module.visionInvisible();
    }

    private static boolean isAllowedByDeadSetting(Profile profile, Target module, LivingEntity target) {
        if (target.isAlive()) {
            return true;
        }

        if (module == null) {
            return true;
        }

        return profile == Profile.ATTACK ? module.attackDead() : module.visionDead();
    }
}

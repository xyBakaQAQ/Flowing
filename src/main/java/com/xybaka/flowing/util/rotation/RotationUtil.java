package com.xybaka.flowing.util.rotation;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {
    private RotationUtil() {
    }

    public static Rotation getRotationTo(ClientPlayerEntity player, Entity target) {
        return getRotationTo(player.getEyePos(), target.getPos().add(0.0D, target.getStandingEyeHeight() * 0.9D, 0.0D));
    }

    public static Rotation getRotationTo(ClientPlayerEntity player, Vec3d targetPos) {
        return getRotationTo(player.getEyePos(), targetPos);
    }

    public static Rotation getRotationTo(Vec3d from, Vec3d targetPos) {
        Vec3d delta = targetPos.subtract(from);
        double horizontalDistance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0D);
        float pitch = (float) (-Math.toDegrees(Math.atan2(delta.y, horizontalDistance)));
        return new Rotation(yaw, pitch);
    }

    public static RotationDelta getRotationDelta(ClientPlayerEntity player, Entity target) {
        return getRotationDelta(player.getYaw(), player.getPitch(), getRotationTo(player, target));
    }

    public static RotationDelta getRotationDelta(ClientPlayerEntity player, Vec3d targetPos) {
        return getRotationDelta(player.getYaw(), player.getPitch(), getRotationTo(player, targetPos));
    }

    public static RotationDelta getRotationDelta(float currentYaw, float currentPitch, Rotation targetRotation) {
        return new RotationDelta(
                MathHelper.wrapDegrees(targetRotation.yaw() - currentYaw),
                MathHelper.wrapDegrees(targetRotation.pitch() - currentPitch)
        );
    }

    public record Rotation(float yaw, float pitch) {
    }

    public record RotationDelta(float yawDelta, float pitchDelta) {
    }
}

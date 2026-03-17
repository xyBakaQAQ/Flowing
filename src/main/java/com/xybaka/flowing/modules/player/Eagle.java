package com.xybaka.flowing.modules.player;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.InventoryUtils;
import com.xybaka.flowing.util.KeyUtil;
import com.xybaka.flowing.util.MoveUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public final class Eagle extends Module {
    private static final double EDGE_CHECK_DISTANCE = 0.32D;
    private static final double EDGE_CHECK_DROP = 0.55D;
    private static final double EDGE_CHECK_INSET = 0.05D;

    private final BooleanSetting blocksOnly = bool("Blocks Only", true);
    private final BooleanSetting requiredPitch = bool("Required Pitch", false);
    private final NumberSetting pitch = number("Pitch", 45.0D, 0.0D, 90.0D, 1.0D)
            .visibleWhen(requiredPitch::getValue);

    private boolean forcingSneak;
    private float lastForward;
    private float lastSideways;

    public Eagle() {
        super("Eagle", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    protected void onDisable() {
        releaseSneak();
        clearMovementState();
    }

    @Override
    public void onTick(TickEvent event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null) {
            resetState();
            return;
        }

        if (!player.isOnGround() || player.getAbilities().flying || player.isClimbing() || player.isTouchingWater()) {
            resetState();
            return;
        }

        if (requiredPitch.getValue() && player.getPitch() < pitch.getValue()) {
            resetState();
            return;
        }

        if (blocksOnly.getValue() && !isHoldingBlocks(player)) {
            resetState();
            return;
        }

        boolean moving = MoveUtil.isInputtingMovement(mc);
        boolean pressingMovementKeys = MoveUtil.isPressingMovementKeys(mc);
        if (moving) {
            lastForward = player.input.movementForward;
            lastSideways = player.input.movementSideways;
        } else if (!pressingMovementKeys && !forcingSneak) {
            resetState();
            return;
        }

        if (shouldSneakAtEdge(player, lastForward, lastSideways)) {
            mc.options.sneakKey.setPressed(true);
            forcingSneak = true;
            return;
        }

        resetState();
    }

    private boolean shouldSneakAtEdge(ClientPlayerEntity player, float forward, float sideways) {
        float length = MathHelper.sqrt(forward * forward + sideways * sideways);
        if (length <= 0.0F) {
            return false;
        }

        float normalizedForward = forward / length;
        float normalizedSideways = sideways / length;
        float yawRadians = player.getYaw() * 0.017453292F;

        double offsetX = (-MathHelper.sin(yawRadians) * normalizedForward + MathHelper.cos(yawRadians) * normalizedSideways) * EDGE_CHECK_DISTANCE;
        double offsetZ = (MathHelper.cos(yawRadians) * normalizedForward + MathHelper.sin(yawRadians) * normalizedSideways) * EDGE_CHECK_DISTANCE;

        Box supportBox = player.getBoundingBox()
                .offset(offsetX, -EDGE_CHECK_DROP, offsetZ)
                .contract(EDGE_CHECK_INSET, 0.0D, EDGE_CHECK_INSET);

        return mc.world.isSpaceEmpty(player, supportBox);
    }

    private boolean isHoldingBlocks(ClientPlayerEntity player) {
        return InventoryUtils.isBlock(player.getMainHandStack()) || InventoryUtils.isBlock(player.getOffHandStack());
    }

    private void resetState() {
        releaseSneak();
        clearMovementState();
    }

    private void clearMovementState() {
        lastForward = 0.0F;
        lastSideways = 0.0F;
    }

    private void releaseSneak() {
        if (!forcingSneak) {
            return;
        }

        if (!KeyUtil.isPhysicalKeyPressed(mc, mc.options.sneakKey)) {
            mc.options.sneakKey.setPressed(false);
        }
        forcingSneak = false;
    }
}

package com.xybaka.flowing.modules.player;

import com.xybaka.flowing.event.features.InputEvent;
import com.xybaka.flowing.mixin.Accessor.MinecraftClientAccessor;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.InventoryUtils;
import com.xybaka.flowing.util.KeyUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import java.util.concurrent.ThreadLocalRandom;

public final class AutoPlace extends Module {
    private final BooleanSetting holdRightClick = bool("Hold Right Click", true);
    private final NumberSetting minCps = number("Min CPS", 8.0D, 1.0D, 20.0D, 1.0D);
    private final NumberSetting maxCps = number("Max CPS", 12.0D, 1.0D, 20.0D, 1.0D);

    private long nextUseAt;
    private BlockPos lastTargetPos;
    private Direction lastTargetSide;

    public AutoPlace() {
        super("AutoPlace", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    protected void onEnable() {
        resetState();
    }

    @Override
    protected void onDisable() {
        resetState();
    }

    @Override
    public void onInput(InputEvent event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null || mc.interactionManager == null) {
            resetPlacementTarget();
            return;
        }

        if (holdRightClick.getValue() && !KeyUtil.isPhysicalKeyPressed(mc, mc.options.useKey)) {
            resetPlacementTarget();
            return;
        }

        Hand hand = getBlockHand(player);
        if (hand == null) {
            resetPlacementTarget();
            return;
        }

        if (!(mc.crosshairTarget instanceof BlockHitResult blockHitResult)
                || blockHitResult.getType() == HitResult.Type.MISS) {
            resetPlacementTarget();
            return;
        }

        Direction side = blockHitResult.getSide();
        if (side == Direction.UP || side == Direction.DOWN) {
            resetPlacementTarget();
            return;
        }

        BlockPos targetPos = blockHitResult.getBlockPos();
        BlockState targetState = mc.world.getBlockState(targetPos);
        if (targetState.isAir() || !targetState.getFluidState().isEmpty()) {
            resetPlacementTarget();
            return;
        }

        if (targetPos.equals(lastTargetPos) && side == lastTargetSide) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextUseAt) {
            return;
        }

        ((MinecraftClientAccessor) mc).flowing$doItemUse();
        lastTargetPos = targetPos.toImmutable();
        lastTargetSide = side;
        nextUseAt = now + randomClickDelay();
    }

    private Hand getBlockHand(ClientPlayerEntity player) {
        ItemStack mainHand = player.getMainHandStack();
        if (InventoryUtils.isBlock(mainHand)) {
            return Hand.MAIN_HAND;
        }

        ItemStack offHand = player.getOffHandStack();
        if (InventoryUtils.isBlock(offHand)) {
            return Hand.OFF_HAND;
        }

        return null;
    }

    private long randomClickDelay() {
        int min = (int) Math.round(Math.min(minCps.getValue(), maxCps.getValue()));
        int max = (int) Math.round(Math.max(minCps.getValue(), maxCps.getValue()));
        int cps = ThreadLocalRandom.current().nextInt(min, max + 1);
        return Math.max(1L, Math.round(1000.0D / cps));
    }

    private void resetState() {
        nextUseAt = 0L;
        resetPlacementTarget();
    }

    private void resetPlacementTarget() {
        lastTargetPos = null;
        lastTargetSide = null;
    }
}


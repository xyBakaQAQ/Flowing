package com.xybaka.flowing.modules.player;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.InventoryUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public final class AutoTool extends Module {
    private final BooleanSetting sword = bool("Sword", false);
    private final BooleanSetting switchBack = bool("Switch Back", true);
    private final NumberSetting switchBackDelay = number("Switch Back Delay", 150.0D, 0.0D, 1000.0D, 10.0D)
            .visibleWhen(switchBack::getValue);
    private final BooleanSetting sneakOnly = bool("Sneak Only", true);

    private int previousSlot = -1;
    private long switchBackAt = -1L;

    public AutoTool() {
        super("AutoTool", Category.PLAYER, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    protected void onDisable() {
        previousSlot = -1;
        switchBackAt = -1L;
    }

    @Override
    public void onTick(TickEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.currentScreen != null) {
            resetState();
            return;
        }

        if (sneakOnly.getValue() && !player.isSneaking()) {
            handleRelease(player);
            return;
        }

        if (!client.options.attackKey.isPressed()) {
            handleRelease(player);
            return;
        }

        HitResult hitResult = client.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            handleRelease(player);
            return;
        }

        switchBackAt = -1L;
        if (previousSlot == -1) {
            previousSlot = player.getInventory().selectedSlot;
        }

        int bestSlot = InventoryUtils.getBestToolSlot(player, client.world.getBlockState(blockHitResult.getBlockPos()), sword.getValue());
        if (bestSlot != player.getInventory().selectedSlot) {
            player.getInventory().selectedSlot = bestSlot;
        }
    }

    private void handleRelease(ClientPlayerEntity player) {
        if (previousSlot == -1) {
            switchBackAt = -1L;
            return;
        }

        if (!switchBack.getValue()) {
            resetState();
            return;
        }

        long now = System.currentTimeMillis();
        if (switchBackAt == -1L) {
            switchBackAt = now + Math.round(switchBackDelay.getValue());
            return;
        }

        if (now < switchBackAt) {
            return;
        }

        if (previousSlot >= 0 && previousSlot < 9) {
            player.getInventory().selectedSlot = previousSlot;
        }
        resetState();
    }

    private void resetState() {
        previousSlot = -1;
        switchBackAt = -1L;
    }
}

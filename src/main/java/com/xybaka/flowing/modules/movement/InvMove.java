package com.xybaka.flowing.modules.movement;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.modules.settings.ModeSetting;
import com.xybaka.flowing.util.MoveUtil;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class InvMove extends Module {
    private final ModeSetting mode = mode("Mode", "Normal", "Normal", "Safe", "StopOnAction");
    private final BooleanSetting inventoryOnly = bool("Inventory Only", false);
    private final BooleanSetting passthroughSneak = bool("Passthrough Sneak", false);
    private final BooleanSetting disableSprint = bool("Disable Sprint", true);

    private final List<ClickSlotC2SPacket> packetBuffer = new ArrayList<>();
    private Map<KeyBinding, Integer> movementKeys = Map.of();

    private boolean trackedInputs;
    private boolean wasMoving;

    private static InvMove instance;
    private static boolean bufferPackets;

    public InvMove() {
        super("InvMove", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, false);
    }

    @Override
    protected void onEnable() {
        instance = this;
        trackedInputs = false;
        wasMoving = false;
        bufferPackets = false;
        packetBuffer.clear();
        initializeMovementKeys();
    }

    @Override
    protected void onDisable() {
        MoveUtil.restoreMovementKeys(mc, movementKeys);
        replayPacketBuffer();
        trackedInputs = false;
        wasMoving = false;
        bufferPackets = false;
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.getWindow() == null) {
            resetState(true);
            return;
        }

        initializeMovementKeys();
        if (movementKeys.isEmpty()) {
            return;
        }

        if (!MoveUtil.shouldHandleScreen(mc.currentScreen, inventoryOnly.getValue())) {
            resetState(false);
            return;
        }

        MoveUtil.updateMovementKeys(mc, movementKeys, passthroughSneak.getValue());
        trackedInputs = true;

        if (disableSprint.getValue() && mc.currentScreen instanceof HandledScreen<?>) {
            mc.options.sprintKey.setPressed(false);
            mc.player.setSprinting(false);
        }

        boolean moving = MoveUtil.isMoving(mc, movementKeys, passthroughSneak.getValue());
        boolean handledScreen = MoveUtil.shouldAffectHandledScreen(mc.currentScreen, inventoryOnly.getValue());

        switch (mode.getValue()) {
            case "Safe" -> {
                if (moving && !wasMoving && handledScreen) {
                    sendCloseHandledScreen();
                }
                bufferPackets = false;
            }
            case "StopOnAction" -> {
                bufferPackets = moving && handledScreen;
                if (!moving && wasMoving) {
                    replayPacketBuffer();
                }
            }
            default -> {
                if (moving && !wasMoving && MoveUtil.isPlayerInventoryScreen(mc.currentScreen)) {
                    sendCloseHandledScreen();
                }
                bufferPackets = false;
                if (!packetBuffer.isEmpty()) {
                    replayPacketBuffer();
                }
            }
        }

        wasMoving = moving;
    }

    private void initializeMovementKeys() {
        if (!movementKeys.isEmpty()) {
            return;
        }

        movementKeys = MoveUtil.createMovementKeyMap(mc);
    }

    private void sendCloseHandledScreen() {
        if (mc.getNetworkHandler() == null || !(mc.currentScreen instanceof HandledScreen<?> screen)) {
            return;
        }

        mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(screen.getScreenHandler().syncId));
    }

    private void resetState(boolean clearBuffer) {
        if (trackedInputs) {
            MoveUtil.restoreMovementKeys(mc, movementKeys);
        }

        if (!bufferPackets && !packetBuffer.isEmpty()) {
            replayPacketBuffer();
        }

        trackedInputs = false;
        wasMoving = false;
        bufferPackets = false;

        if (clearBuffer) {
            packetBuffer.clear();
        }
    }

    private boolean isMovingNow() {
        return MoveUtil.shouldHandleScreen(mc.currentScreen, inventoryOnly.getValue())
                && MoveUtil.isMoving(mc, movementKeys, passthroughSneak.getValue());
    }

    private void replayPacketBuffer() {
        if (packetBuffer.isEmpty()) {
            return;
        }

        if (mc.getNetworkHandler() == null) {
            packetBuffer.clear();
            return;
        }

        List<ClickSlotC2SPacket> replay = new ArrayList<>(packetBuffer);
        packetBuffer.clear();
        boolean previous = bufferPackets;
        bufferPackets = false;
        for (ClickSlotC2SPacket packet : replay) {
            mc.getNetworkHandler().sendPacket(packet);
        }
        bufferPackets = previous && shouldBufferClickSlotPacket();
    }

    public static boolean shouldBlockHandledClicks() {
        return instance != null
                && instance.isEnabled()
                && instance.mode.is("Safe")
                && MoveUtil.shouldAffectHandledScreen(instance.mc.currentScreen, instance.inventoryOnly.getValue())
                && instance.isMovingNow();
    }

    public static boolean shouldBufferClickSlotPacket() {
        return instance != null
                && instance.isEnabled()
                && instance.mode.is("StopOnAction")
                && bufferPackets
                && MoveUtil.shouldAffectHandledScreen(instance.mc.currentScreen, instance.inventoryOnly.getValue());
    }

    public static boolean shouldPauseInventoryActions() {
        return instance != null
                && instance.isEnabled()
                && !instance.mode.is("StopOnAction")
                && MoveUtil.shouldAffectHandledScreen(instance.mc.currentScreen, instance.inventoryOnly.getValue())
                && instance.isMovingNow();
    }

    public static boolean isStopOnActionActive() {
        return instance != null
                && instance.isEnabled()
                && instance.mode.is("StopOnAction")
                && MoveUtil.shouldAffectHandledScreen(instance.mc.currentScreen, instance.inventoryOnly.getValue());
    }

    public static void bufferPacket(ClickSlotC2SPacket packet) {
        if (instance != null) {
            instance.packetBuffer.add(packet);
        }
    }
}

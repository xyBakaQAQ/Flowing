package com.xybaka.flowing.util;

import com.xybaka.flowing.gui.clickgui.ClickGuiScreen;
import com.xybaka.flowing.mixin.Accessor.KeyBindingAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MoveUtil {
    private MoveUtil() {
    }

    public static Map<KeyBinding, Integer> createMovementKeyMap(MinecraftClient client) {
        Map<KeyBinding, Integer> movementKeys = new LinkedHashMap<>();
        if (client.options == null) {
            return movementKeys;
        }

        addMovementKey(movementKeys, client.options.forwardKey);
        addMovementKey(movementKeys, client.options.backKey);
        addMovementKey(movementKeys, client.options.leftKey);
        addMovementKey(movementKeys, client.options.rightKey);
        addMovementKey(movementKeys, client.options.jumpKey);
        addMovementKey(movementKeys, client.options.sneakKey);
        return movementKeys;
    }

    public static void updateMovementKeys(MinecraftClient client, Map<KeyBinding, Integer> movementKeys, boolean passthroughSneak) {
        if (client.getWindow() == null || client.options == null) {
            return;
        }

        Window window = client.getWindow();
        long handle = window.getHandle();
        for (Map.Entry<KeyBinding, Integer> entry : movementKeys.entrySet()) {
            KeyBinding keyBinding = entry.getKey();
            if (keyBinding == client.options.sneakKey && !passthroughSneak) {
                keyBinding.setPressed(false);
                continue;
            }

            keyBinding.setPressed(InputUtil.isKeyPressed(handle, entry.getValue()));
        }
    }

    public static void restoreMovementKeys(MinecraftClient client, Map<KeyBinding, Integer> movementKeys) {
        if (client == null || client.getWindow() == null) {
            return;
        }

        Window window = client.getWindow();
        long handle = window.getHandle();
        for (Map.Entry<KeyBinding, Integer> entry : movementKeys.entrySet()) {
            entry.getKey().setPressed(InputUtil.isKeyPressed(handle, entry.getValue()));
        }
    }

    public static boolean isMoving(MinecraftClient client, Map<KeyBinding, Integer> movementKeys, boolean passthroughSneak) {
        if (client.options == null) {
            return false;
        }

        for (Map.Entry<KeyBinding, Integer> entry : movementKeys.entrySet()) {
            KeyBinding keyBinding = entry.getKey();
            if (keyBinding == client.options.sneakKey && !passthroughSneak) {
                continue;
            }

            if (keyBinding.isPressed()) {
                return true;
            }
        }

        return false;
    }

    public static boolean shouldHandleScreen(Screen screen, boolean inventoryOnly) {
        if (screen == null || screen instanceof ChatScreen) {
            return false;
        }

        if (screen instanceof ClickGuiScreen) {
            return true;
        }

        if (!(screen instanceof HandledScreen<?>)) {
            return !inventoryOnly;
        }

        if (!inventoryOnly) {
            return true;
        }

        return isPlayerInventoryScreen(screen);
    }

    public static boolean shouldAffectHandledScreen(Screen screen, boolean inventoryOnly) {
        if (!(screen instanceof HandledScreen<?>)) {
            return false;
        }

        if (!inventoryOnly) {
            return true;
        }

        return isPlayerInventoryScreen(screen);
    }

    public static boolean isPlayerInventoryScreen(Screen screen) {
        return screen instanceof InventoryScreen || screen instanceof CreativeInventoryScreen;
    }

    public static boolean isMovingForward(MinecraftClient client) {
        return client.player != null && client.player.input.movementForward > 0.0F;
    }

    public static boolean isInputtingMovement(MinecraftClient client) {
        return client.player != null
                && client.player.input != null
                && (Math.abs(client.player.input.movementForward) > 0.01F
                || Math.abs(client.player.input.movementSideways) > 0.01F);
    }

    public static boolean isPressingMovementKeys(MinecraftClient client) {
        return client.options != null && (
                client.options.forwardKey.isPressed()
                        || client.options.backKey.isPressed()
                        || client.options.leftKey.isPressed()
                        || client.options.rightKey.isPressed()
        );
    }

    private static void addMovementKey(Map<KeyBinding, Integer> movementKeys, KeyBinding keyBinding) {
        InputUtil.Key boundKey = ((KeyBindingAccessor) keyBinding).flowing$getBoundKey();
        if (boundKey == null || boundKey.getCategory() != InputUtil.Type.KEYSYM) {
            return;
        }

        int keyCode = KeyUtil.fromConfigKey(KeyUtil.toConfigKey(boundKey.getCode()));
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN) {
            return;
        }

        movementKeys.put(keyBinding, keyCode);
    }
}

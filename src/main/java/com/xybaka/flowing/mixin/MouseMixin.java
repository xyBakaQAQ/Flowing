package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.component.HudSnapHelper;
import com.xybaka.flowing.gui.inventory.InventoryRenderer;
import com.xybaka.flowing.gui.keystrokes.CpsCounter;
import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.gui.scoreboard.ScoreboardRenderer;
import com.xybaka.flowing.gui.targethud.TargetHudRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.modules.render.Keystrokes;
import com.xybaka.flowing.modules.render.Scoreboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void flowing$dragHudComponents(long window, double x, double y, CallbackInfo ci) {
        if (!(client.currentScreen instanceof ChatScreen)) {
            HudSnapHelper.clearGuides();
            return;
        }

        Window clientWindow = client.getWindow();
        if (window != clientWindow.getHandle()) {
            return;
        }

        double scaledX = x * clientWindow.getScaledWidth() / clientWindow.getWidth();
        double scaledY = y * clientWindow.getScaledHeight() / clientWindow.getHeight();
        boolean snap = isShiftDown(clientWindow);

        Keystrokes keystrokes = ModuleManager.getModule(Keystrokes.class);
        if (keystrokes != null && keystrokes.isEnabled() && KeystrokesRenderer.isDragging() && KeystrokesRenderer.drag(keystrokes, scaledX, scaledY, snap)) {
            ci.cancel();
            return;
        }

        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if (scoreboard != null && scoreboard.isEnabled() && ScoreboardRenderer.isDragging() && ScoreboardRenderer.drag(scaledX, scaledY, snap)) {
            ci.cancel();
            return;
        }

        HUD hud = ModuleManager.getModule(HUD.class);
        if (hud == null || !hud.isEnabled()) {
            HudSnapHelper.clearGuides();
            return;
        }

        if (hud.shouldRenderTargetHud() && TargetHudRenderer.isDragging() && TargetHudRenderer.drag(hud, scaledX, scaledY, snap)) {
            ci.cancel();
            return;
        }

        if (hud.shouldRenderInventory() && InventoryRenderer.isDragging() && InventoryRenderer.drag(hud, scaledX, scaledY, snap)) {
            ci.cancel();
            return;
        }

        HudSnapHelper.clearGuides();
    }

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void flowing$handleMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        if (window != client.getWindow().getHandle()) {
            return;
        }

        if (action == GLFW.GLFW_PRESS) {
            CpsCounter.registerClick(button);
        }

        if (client.currentScreen instanceof ChatScreen
                && button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                && action == GLFW.GLFW_RELEASE) {
            KeystrokesRenderer.stopDragging();
            ScoreboardRenderer.stopDragging();
            TargetHudRenderer.stopDragging();
            InventoryRenderer.stopDragging();
            HudSnapHelper.clearGuides();
        }
    }

    private boolean isShiftDown(Window clientWindow) {
        long handle = clientWindow.getHandle();
        return InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT)
                || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
    }
}

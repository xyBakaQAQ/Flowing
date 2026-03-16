package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.keystrokes.CpsCounter;
import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
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
    private void flowing$dragKeystrokes(long window, double x, double y, CallbackInfo ci) {
        if (!(client.currentScreen instanceof ChatScreen)) {
            return;
        }

        HUD hud = ModuleManager.getModule(HUD.class);
        if (hud == null || !hud.isEnabled() || !hud.shouldRenderKeystrokes() || !KeystrokesRenderer.isDragging()) {
            return;
        }

        Window clientWindow = client.getWindow();
        if (window != clientWindow.getHandle()) {
            return;
        }

        double scaledX = x * clientWindow.getScaledWidth() / clientWindow.getWidth();
        double scaledY = y * clientWindow.getScaledHeight() / clientWindow.getHeight();
        if (KeystrokesRenderer.drag(hud, scaledX, scaledY)) {
            ci.cancel();
        }
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
        }
    }
}

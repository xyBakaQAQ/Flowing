package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void flowing$dragKeystrokesClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        HUD hud = ModuleManager.getModule(HUD.class);
        if (hud == null || !hud.isEnabled() || !hud.shouldRenderKeystrokes()) {
            return;
        }

        if (KeystrokesRenderer.beginDragging(hud, mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }
}

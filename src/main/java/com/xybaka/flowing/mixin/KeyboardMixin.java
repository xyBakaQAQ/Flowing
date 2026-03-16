package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onKey", at = @At("TAIL"))
    private void flowing$handleModuleKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if (window != this.client.getWindow().getHandle()) {
            return;
        }

        if (this.client.currentScreen != null) {
            return;
        }

        ModuleManager.onKey(key, action);
    }
}
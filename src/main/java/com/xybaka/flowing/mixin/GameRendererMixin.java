package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void flowing$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        if (module != null && module.noHurtCam()) {
            ci.cancel();
        }
    }
}

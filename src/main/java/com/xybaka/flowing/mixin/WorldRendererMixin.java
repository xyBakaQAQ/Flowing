package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(method = "hasBlindnessOrDarkness", at = @At("HEAD"), cancellable = true)
    private void flowing$noBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        if (module != null && module.noBlindness()) {
            cir.setReturnValue(false);
        }
    }
}

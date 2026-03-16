package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Camera.class)
public class CameraMixin {
    @Inject(method = "clipToSpace", at = @At("HEAD"), cancellable = true)
    private void flowing$noClipCamera(float desiredCameraDistance, CallbackInfoReturnable<Float> cir) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        if (module != null && module.cameraNoClip()) {
            cir.setReturnValue(desiredCameraDistance);
        }
    }
}
package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(InGameOverlayRenderer.class)
public class InGameOverlayRendererMixin {
    private static final float FLOWING_LOW_FIRE_TRANSLATE_Y = -0.55F;

    @ModifyArg(
            method = "renderFireOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"),
            index = 1
    )
    private static float flowing$lowerFireOverlay(float originalY) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        return module != null && module.lowFireOverlay() ? FLOWING_LOW_FIRE_TRANSLATE_Y : originalY;
    }
}

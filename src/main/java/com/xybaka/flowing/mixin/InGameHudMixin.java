package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    private void flowing$renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HUD hud = ModuleManager.getModule(HUD.class);
        if (hud != null && hud.isEnabled()) {
            hud.render(context);
            if (hud.shouldRenderKeystrokes()) {
                KeystrokesRenderer.render(context, hud);
            }
        }
    }

    @Inject(method = "renderNauseaOverlay", at = @At("HEAD"), cancellable = true)
    private void flowing$noNausea(DrawContext context, float nauseaStrength, CallbackInfo ci) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        if (module != null && module.noNausea()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
    private void flowing$noPumpkinOverlay(DrawContext context, Identifier texture, float opacity, CallbackInfo ci) {
        com.xybaka.flowing.modules.render.Camera module = ModuleManager.getModule(com.xybaka.flowing.modules.render.Camera.class);
        if (module != null && module.noPumpkinOverlay() && texture.getPath().contains("pumpkinblur")) {
            ci.cancel();
        }
    }
}

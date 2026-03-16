package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.FullBright;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;",
                    ordinal = 1
            )
    )
    private Object flowing$redirectGamma(SimpleOption<?> option) {
        if (ModuleManager.isEnabled(FullBright.class)) {
            return 15.0D;
        }

        return option.getValue();
    }
}

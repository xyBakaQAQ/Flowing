package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.Cape;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public class PlayerListEntryMixin {
    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void flowing$overrideCape(CallbackInfoReturnable<SkinTextures> cir) {
        Cape module = ModuleManager.getModule(Cape.class);
        if (module == null || !module.shouldUseCape()) {
            return;
        }

        SkinTextures skinTextures = cir.getReturnValue();
        if (!module.shouldOverrideCape() && skinTextures.capeTexture() != null) {
            return;
        }

        cir.setReturnValue(new SkinTextures(
                skinTextures.texture(),
                skinTextures.textureUrl(),
                module.getCapeTexture(),
                skinTextures.elytraTexture(),
                skinTextures.model(),
                skinTextures.secure()
        ));
    }
}

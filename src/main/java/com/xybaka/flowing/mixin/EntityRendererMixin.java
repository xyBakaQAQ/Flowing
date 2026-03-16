package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.NameTags;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {
    @Inject(method = "hasLabel", at = @At("HEAD"), cancellable = true)
    private void flowing$suppressVanillaPlayerLabels(T entity, double squaredDistanceToCamera, CallbackInfoReturnable<Boolean> cir) {
        NameTags module = ModuleManager.getModule(NameTags.class);
        if (module != null && module.isEnabled() && entity instanceof PlayerEntity) {
            cir.setReturnValue(false);
        }
    }
}

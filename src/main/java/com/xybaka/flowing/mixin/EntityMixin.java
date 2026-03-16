package com.xybaka.flowing.mixin;

import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.client.Teams;
import com.xybaka.flowing.modules.render.ESP;
import com.xybaka.flowing.util.TargetUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void flowing$espGlow(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        ESP module = ModuleManager.getModule(ESP.class);
        if (!shouldApplyGlow(module, entity)) {
            return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "getTeamColorValue", at = @At("HEAD"), cancellable = true)
    private void flowing$espGlowTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity entity = (Entity) (Object) this;
        ESP esp = ModuleManager.getModule(ESP.class);
        if (!shouldApplyGlow(esp, entity)) {
            return;
        }

        Teams teams = ModuleManager.getModule(Teams.class);
        if (teams == null) {
            return;
        }

        Integer color = teams.getGlowColor(entity);
        if (color != null) {
            cir.setReturnValue(color);
        }
    }

    private boolean shouldApplyGlow(ESP module, Entity entity) {
        return module != null
                && module.shouldGlow()
                && entity instanceof LivingEntity livingEntity
                && TargetUtil.isVisionRenderTarget(livingEntity);
    }
}

package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.scoreboard.ScoreboardRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.Scoreboard;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public abstract class ScoreboardHudMixin {
    @Redirect(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void flowing$redirectScoreboardRender(InGameHud hud, DrawContext context, ScoreboardObjective objective) {
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if (scoreboard != null && scoreboard.isEnabled()) {
            ScoreboardRenderer.render(context, hud, objective);
            return;
        }

        ((com.xybaka.flowing.mixin.Accessor.InGameHudAccessor) hud).flowing$renderScoreboardSidebar(context, objective);
    }
}


package com.xybaka.flowing.mixin.Accessor;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InGameHud.class)
public interface InGameHudAccessor {
    @Invoker("renderScoreboardSidebar")
    void flowing$renderScoreboardSidebar(DrawContext context, ScoreboardObjective objective);
}

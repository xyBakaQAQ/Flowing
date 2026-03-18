package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.arraylist.ArrayListRenderer;
import com.xybaka.flowing.gui.keybinds.KeyBindsRenderer;
import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.gui.notification.NotificationRenderer;
import com.xybaka.flowing.gui.scoreboard.ScoreboardRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.modules.render.KeyBinds;
import com.xybaka.flowing.modules.render.Keystrokes;
import com.xybaka.flowing.modules.render.NameTags;
import com.xybaka.flowing.modules.render.Scoreboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    private static final float SCOREBOARD_BODY_BACKGROUND_OPACITY = 0.3F;
    private static final float SCOREBOARD_TITLE_BACKGROUND_OPACITY = 0.4F;
    private static final float SCOREBOARD_OPACITY_EPSILON = 0.0001F;

    @Inject(method = "render", at = @At("TAIL"))
    private void flowing$renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        boolean chatOpen = client.currentScreen instanceof ChatScreen;

        NameTags.renderHud(context);

        HUD hud = ModuleManager.getModule(HUD.class);
        if (hud != null && hud.isEnabled()) {
            if (!chatOpen) {
                hud.render(context);
            } else if (hud.shouldRenderTargetHud()) {
                hud.renderTargetHud(context);
            }

            if (!chatOpen && hud.shouldRenderArrayList()) {
                ArrayListRenderer.render(context);
            }

            if (!chatOpen && hud.shouldRenderNotifications()) {
                NotificationRenderer.render(context);
            }
        }

        KeyBinds keyBinds = ModuleManager.getModule(KeyBinds.class);
        if (!chatOpen && keyBinds != null && keyBinds.isEnabled()) {
            KeyBindsRenderer.render(context, keyBinds);
        }

        Keystrokes keystrokes = ModuleManager.getModule(Keystrokes.class);
        if (!chatOpen && keystrokes != null && keystrokes.isEnabled()) {
            KeystrokesRenderer.render(context, keystrokes);
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

    @Redirect(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void flowing$redirectScoreboardRender(InGameHud hud, DrawContext context, ScoreboardObjective objective) {
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if (scoreboard != null && scoreboard.isEnabled()) {
            ScoreboardRenderer.render(context, hud, objective);
            return;
        }

        ((com.xybaka.flowing.mixin.Accessor.InGameHudAccessor) hud).flowing$renderScoreboardSidebar(context, objective);
    }

    @Redirect(method = "method_55439", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/ScoreboardEntry;formatted(Lnet/minecraft/scoreboard/number/NumberFormat;)Lnet/minecraft/text/MutableText;"))
    private MutableText flowing$toggleScoreText(ScoreboardEntry entry, NumberFormat numberFormat) {
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if (scoreboard != null && scoreboard.isEnabled() && scoreboard.shouldHideNumbers()) {
            return Text.empty();
        }

        return entry.formatted(numberFormat);
    }

    @Redirect(method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getTextBackgroundColor(F)I"))
    private int flowing$overrideScoreboardBackground(GameOptions options, float opacity) {
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if (scoreboard == null || !scoreboard.isEnabled()) {
            return options.getTextBackgroundColor(opacity);
        }

        if (Math.abs(opacity - SCOREBOARD_TITLE_BACKGROUND_OPACITY) < SCOREBOARD_OPACITY_EPSILON
                && !scoreboard.shouldApplyBackgroundToTitle()) {
            return options.getTextBackgroundColor(opacity);
        }

        if (Math.abs(opacity - SCOREBOARD_BODY_BACKGROUND_OPACITY) < SCOREBOARD_OPACITY_EPSILON
                || Math.abs(opacity - SCOREBOARD_TITLE_BACKGROUND_OPACITY) < SCOREBOARD_OPACITY_EPSILON) {
            return scoreboard.getBackgroundColor();
        }

        return options.getTextBackgroundColor(opacity);
    }
}

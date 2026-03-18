package com.xybaka.flowing.mixin;

import com.xybaka.flowing.gui.arraylist.ArrayListRenderer;
import com.xybaka.flowing.gui.component.HudSnapHelper;
import com.xybaka.flowing.gui.inventory.InventoryRenderer;
import com.xybaka.flowing.gui.keybinds.KeyBindsRenderer;
import com.xybaka.flowing.gui.keystrokes.KeystrokesRenderer;
import com.xybaka.flowing.gui.notification.NotificationRenderer;
import com.xybaka.flowing.gui.scoreboard.ScoreboardRenderer;
import com.xybaka.flowing.gui.targethud.TargetHudRenderer;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.modules.render.KeyBinds;
import com.xybaka.flowing.modules.render.Keystrokes;
import com.xybaka.flowing.modules.render.Scoreboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {
    private static final float FLOWING_CHAT_HUD_Z = 500.0F;

    @Inject(method = "render", at = @At("TAIL"))
    private void flowing$renderHudOnTop(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        HUD hud = ModuleManager.getModule(HUD.class);
        Keystrokes keystrokes = ModuleManager.getModule(Keystrokes.class);
        KeyBinds keyBinds = ModuleManager.getModule(KeyBinds.class);
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);
        if ((hud == null || !hud.isEnabled())
                && (keystrokes == null || !keystrokes.isEnabled())
                && (keyBinds == null || !keyBinds.isEnabled())
                && (scoreboard == null || !scoreboard.isEnabled())) {
            return;
        }

        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, FLOWING_CHAT_HUD_Z);

        if (hud != null && hud.isEnabled() && hud.shouldRenderArrayList()) {
            ArrayListRenderer.render(context);
        }

        if (keyBinds != null && keyBinds.isEnabled()) {
            KeyBindsRenderer.render(context, keyBinds);
        }

        if (keystrokes != null && keystrokes.isEnabled()) {
            KeystrokesRenderer.render(context, keystrokes);
        }

        if (hud != null && hud.isEnabled()) {
            if (hud.shouldRenderTargetHud()) {
                TargetHudRenderer.render(context, hud);
            }

            if (hud.shouldRenderInventory()) {
                InventoryRenderer.render(context, hud);
            }

            if (hud.shouldRenderNotifications()) {
                NotificationRenderer.render(context);
            }
        }

        if (scoreboard != null && scoreboard.isEnabled()) {
            var objective = ScoreboardRenderer.getCurrentObjective();
            if (objective != null) {
                ScoreboardRenderer.render(context, MinecraftClient.getInstance().inGameHud, objective);
            }
        }

        HudSnapHelper.renderGuides(context);
        context.getMatrices().pop();
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void flowing$dragHudComponentsClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return;
        }

        HUD hud = ModuleManager.getModule(HUD.class);
        Keystrokes keystrokes = ModuleManager.getModule(Keystrokes.class);
        KeyBinds keyBinds = ModuleManager.getModule(KeyBinds.class);
        Scoreboard scoreboard = ModuleManager.getModule(Scoreboard.class);

        if (hud != null && hud.isEnabled() && hud.shouldRenderArrayList() && ArrayListRenderer.beginDragging(mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (keyBinds != null && keyBinds.isEnabled() && KeyBindsRenderer.beginDragging(keyBinds, mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (keystrokes != null && keystrokes.isEnabled() && KeystrokesRenderer.beginDragging(keystrokes, mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (scoreboard != null && scoreboard.isEnabled() && ScoreboardRenderer.beginDragging(mouseX, mouseY, MinecraftClient.getInstance().inGameHud)) {
            cir.setReturnValue(true);
            return;
        }

        if (hud == null || !hud.isEnabled()) {
            return;
        }

        if (hud.shouldRenderTargetHud() && TargetHudRenderer.beginDragging(hud, mouseX, mouseY)) {
            cir.setReturnValue(true);
            return;
        }

        if (hud.shouldRenderInventory() && InventoryRenderer.beginDragging(hud, mouseX, mouseY)) {
            cir.setReturnValue(true);
        }
    }
}

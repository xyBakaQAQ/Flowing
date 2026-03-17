package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.gui.inventory.InventoryRenderer;
import com.xybaka.flowing.gui.targethud.TargetHudRenderer;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.settings.BooleanSetting;
import com.xybaka.flowing.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.lwjgl.glfw.GLFW;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class HUD extends Module {
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int BACKGROUND_COLOR = ColorUtil.rgba(0, 0, 0, 144);

    private final BooleanSetting arrayList = bool("ArrayList", true);
    private final BooleanSetting targetHud = bool("TargetHud", true);
    private final BooleanSetting inventory = bool("Inventory", true);
    private final BooleanSetting potions = bool("Potions", true);
    private final BooleanSetting info = bool("Info", true);
    private final BooleanSetting notifications = bool("Notifications", true);

    public HUD() {
        super("HUD", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    public boolean shouldRenderArrayList() {
        return arrayList.getValue();
    }

    public boolean shouldRenderTargetHud() {
        return targetHud.getValue();
    }

    public boolean shouldRenderInventory() {
        return inventory.getValue();
    }

    public boolean shouldRenderPotions() {
        return potions.getValue();
    }

    public boolean shouldRenderInfo() {
        return info.getValue();
    }

    public boolean shouldRenderNotifications() {
        return notifications.getValue();
    }

    public void render(DrawContext context) {
        renderArrayListOverlay(context);
        if (shouldRenderTargetHud()) {
            renderTargetHud(context);
        }
        if (shouldRenderInventory()) {
            renderInventory(context);
        }
        if (shouldRenderPotions()) {
            renderPotions(context);
        }
        if (shouldRenderInfo()) {
            renderInfo(context);
        }
    }

    public void renderArrayListOverlay(DrawContext context) {
        if (shouldRenderArrayList()) {
            renderArrayList(context);
        }
    }

    public void renderTargetHud(DrawContext context) {
        TargetHudRenderer.render(context, this);
    }

    public void renderInventory(DrawContext context) {
        InventoryRenderer.render(context, this);
    }

    private void renderArrayList(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = mc.textRenderer;
        List<Module> enabledModules = ModuleManager.getEnabledModules().stream()
                .sorted(Comparator.comparingInt((Module module) -> textRenderer.getWidth(module.getName())).reversed())
                .toList();

        int y = PADDING;
        for (Module module : enabledModules) {
            String name = module.getName();
            int textWidth = textRenderer.getWidth(name);
            int x = context.getScaledWindowWidth() - textWidth - PADDING;

            context.fill(x - 2, y - 1, x + textWidth + 2, y + LINE_HEIGHT - 1, BACKGROUND_COLOR);
            context.drawText(textRenderer, name, x, y, TEXT_COLOR, true);
            y += LINE_HEIGHT;
        }
    }

    private void renderPotions(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        Collection<StatusEffectInstance> effects = mc.player.getStatusEffects();
        int y = context.getScaledWindowHeight() - textRenderer.fontHeight - PADDING;

        for (StatusEffectInstance effect : effects) {
            String effectName = effect.getEffectType().value().getName().getString();
            String duration = formatDuration(effect.getDuration());
            String text = effectName + " " + (effect.getAmplifier() + 1) + " - " + duration;
            int x = context.getScaledWindowWidth() - textRenderer.getWidth(text) - PADDING;
            int color = effect.getEffectType().value().getColor() | 0xFF000000;

            context.drawText(textRenderer, text, x, y, color, true);
            y -= textRenderer.fontHeight + PADDING;
        }
    }

    private void renderInfo(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        TextRenderer textRenderer = mc.textRenderer;
        int y = context.getScaledWindowHeight() - textRenderer.fontHeight - PADDING;

        String xyz = String.format("XYZ: %.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        context.drawText(textRenderer, xyz, PADDING, y, TEXT_COLOR, true);
        y -= textRenderer.fontHeight + PADDING;

        double deltaX = mc.player.getX() - mc.player.prevX;
        double deltaZ = mc.player.getZ() - mc.player.prevZ;
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ) * 20.0D;
        String speedText = String.format("Speed: %.2f m/s", speed);
        context.drawText(textRenderer, speedText, PADDING, y, TEXT_COLOR, true);
        y -= textRenderer.fontHeight + PADDING;

        String fpsText = "FPS: " + mc.getCurrentFps();
        context.drawText(textRenderer, fpsText, PADDING, y, TEXT_COLOR, true);
    }

    private String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        return String.format("%d:%02d", totalSeconds / 60, totalSeconds % 60);
    }
}


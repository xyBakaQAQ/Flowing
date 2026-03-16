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
import org.lwjgl.glfw.GLFW;

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
        TextRenderer textRenderer = client.textRenderer;
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
}

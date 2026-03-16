package com.xybaka.flowing.modules.render;

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
    private final BooleanSetting keystrokes = bool("Keystrokes", true);
    private final BooleanSetting keystrokesMouseButtons = bool("Mouse Buttons", true)
            .visibleWhen(this::shouldRenderKeystrokes)
            .childOf(keystrokes);
    private final BooleanSetting keystrokesSpace = bool("Space", true)
            .visibleWhen(this::shouldRenderKeystrokes)
            .childOf(keystrokes);
    private final BooleanSetting keystrokesShift = bool("Shift", false)
            .visibleWhen(this::shouldRenderKeystrokes)
            .childOf(keystrokes);
    private final BooleanSetting keystrokesCps = bool("CPS", false)
            .visibleWhen(() -> shouldRenderKeystrokes() && shouldRenderKeystrokesMouseButtons())
            .childOf(keystrokesMouseButtons);

    public HUD() {
        super("HUD", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    public boolean shouldRenderArrayList() {
        return arrayList.getValue();
    }

    public boolean shouldRenderKeystrokes() {
        return keystrokes.getValue();
    }

    public boolean shouldRenderKeystrokesMouseButtons() {
        return keystrokesMouseButtons.getValue();
    }

    public boolean shouldRenderKeystrokesSpace() {
        return keystrokesSpace.getValue();
    }

    public boolean shouldRenderKeystrokesShift() {
        return keystrokesShift.getValue();
    }

    public boolean shouldRenderKeystrokesCps() {
        return keystrokesCps.getValue();
    }

    public void render(DrawContext context) {
        if (!shouldRenderArrayList()) {
            return;
        }

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


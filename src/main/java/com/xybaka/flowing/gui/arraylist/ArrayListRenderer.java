package com.xybaka.flowing.gui.arraylist;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.client.ClickGUI;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Comparator;
import java.util.List;

public final class ArrayListRenderer {
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = ColorUtil.white;
    private static final int BACKGROUND_COLOR = ColorUtil.rgba(0, 0, 0, 144);
    private static final HudComponent COMPONENT = new HudComponent("arraylist", Math.max(0, WindowUtil.getWindowX() - 120), WindowUtil.getWindowY() - 80);

    private ArrayListRenderer() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        List<Module> enabledModules = getDisplayModules(textRenderer);
        int width = getWidth(textRenderer, enabledModules);
        int height = Math.max(LINE_HEIGHT, enabledModules.size() * LINE_HEIGHT);

        COMPONENT.setSize(width, height);
        int x = COMPONENT.getRenderX(getDefaultX(width));
        int y = COMPONENT.getRenderY(getDefaultBottomOffset(height));

        for (Module entry : enabledModules) {
            String name = entry.getName();
            int textWidth = textRenderer.getWidth(name);
            int textX = x + width - textWidth;

            context.fill(textX - 2, y - 1, textX + textWidth + 2, y + LINE_HEIGHT - 1, BACKGROUND_COLOR);
            context.drawText(textRenderer, name, textX, y, TEXT_COLOR, true);
            y += LINE_HEIGHT;
        }
    }

    public static boolean beginDragging(double mouseX, double mouseY) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<Module> enabledModules = getDisplayModules(textRenderer);
        int width = getWidth(textRenderer, enabledModules);
        int height = Math.max(LINE_HEIGHT, enabledModules.size() * LINE_HEIGHT);
        COMPONENT.setSize(width, height);
        return COMPONENT.beginDragging(mouseX, mouseY, getDefaultX(width), getDefaultBottomOffset(height));
    }

    public static boolean drag(double mouseX, double mouseY, boolean snap) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        List<Module> enabledModules = getDisplayModules(textRenderer);
        int width = getWidth(textRenderer, enabledModules);
        int height = Math.max(LINE_HEIGHT, enabledModules.size() * LINE_HEIGHT);
        COMPONENT.setSize(width, height);
        return COMPONENT.drag(mouseX, mouseY, snap);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static void stopDragging() {
        COMPONENT.stopDragging();
    }

    private static List<Module> getDisplayModules(TextRenderer textRenderer) {
        return ModuleManager.getEnabledModules().stream()
                .filter(module -> !(module instanceof ClickGUI))
                .sorted(Comparator.comparingInt((Module module) -> textRenderer.getWidth(module.getName())).reversed())
                .toList();
    }

    private static int getWidth(TextRenderer textRenderer, List<Module> modules) {
        int widest = modules.stream()
                .mapToInt(module -> textRenderer.getWidth(module.getName()))
                .max()
                .orElse(0);
        return widest + PADDING * 2;
    }

    private static int getDefaultX(int width) {
        return Math.max(0, WindowUtil.getWindowX() - width - PADDING);
    }

    private static int getDefaultBottomOffset(int height) {
        return Math.max(0, WindowUtil.getWindowY() - height - PADDING);
    }
}

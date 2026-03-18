package com.xybaka.flowing.gui.keybinds;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.KeyBinds;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.KeyUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public final class KeyBindsRenderer {
    private static final int LEFT = 6;
    private static final int PADDING = 6;
    private static final int ROW_HEIGHT = 10;
    private static final int TITLE_GAP = 4;
    private static final int TITLE_COLOR = ColorUtil.white;
    private static final int ENABLED_COLOR = ColorUtil.green;
    private static final int DISABLED_COLOR = ColorUtil.white;
    private static final int PANEL_COLOR = ColorUtil.rgba(0, 0, 0, 120);
    private static final int BORDER_COLOR = ColorUtil.rgba(255, 255, 255, 36);
    private static final int MIN_WIDTH = 100;
    private static final HudComponent COMPONENT = new HudComponent("keybinds", LEFT, WindowUtil.getCenteredY(140));

    private KeyBindsRenderer() {
    }

    public static void render(DrawContext context, KeyBinds module) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        List<Module> modules = getDisplayModules();
        Layout layout = measure(textRenderer, modules);

        COMPONENT.setSize(layout.width(), layout.height());
        int baseX = COMPONENT.getRenderX(LEFT);
        int baseY = COMPONENT.getRenderY(getDefaultBottomOffset(layout.height()));
        int right = baseX + layout.width();
        int bottom = baseY + layout.height();

        context.fill(baseX, baseY, right, bottom, PANEL_COLOR);
        context.drawBorder(baseX, baseY, layout.width(), layout.height(), BORDER_COLOR);
        context.drawText(textRenderer, "KeyBinds", baseX + PADDING, baseY + PADDING, TITLE_COLOR, true);

        int y = baseY + PADDING + ROW_HEIGHT + TITLE_GAP;
        for (Module entry : modules) {
            String moduleName = entry.getName();
            String bindName = getBindName(entry);
            int color = entry.isEnabled() ? ENABLED_COLOR : DISABLED_COLOR;
            int bindX = right - PADDING - textRenderer.getWidth(bindName);

            context.drawText(textRenderer, moduleName, baseX + PADDING, y, color, false);
            context.drawText(textRenderer, bindName, bindX, y, color, false);
            y += ROW_HEIGHT;
        }
    }

    public static boolean beginDragging(KeyBinds module, double mouseX, double mouseY) {
        Layout layout = measure(MinecraftClient.getInstance().textRenderer, getDisplayModules());
        COMPONENT.setSize(layout.width(), layout.height());
        return COMPONENT.beginDragging(mouseX, mouseY, LEFT, getDefaultBottomOffset(layout.height()));
    }

    public static boolean drag(KeyBinds module, double mouseX, double mouseY, boolean snap) {
        Layout layout = measure(MinecraftClient.getInstance().textRenderer, getDisplayModules());
        COMPONENT.setSize(layout.width(), layout.height());
        return COMPONENT.drag(mouseX, mouseY, snap);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static void stopDragging() {
        COMPONENT.stopDragging();
    }

    private static List<Module> getDisplayModules() {
        return ModuleManager.getModules().stream()
                .filter(module -> module.getKey() != GLFW.GLFW_KEY_UNKNOWN)
                .sorted(Comparator.comparing(Module::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private static Layout measure(TextRenderer textRenderer, List<Module> modules) {
        int widestLine = MIN_WIDTH;
        for (Module module : modules) {
            int lineWidth = textRenderer.getWidth(module.getName()) + 16 + textRenderer.getWidth(getBindName(module));
            widestLine = Math.max(widestLine, lineWidth);
        }

        int width = widestLine + PADDING * 2;
        int height = PADDING * 2 + ROW_HEIGHT + TITLE_GAP + modules.size() * ROW_HEIGHT;
        return new Layout(width, height);
    }

    private static String getBindName(Module module) {
        return KeyUtil.toDisplayName(module.getKey());
    }

    private static int getDefaultBottomOffset(int height) {
        return (WindowUtil.getWindowY() - height) / 2;
    }

    private record Layout(int width, int height) {
    }
}

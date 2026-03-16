package com.xybaka.flowing.gui.keystrokes;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class KeystrokesRenderer {
    private static final int KEY_SIZE = 18;
    private static final int KEY_GAP = 4;
    private static final int BAR_HEIGHT = 12;
    private static final int WIDTH = KEY_SIZE * 3 + KEY_GAP * 2;
    private static final int MOUSE_KEY_WIDTH = (WIDTH - KEY_GAP) / 2;
    private static final int KEY_TEXT_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int KEY_IDLE_COLOR = ColorUtil.rgba(0, 0, 0, 144);
    private static final int KEY_ACTIVE_COLOR = ColorUtil.rgba(131, 182, 255, 210);
    private static final int KEY_BORDER_COLOR = ColorUtil.rgba(255, 255, 255, 40);
    private static final HudComponent COMPONENT = new HudComponent("keystrokes", 6, 6);

    private KeystrokesRenderer() {
    }

    public static int getWidth() {
        return WIDTH;
    }

    public static int getHeight(HUD hud) {
        int height = KEY_SIZE * 2 + KEY_GAP;
        if (hud == null) {
            return height + KEY_GAP + KEY_SIZE + KEY_GAP + BAR_HEIGHT;
        }
        if (hud.shouldRenderKeystrokesMouseButtons()) {
            height += KEY_GAP + KEY_SIZE;
        }
        if (hud.shouldRenderKeystrokesSpace()) {
            height += KEY_GAP + BAR_HEIGHT;
        }
        if (hud.shouldRenderKeystrokesShift()) {
            height += KEY_GAP + BAR_HEIGHT;
        }
        return height;
    }

    public static void render(DrawContext context, HUD hud) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options == null) {
            return;
        }

        syncComponentSize(hud);
        int baseX = COMPONENT.getRenderX();
        int baseY = COMPONENT.getRenderY();
        int centerX = baseX + KEY_SIZE + KEY_GAP;
        int rightX = baseX + WIDTH - KEY_SIZE;
        int currentY = baseY;

        renderKey(context, centerX, currentY, KEY_SIZE, KEY_SIZE, "W", client.options.forwardKey.isPressed());
        currentY += KEY_SIZE + KEY_GAP;
        renderKey(context, baseX, currentY, KEY_SIZE, KEY_SIZE, "A", client.options.leftKey.isPressed());
        renderKey(context, centerX, currentY, KEY_SIZE, KEY_SIZE, "S", client.options.backKey.isPressed());
        renderKey(context, rightX, currentY, KEY_SIZE, KEY_SIZE, "D", client.options.rightKey.isPressed());
        currentY += KEY_SIZE;

        if (hud.shouldRenderKeystrokesMouseButtons()) {
            currentY += KEY_GAP;
            int rightMouseX = baseX + WIDTH - MOUSE_KEY_WIDTH;
            String leftLabel = hud.shouldRenderKeystrokesCps() ? "LMB " + CpsCounter.getLeftCps() : "LMB";
            String rightLabel = hud.shouldRenderKeystrokesCps() ? "RMB " + CpsCounter.getRightCps() : "RMB";
            renderKey(context, baseX, currentY, MOUSE_KEY_WIDTH, KEY_SIZE, leftLabel, client.options.attackKey.isPressed());
            renderKey(context, rightMouseX, currentY, MOUSE_KEY_WIDTH, KEY_SIZE, rightLabel, client.options.useKey.isPressed());
            currentY += KEY_SIZE;
        }

        if (hud.shouldRenderKeystrokesSpace()) {
            currentY += KEY_GAP;
            renderKey(context, baseX, currentY, WIDTH, BAR_HEIGHT, "SPACE", client.options.jumpKey.isPressed());
            currentY += BAR_HEIGHT;
        }

        if (hud.shouldRenderKeystrokesShift()) {
            currentY += KEY_GAP;
            renderKey(context, baseX, currentY, WIDTH, BAR_HEIGHT, "SHIFT", client.options.sneakKey.isPressed());
        }
    }

    public static boolean beginDragging(HUD hud, double mouseX, double mouseY) {
        syncComponentSize(hud);
        return COMPONENT.beginDragging(mouseX, mouseY);
    }

    public static boolean drag(HUD hud, double mouseX, double mouseY) {
        syncComponentSize(hud);
        return COMPONENT.drag(mouseX, mouseY);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static boolean stopDragging() {
        return COMPONENT.stopDragging();
    }

    private static void syncComponentSize(HUD hud) {
        COMPONENT.setSize(WIDTH, getHeight(hud));
    }

    private static void renderKey(DrawContext context, int x, int y, int width, int height, String label, boolean pressed) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        int backgroundColor = pressed ? KEY_ACTIVE_COLOR : KEY_IDLE_COLOR;

        context.fill(x, y, x + width, y + height, backgroundColor);
        context.drawBorder(x, y, width, height, KEY_BORDER_COLOR);

        int textX = x + (width - textRenderer.getWidth(label)) / 2;
        int textY = y + (height - 8) / 2;
        context.drawText(textRenderer, label, textX, textY, KEY_TEXT_COLOR, true);
    }
}

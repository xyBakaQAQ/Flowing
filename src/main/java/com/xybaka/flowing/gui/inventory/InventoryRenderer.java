package com.xybaka.flowing.gui.inventory;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public final class InventoryRenderer {
    private static final int COLUMNS = 9;
    private static final int ROWS = 3;
    private static final int SLOT_SIZE = 18;
    private static final int PADDING = 6;
    private static final int TITLE_HEIGHT = 10;
    private static final int CONTENT_TOP = PADDING + TITLE_HEIGHT + 2;
    private static final int WIDTH = PADDING * 2 + COLUMNS * SLOT_SIZE;
    private static final int HEIGHT = CONTENT_TOP + ROWS * SLOT_SIZE + PADDING;
    private static final int PANEL_COLOR = ColorUtil.rgba(14, 20, 28, 124);
    private static final int HIGHLIGHT_COLOR = ColorUtil.rgba(255, 255, 255, 18);
    private static final int BORDER_COLOR = ColorUtil.rgba(255, 255, 255, 30);
    private static final int TITLE_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final HudComponent COMPONENT = new HudComponent(
            "inventory",
            6,
            120
    );

    private InventoryRenderer() {
    }

    public static void render(DrawContext context, HUD hud) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        syncComponentSize();
        int baseX = COMPONENT.getRenderX();
        int baseY = COMPONENT.getRenderY();
        int right = baseX + WIDTH;
        int bottom = baseY + HEIGHT;

        renderPanel(context, baseX, baseY, right, bottom);

        TextRenderer textRenderer = client.textRenderer;
        context.drawText(textRenderer, "Inventory", baseX + PADDING, baseY + PADDING - 1, TITLE_COLOR, false);

        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                int slotX = baseX + PADDING + column * SLOT_SIZE;
                int slotY = baseY + CONTENT_TOP + row * SLOT_SIZE;
                int inventoryIndex = column + (row + 1) * COLUMNS;
                ItemStack stack = client.player.getInventory().getStack(inventoryIndex);

                if (stack.isEmpty()) {
                    continue;
                }

                context.drawItem(stack, slotX + 1, slotY + 1);
                context.drawStackOverlay(textRenderer, stack, slotX + 1, slotY + 1);
            }
        }
    }

    public static boolean beginDragging(HUD hud, double mouseX, double mouseY) {
        syncComponentSize();
        return COMPONENT.beginDragging(mouseX, mouseY);
    }

    public static boolean drag(HUD hud, double mouseX, double mouseY, boolean snap) {
        syncComponentSize();
        return COMPONENT.drag(mouseX, mouseY, snap);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static void stopDragging() {
        COMPONENT.stopDragging();
    }

    private static void renderPanel(DrawContext context, int left, int top, int right, int bottom) {
        context.fill(left, top, right, bottom, PANEL_COLOR);
        context.fill(left + 1, top + 1, right - 1, top + CONTENT_TOP - 2, HIGHLIGHT_COLOR);
        context.drawBorder(left, top, WIDTH, HEIGHT, BORDER_COLOR);
    }

    private static void syncComponentSize() {
        COMPONENT.setSize(WIDTH, HEIGHT);
    }
}




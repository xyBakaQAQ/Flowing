package com.xybaka.flowing.gui.targethud;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.modules.render.HUD;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.TargetUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;

public final class TargetHudRenderer {
    private static final int PADDING = 6;
    private static final int LINE_HEIGHT = 10;
    private static final int MIN_WIDTH = 110;
    private static final int BACKGROUND_COLOR = ColorUtil.rgba(0, 0, 0, 150);
    private static final int BORDER_COLOR = ColorUtil.rgba(255, 255, 255, 42);
    private static final int TITLE_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int INFO_COLOR = ColorUtil.rgb(211, 216, 224);
    private static final int HEALTH_BAR_BG = ColorUtil.rgba(255, 255, 255, 32);
    private static final int HEALTH_BAR_FILL = ColorUtil.rgb(131, 182, 255);
    private static final HudComponent COMPONENT = new HudComponent(
            "targethud",
            WindowUtil.getCenteredX(140),
            48
    );

    private TargetHudRenderer() {
    }

    public static void render(DrawContext context, HUD hud) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        boolean preview = client.currentScreen instanceof ChatScreen;
        LivingEntity displayEntity = preview ? client.player : TargetUtil.getTrackedVisionTarget();
        if (displayEntity == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        String name = displayEntity.getName().getString();
        String detail = String.format(
                "HP %.1f | Dist %.1f",
                displayEntity.getHealth(),
                client.player.distanceTo(displayEntity)
        );
        int width = Math.max(MIN_WIDTH, Math.max(textRenderer.getWidth(name), textRenderer.getWidth(detail)) + PADDING * 2);
        int height = PADDING * 2 + LINE_HEIGHT * 2 + 8;

        COMPONENT.setSize(width, height);
        int x = COMPONENT.getRenderX();
        int y = COMPONENT.getRenderY();

        context.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        context.drawBorder(x, y, width, height, BORDER_COLOR);
        context.drawText(textRenderer, name, x + PADDING, y + PADDING, TITLE_COLOR, true);
        context.drawText(textRenderer, detail, x + PADDING, y + PADDING + LINE_HEIGHT, INFO_COLOR, false);

        int barX = x + PADDING;
        int barY = y + height - PADDING - 4;
        int barWidth = width - PADDING * 2;
        context.fill(barX, barY, barX + barWidth, barY + 4, HEALTH_BAR_BG);

        float maxHealth = Math.max(1.0F, displayEntity.getMaxHealth());
        int fillWidth = Math.max(0, Math.min(barWidth, Math.round((displayEntity.getHealth() / maxHealth) * barWidth)));
        context.fill(barX, barY, barX + fillWidth, barY + 4, HEALTH_BAR_FILL);
    }

    public static boolean beginDragging(HUD hud, double mouseX, double mouseY) {
        syncPreviewSize();
        return COMPONENT.beginDragging(mouseX, mouseY);
    }

    public static boolean drag(HUD hud, double mouseX, double mouseY, boolean snap) {
        syncPreviewSize();
        return COMPONENT.drag(mouseX, mouseY, snap);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static void stopDragging() {
        COMPONENT.stopDragging();
    }

    private static void syncPreviewSize() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        String name = client.player.getName().getString();
        String detail = String.format("HP %.1f | Dist %.1f", client.player.getHealth(), 0.0F);
        int width = Math.max(MIN_WIDTH, Math.max(textRenderer.getWidth(name), textRenderer.getWidth(detail)) + PADDING * 2);
        int height = PADDING * 2 + LINE_HEIGHT * 2 + 8;
        COMPONENT.setSize(width, height);
    }
}



package com.xybaka.flowing.gui.notification;

import com.xybaka.flowing.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.List;

public final class NotificationRenderer {
    private static final int PADDING = 6;
    private static final int SPACING = 6;
    private static final int SCREEN_MARGIN = 12;
    private static final int TITLE_HEIGHT = 10;
    private static final int MESSAGE_HEIGHT = 9;
    private static final int BAR_HEIGHT = 3;
    private static final int MIN_WIDTH = 150;
    private static final int MAX_WIDTH = 220;
    private static final int ENTRY_HEIGHT = PADDING * 2 + TITLE_HEIGHT + MESSAGE_HEIGHT + BAR_HEIGHT + 4;
    private static final int BACKGROUND_COLOR = ColorUtil.rgba(12, 17, 23, 215);
    private static final int BORDER_COLOR = ColorUtil.rgba(255, 255, 255, 38);
    private static final int TITLE_COLOR = ColorUtil.rgb(255, 255, 255);
    private static final int MESSAGE_COLOR = ColorUtil.rgb(211, 216, 224);
    private static final int BAR_BACKGROUND = ColorUtil.rgba(255, 255, 255, 24);
    private static final long ANIMATION_MS = 180L;

    private NotificationRenderer() {
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        List<Notification> notifications = NotificationManager.getActiveNotifications();
        if (notifications.isEmpty()) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        long now = System.currentTimeMillis();
        int y = screenHeight - SCREEN_MARGIN;

        for (int index = notifications.size() - 1; index >= 0; index--) {
            Notification notification = notifications.get(index);
            int width = getWidth(textRenderer, notification);
            int entryTop = y - ENTRY_HEIGHT;
            int x = screenWidth - width - SCREEN_MARGIN + getSlideOffset(notification, now, width);
            int background = withAnimatedAlpha(BACKGROUND_COLOR, notification, now);
            int border = withAnimatedAlpha(BORDER_COLOR, notification, now);
            int titleColor = withAnimatedAlpha(TITLE_COLOR, notification, now);
            int messageColor = withAnimatedAlpha(MESSAGE_COLOR, notification, now);
            int barBackground = withAnimatedAlpha(BAR_BACKGROUND, notification, now);
            int accentColor = withAnimatedAlpha(getAccentColor(notification.getType()), notification, now);

            context.fill(x, entryTop, x + width, y, background);
            context.drawBorder(x, entryTop, width, ENTRY_HEIGHT, border);
            context.fill(x, entryTop, x + 3, y, accentColor);

            context.drawText(textRenderer, notification.getTitle(), x + PADDING + 4, entryTop + PADDING, titleColor, true);
            context.drawText(textRenderer, notification.getMessage(), x + PADDING + 4, entryTop + PADDING + TITLE_HEIGHT + 2, messageColor, false);

            int progressLeft = x + PADDING + 4;
            int progressTop = y - PADDING - BAR_HEIGHT;
            int progressWidth = width - (PADDING + 4) * 2;
            context.fill(progressLeft, progressTop, progressLeft + progressWidth, progressTop + BAR_HEIGHT, barBackground);

            double remainingProgress = 1.0D - Math.max(0.0D, Math.min(1.0D,
                    (double) notification.getElapsedMs(now) / notification.getDurationMs()));
            int fillWidth = (int) Math.round(progressWidth * remainingProgress);
            context.fill(progressLeft, progressTop, progressLeft + fillWidth, progressTop + BAR_HEIGHT, accentColor);

            y = entryTop - SPACING;
        }
    }

    private static int getWidth(TextRenderer textRenderer, Notification notification) {
        int contentWidth = Math.max(textRenderer.getWidth(notification.getTitle()), textRenderer.getWidth(notification.getMessage()));
        return Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, contentWidth + (PADDING + 4) * 2));
    }

    private static int getSlideOffset(Notification notification, long now, int width) {
        long elapsed = notification.getElapsedMs(now);
        long remaining = notification.getDurationMs() - elapsed;

        if (elapsed < ANIMATION_MS) {
            double progress = 1.0D - (double) elapsed / ANIMATION_MS;
            return (int) Math.round(width * easeOutCubic(progress));
        }

        if (remaining < ANIMATION_MS) {
            double progress = 1.0D - (double) remaining / ANIMATION_MS;
            return (int) Math.round(width * easeInCubic(progress));
        }

        return 0;
    }

    private static int withAnimatedAlpha(int color, Notification notification, long now) {
        double alphaScale = getAlphaScale(notification, now);
        int alpha = (int) Math.round(ColorUtil.getAlpha(color) * alphaScale);
        return ColorUtil.withAlpha(color, alpha);
    }

    private static double getAlphaScale(Notification notification, long now) {
        long elapsed = notification.getElapsedMs(now);
        long remaining = notification.getDurationMs() - elapsed;

        if (elapsed < ANIMATION_MS) {
            return easeOutCubic((double) elapsed / ANIMATION_MS);
        }

        if (remaining < ANIMATION_MS) {
            return 1.0D - easeInCubic(1.0D - (double) remaining / ANIMATION_MS);
        }

        return 1.0D;
    }

    private static int getAccentColor(NotificationType type) {
        return switch (type) {
            case SUCCESS -> ColorUtil.rgb(115, 225, 140);
            case WARNING -> ColorUtil.rgb(255, 201, 87);
            case ERROR -> ColorUtil.rgb(255, 108, 108);
            case INFO -> ColorUtil.rgb(131, 182, 255);
        };
    }

    private static double easeOutCubic(double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        return 1.0D - Math.pow(1.0D - clamped, 3.0D);
    }

    private static double easeInCubic(double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        return clamped * clamped * clamped;
    }
}

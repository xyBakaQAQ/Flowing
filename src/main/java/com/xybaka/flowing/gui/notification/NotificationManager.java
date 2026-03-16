package com.xybaka.flowing.gui.notification;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class NotificationManager {
    private static final int MAX_VISIBLE = 4;
    private static final long DEFAULT_DURATION_MS = 2200L;
    private static final List<Notification> NOTIFICATIONS = new ArrayList<>();

    private NotificationManager() {
    }

    public static void push(String title, String message, NotificationType type) {
        push(title, message, type, DEFAULT_DURATION_MS);
    }

    public static void push(String title, String message, NotificationType type, long durationMs) {
        NOTIFICATIONS.add(new Notification(title, message, type, durationMs));
        trimOverflow();
    }

    public static List<Notification> getActiveNotifications() {
        long now = System.currentTimeMillis();
        Iterator<Notification> iterator = NOTIFICATIONS.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().isExpired(now)) {
                iterator.remove();
            }
        }
        return List.copyOf(NOTIFICATIONS);
    }

    private static void trimOverflow() {
        while (NOTIFICATIONS.size() > MAX_VISIBLE) {
            NOTIFICATIONS.remove(0);
        }
    }
}

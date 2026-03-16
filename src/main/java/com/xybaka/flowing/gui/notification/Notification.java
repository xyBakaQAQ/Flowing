package com.xybaka.flowing.gui.notification;

public final class Notification {
    private final String title;
    private final String message;
    private final NotificationType type;
    private final long createdAt;
    private final long durationMs;

    public Notification(String title, String message, NotificationType type, long durationMs) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.durationMs = durationMs;
        this.createdAt = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationType getType() {
        return type;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public long getElapsedMs(long now) {
        return now - createdAt;
    }

    public boolean isExpired(long now) {
        return getElapsedMs(now) >= durationMs;
    }
}

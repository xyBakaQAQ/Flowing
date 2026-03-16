package com.xybaka.flowing.gui.component;

import com.xybaka.flowing.config.ConfigManager;

public final class HudComponent {
    private final String id;
    private final int defaultX;
    private final int defaultY;
    private final DraggableHudComponent draggable = new DraggableHudComponent(0, 0);

    public HudComponent(String id, int defaultX, int defaultY) {
        this.id = id;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return HudPositionManager.getInstance().getX(id, defaultX);
    }

    public int getY() {
        return HudPositionManager.getInstance().getY(id, defaultY);
    }

    public void setSize(int width, int height) {
        draggable.setSize(width, height);
    }

    public int getRenderX() {
        return draggable.getRenderX(getX());
    }

    public int getRenderY() {
        return draggable.getRenderY(getY());
    }

    public void setPosition(int x, int y) {
        HudPositionManager.getInstance().setPosition(id, x, y);
        ConfigManager.requestSave();
    }

    public boolean beginDragging(double mouseX, double mouseY) {
        return draggable.beginDragging(getX(), getY(), mouseX, mouseY);
    }

    public boolean drag(double mouseX, double mouseY) {
        if (!draggable.drag(mouseX, mouseY)) {
            return false;
        }

        setPosition(draggable.getDraggedX(mouseX), draggable.getDraggedBottomOffset(mouseY));
        return true;
    }

    public boolean isDragging() {
        return draggable.isDragging();
    }

    public boolean stopDragging() {
        return draggable.stopDragging();
    }
}

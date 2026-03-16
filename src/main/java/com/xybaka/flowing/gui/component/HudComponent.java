package com.xybaka.flowing.gui.component;

import com.xybaka.flowing.config.ConfigManager;

import java.util.ArrayList;
import java.util.List;

public final class HudComponent {
    private static final List<HudComponent> REGISTERED_COMPONENTS = new ArrayList<>();

    private final String id;
    private final int defaultX;
    private final int defaultY;
    private final DraggableHudComponent draggable = new DraggableHudComponent(0, 0);
    private int width;
    private int height;

    public HudComponent(String id, int defaultX, int defaultY) {
        this.id = id;
        this.defaultX = defaultX;
        this.defaultY = defaultY;
        REGISTERED_COMPONENTS.add(this);
    }

    public String getId() {
        return id;
    }

    public boolean hasStoredPosition() {
        return HudPositionManager.getInstance().hasPosition(id);
    }

    public int getX() {
        return HudPositionManager.getInstance().getX(id, defaultX, defaultY);
    }

    public int getY() {
        return HudPositionManager.getInstance().getY(id, defaultX, defaultY);
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        draggable.setSize(width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getRenderX() {
        return draggable.getRenderX(getX());
    }

    public int getRenderY() {
        return draggable.getRenderY(getY());
    }

    public int getRenderX(int fallbackX) {
        return draggable.getRenderX(hasStoredPosition() ? getX() : fallbackX);
    }

    public int getRenderY(int fallbackBottomOffset) {
        return draggable.getRenderY(hasStoredPosition() ? getY() : fallbackBottomOffset);
    }

    public void setPosition(int x, int y) {
        HudPositionManager.getInstance().setPosition(id, x, y);
        ConfigManager.requestSave();
    }

    public boolean beginDragging(double mouseX, double mouseY) {
        return draggable.beginDragging(getX(), getY(), mouseX, mouseY);
    }

    public boolean beginDragging(double mouseX, double mouseY, int fallbackX, int fallbackBottomOffset) {
        int leftX = hasStoredPosition() ? getX() : fallbackX;
        int bottomOffset = hasStoredPosition() ? getY() : fallbackBottomOffset;
        return draggable.beginDragging(leftX, bottomOffset, mouseX, mouseY);
    }

    public boolean drag(double mouseX, double mouseY) {
        return drag(mouseX, mouseY, false);
    }

    public boolean drag(double mouseX, double mouseY, boolean snap) {
        if (!draggable.drag(mouseX, mouseY)) {
            return false;
        }

        int draggedX = draggable.getDraggedX(mouseX);
        int draggedBottomOffset = draggable.getDraggedBottomOffset(mouseY);
        if (snap) {
            HudSnapHelper.SnapPosition snapped = HudSnapHelper.snap(this, draggedX, draggedBottomOffset);
            setPosition(snapped.x(), snapped.bottomOffset());
            return true;
        }

        HudSnapHelper.clearGuides();
        setPosition(draggedX, draggedBottomOffset);
        return true;
    }

    public boolean isDragging() {
        return draggable.isDragging();
    }

    public void stopDragging() {
        draggable.stopDragging();
    }

    public static List<HudComponent> getRegisteredComponents() {
        return List.copyOf(REGISTERED_COMPONENTS);
    }
}

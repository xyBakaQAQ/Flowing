package com.xybaka.flowing.gui.component;

import com.xybaka.flowing.util.WindowUtil;

public final class DraggableHudComponent {
    private int width;
    private int height;
    private boolean dragging;
    private int dragOffsetX;
    private int dragOffsetY;

    public DraggableHudComponent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getRenderX(int leftX) {
        return clamp(leftX, 0, Math.max(0, WindowUtil.getWindowX() - width));
    }

    public int getRenderY(int bottomOffset) {
        int topY = WindowUtil.getWindowY() - height - bottomOffset;
        return clamp(topY, 0, Math.max(0, WindowUtil.getWindowY() - height));
    }

    public boolean isHovering(int leftX, int bottomOffset, double mouseX, double mouseY) {
        int renderX = getRenderX(leftX);
        int renderY = getRenderY(bottomOffset);
        return mouseX >= renderX && mouseX <= renderX + width && mouseY >= renderY && mouseY <= renderY + height;
    }

    public boolean beginDragging(int leftX, int bottomOffset, double mouseX, double mouseY) {
        if (!isHovering(leftX, bottomOffset, mouseX, mouseY)) {
            return false;
        }

        dragging = true;
        dragOffsetX = (int) mouseX - getRenderX(leftX);
        dragOffsetY = (int) mouseY - getRenderY(bottomOffset);
        return true;
    }

    public boolean drag(double mouseX, double mouseY) {
        return dragging;
    }

    public int getDraggedX(double mouseX) {
        int maxX = Math.max(0, WindowUtil.getWindowX() - width);
        return clamp((int) mouseX - dragOffsetX, 0, maxX);
    }

    public int getDraggedBottomOffset(double mouseY) {
        int maxY = Math.max(0, WindowUtil.getWindowY() - height);
        int newY = clamp((int) mouseY - dragOffsetY, 0, maxY);
        return WindowUtil.getWindowY() - height - newY;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void stopDragging() {
        dragging = false;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

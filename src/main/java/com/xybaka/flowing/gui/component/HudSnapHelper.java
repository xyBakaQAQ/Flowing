package com.xybaka.flowing.gui.component;

import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public final class HudSnapHelper {
    private static final int SNAP_DISTANCE = 10;
    private static final int COMPONENT_GAP = 4;
    private static final int GUIDE_COLOR = ColorUtil.rgba(131, 182, 255, 170);
    private static final int GUIDE_HIGHLIGHT_COLOR = ColorUtil.rgba(255, 255, 255, 210);

    private static GuideLine horizontalGuide;
    private static GuideLine verticalGuide;

    private HudSnapHelper() {
    }

    public static SnapPosition snap(HudComponent component, int rawX, int rawBottomOffset) {
        int width = component.getWidth();
        int height = component.getHeight();
        int maxX = Math.max(0, WindowUtil.getWindowX() - width);
        int maxTop = Math.max(0, WindowUtil.getWindowY() - height);
        int rawTop = WindowUtil.getWindowY() - height - rawBottomOffset;

        SnapCandidate horizontal = snapAxis(rawX, getHorizontalCandidates(component, width), 0, maxX);
        SnapCandidate vertical = snapAxis(rawTop, getVerticalCandidates(component, height), 0, maxTop);
        verticalGuide = horizontal.guideLine();
        horizontalGuide = vertical.guideLine();

        int clampedTop = clamp(vertical.position(), 0, maxTop);
        return new SnapPosition(clamp(horizontal.position(), 0, maxX), WindowUtil.getWindowY() - height - clampedTop);
    }

    public static void clearGuides() {
        horizontalGuide = null;
        verticalGuide = null;
    }

    public static void renderGuides(DrawContext context) {
        if (verticalGuide != null) {
            context.fill(verticalGuide.coordinate(), 0, verticalGuide.coordinate() + 1, WindowUtil.getWindowY(), GUIDE_COLOR);
            context.fill(verticalGuide.coordinate() - 1, 0, verticalGuide.coordinate(), WindowUtil.getWindowY(), GUIDE_HIGHLIGHT_COLOR);
        }

        if (horizontalGuide != null) {
            context.fill(0, horizontalGuide.coordinate(), WindowUtil.getWindowX(), horizontalGuide.coordinate() + 1, GUIDE_COLOR);
            context.fill(0, horizontalGuide.coordinate() - 1, WindowUtil.getWindowX(), horizontalGuide.coordinate(), GUIDE_HIGHLIGHT_COLOR);
        }
    }

    private static SnapCandidate snapAxis(int value, List<SnapCandidate> candidates, int min, int max) {
        int best = clamp(value, min, max);
        int bestDistance = SNAP_DISTANCE + 1;
        GuideLine bestGuide = null;
        for (SnapCandidate candidate : candidates) {
            int clampedCandidate = clamp(candidate.position(), min, max);
            int distance = Math.abs(value - clampedCandidate);
            if (distance <= SNAP_DISTANCE && distance < bestDistance) {
                best = clampedCandidate;
                bestDistance = distance;
                bestGuide = candidate.guideLine();
            }
        }
        return new SnapCandidate(best, bestGuide);
    }

    private static List<SnapCandidate> getHorizontalCandidates(HudComponent component, int width) {
        List<SnapCandidate> candidates = new ArrayList<>();
        candidates.add(new SnapCandidate(0, new GuideLine(0)));
        candidates.add(new SnapCandidate((WindowUtil.getWindowX() - width) / 2, new GuideLine(WindowUtil.getWindowX() / 2)));
        candidates.add(new SnapCandidate(WindowUtil.getWindowX() - width, new GuideLine(WindowUtil.getWindowX() - 1)));

        for (HudComponent other : HudComponent.getRegisteredComponents()) {
            if (other == component || other.getWidth() <= 0 || other.getHeight() <= 0) {
                continue;
            }

            int otherLeft = other.getRenderX();
            int otherRight = otherLeft + other.getWidth();
            int otherCenter = otherLeft + other.getWidth() / 2;
            candidates.add(new SnapCandidate(otherLeft, new GuideLine(otherLeft)));
            candidates.add(new SnapCandidate(otherRight - width, new GuideLine(otherRight)));
            candidates.add(new SnapCandidate(otherLeft + (other.getWidth() - width) / 2, new GuideLine(otherCenter)));
            candidates.add(new SnapCandidate(otherLeft - COMPONENT_GAP - width, new GuideLine(otherLeft)));
            candidates.add(new SnapCandidate(otherRight + COMPONENT_GAP, new GuideLine(otherRight)));
        }

        return candidates;
    }

    private static List<SnapCandidate> getVerticalCandidates(HudComponent component, int height) {
        List<SnapCandidate> candidates = new ArrayList<>();
        candidates.add(new SnapCandidate(0, new GuideLine(0)));
        candidates.add(new SnapCandidate((WindowUtil.getWindowY() - height) / 2, new GuideLine(WindowUtil.getWindowY() / 2)));
        candidates.add(new SnapCandidate(WindowUtil.getWindowY() - height, new GuideLine(WindowUtil.getWindowY() - 1)));

        for (HudComponent other : HudComponent.getRegisteredComponents()) {
            if (other == component || other.getWidth() <= 0 || other.getHeight() <= 0) {
                continue;
            }

            int otherTop = other.getRenderY();
            int otherBottom = otherTop + other.getHeight();
            int otherCenter = otherTop + other.getHeight() / 2;
            candidates.add(new SnapCandidate(otherTop, new GuideLine(otherTop)));
            candidates.add(new SnapCandidate(otherBottom - height, new GuideLine(otherBottom)));
            candidates.add(new SnapCandidate(otherTop + (other.getHeight() - height) / 2, new GuideLine(otherCenter)));
            candidates.add(new SnapCandidate(otherTop - COMPONENT_GAP - height, new GuideLine(otherTop)));
            candidates.add(new SnapCandidate(otherBottom + COMPONENT_GAP, new GuideLine(otherBottom)));
        }

        return candidates;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record SnapCandidate(int position, GuideLine guideLine) {
    }

    private record GuideLine(int coordinate) {
    }

    public record SnapPosition(int x, int bottomOffset) {
    }
}


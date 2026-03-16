package com.xybaka.flowing.gui.scoreboard;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.mixin.Accessor.InGameHudAccessor;
import com.xybaka.flowing.modules.ModuleManager;
import com.xybaka.flowing.modules.render.Scoreboard;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.Team;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.scoreboard.number.StyledNumberFormat;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Comparator;
import java.util.List;

public final class ScoreboardRenderer {
    private static final Comparator<ScoreboardEntry> ENTRY_COMPARATOR = Comparator
            .comparingInt(ScoreboardEntry::value)
            .reversed()
            .thenComparing(ScoreboardEntry::owner, String.CASE_INSENSITIVE_ORDER);
    private static final int MAX_ENTRIES = 15;
    private static final int HORIZONTAL_MARGIN = 3;
    private static final HudComponent COMPONENT = new HudComponent(
            "scoreboard",
            Math.max(0, WindowUtil.getWindowX() - 120),
            Math.max(0, WindowUtil.getWindowY() / 2 - 80)
    );

    private ScoreboardRenderer() {
    }

    public static void render(DrawContext context, InGameHud hud, ScoreboardObjective objective) {
        if (objective == null) {
            return;
        }

        Layout layout = Layout.measure(hud, objective);
        COMPONENT.setSize(layout.width(), layout.height());

        int renderX = COMPONENT.getRenderX(layout.vanillaLeft());
        int renderY = COMPONENT.getRenderY(layout.vanillaBottomOffset());
        int translateX = renderX - layout.vanillaLeft();
        int translateY = renderY - layout.vanillaTop();

        context.getMatrices().push();
        context.getMatrices().translate(translateX, translateY, 0.0F);
        if (shouldHideNumbers()) {
            renderWithoutNumbers(context, hud, layout);
        } else {
            ((InGameHudAccessor) hud).flowing$renderScoreboardSidebar(context, objective);
        }
        context.getMatrices().pop();
    }

    public static boolean beginDragging(double mouseX, double mouseY, InGameHud hud) {
        ScoreboardObjective objective = getCurrentObjective();
        if (objective == null) {
            return false;
        }

        Layout layout = Layout.measure(hud, objective);
        COMPONENT.setSize(layout.width(), layout.height());
        return COMPONENT.beginDragging(mouseX, mouseY, layout.vanillaLeft(), layout.vanillaBottomOffset());
    }

    public static boolean drag(double mouseX, double mouseY, boolean snap) {
        return COMPONENT.drag(mouseX, mouseY, snap);
    }

    public static boolean isDragging() {
        return COMPONENT.isDragging();
    }

    public static void stopDragging() {
        COMPONENT.stopDragging();
    }

    public static ScoreboardObjective getCurrentObjective() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) {
            return null;
        }

        var scoreboard = client.world.getScoreboard();
        ClientPlayerEntity player = client.player;
        Team team = scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
        if (team != null) {
            Formatting formatting = team.getColor();
            ScoreboardDisplaySlot slot = ScoreboardDisplaySlot.fromFormatting(formatting);
            if (slot != null) {
                ScoreboardObjective objective = scoreboard.getObjectiveForSlot(slot);
                if (objective != null) {
                    return objective;
                }
            }
        }

        return scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
    }

    private static boolean shouldHideNumbers() {
        Scoreboard module = ModuleManager.getModule(Scoreboard.class);
        return module != null && module.shouldHideNumbers();
    }

    private static void renderWithoutNumbers(DrawContext context, InGameHud hud, Layout layout) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = hud.getTextRenderer();
        int rowBackgroundColor = client.options.getTextBackgroundColor(0.3F);
        int titleBackgroundColor = client.options.getTextBackgroundColor(0.4F);

        context.fill(layout.left() - 2, layout.titleTop(), layout.right(), layout.titleBottom(), titleBackgroundColor);
        context.fill(layout.left() - 2, layout.titleBottom(), layout.right(), layout.bottom(), rowBackgroundColor);
        context.drawText(textRenderer, layout.title(), layout.left() + layout.contentWidth() / 2 - layout.titleWidth() / 2, layout.titleTop() + 1, -1, false);

        for (int index = 0; index < layout.entries().size(); index++) {
            ScoreboardEntry entry = layout.entries().get(index);
            int y = layout.bottom() - (layout.entries().size() - index) * 9;
            context.drawText(textRenderer, entry.name(), layout.left(), y, -1, false);
        }
    }

    private record Layout(
            ScoreboardObjective objective,
            List<ScoreboardEntry> entries,
            Text title,
            int titleWidth,
            int contentWidth,
            int vanillaLeft,
            int vanillaTop,
            int vanillaBottomOffset,
            int left,
            int right,
            int titleTop,
            int titleBottom,
            int bottom,
            int width,
            int height) {
        private static Layout measure(InGameHud hud, ScoreboardObjective objective) {
            TextRenderer textRenderer = hud.getTextRenderer();
            NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
            boolean hideNumbers = shouldHideNumbers();
            List<ScoreboardEntry> entries = objective.getScoreboard().getScoreboardEntries(objective).stream()
                    .filter(entry -> !entry.hidden())
                    .sorted(ENTRY_COMPARATOR)
                    .limit(MAX_ENTRIES)
                    .toList();

            Text title = objective.getDisplayName();
            int titleWidth = textRenderer.getWidth(title);
            int colonWidth = textRenderer.getWidth(":");
            int contentWidth = titleWidth;
            for (ScoreboardEntry entry : entries) {
                int rowWidth = textRenderer.getWidth(entry.name());
                if (!hideNumbers) {
                    int scoreWidth = textRenderer.getWidth(entry.formatted(numberFormat));
                    if (scoreWidth > 0) {
                        rowWidth += colonWidth + scoreWidth;
                    }
                }
                contentWidth = Math.max(contentWidth, rowWidth);
            }

            int totalRowsHeight = entries.size() * 9;
            int bottom = WindowUtil.getWindowY() / 2 + totalRowsHeight / 3;
            int titleBaseline = bottom - totalRowsHeight;
            int left = WindowUtil.getWindowX() - contentWidth - HORIZONTAL_MARGIN - 2;
            int right = WindowUtil.getWindowX() - HORIZONTAL_MARGIN + 2;
            int titleTop = titleBaseline - 10;
            int titleBottom = titleBaseline - 1;
            int width = contentWidth + 4;
            int height = totalRowsHeight + 10;
            int vanillaBottomOffset = WindowUtil.getWindowY() - height - titleTop;
            return new Layout(objective, entries, title, titleWidth, contentWidth, left, titleTop, vanillaBottomOffset, left, right, titleTop, titleBottom, bottom, width, height);
        }
    }
}

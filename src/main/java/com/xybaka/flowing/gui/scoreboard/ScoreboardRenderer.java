package com.xybaka.flowing.gui.scoreboard;

import com.xybaka.flowing.gui.component.HudComponent;
import com.xybaka.flowing.mixin.Accessor.InGameHudAccessor;
import com.xybaka.flowing.util.WindowUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
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
        ((InGameHudAccessor) hud).flowing$renderScoreboardSidebar(context, objective);
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

    private static SidebarEntry createSidebarEntry(net.minecraft.scoreboard.Scoreboard scoreboard, NumberFormat numberFormat, ScoreboardEntry entry, TextRenderer textRenderer) {
        AbstractTeam team = scoreboard.getScoreHolderTeam(entry.owner());
        Text name = Team.decorateName(team, entry.name());
        Text score = entry.formatted(numberFormat);
        return new SidebarEntry(name, textRenderer.getWidth(score));
    }

    private record SidebarEntry(Text name, int scoreWidth) {
    }

    private record Layout(int vanillaLeft, int vanillaTop, int vanillaBottomOffset, int width, int height) {
        private static Layout measure(InGameHud hud, ScoreboardObjective objective) {
            TextRenderer textRenderer = hud.getTextRenderer();
            var scoreboard = objective.getScoreboard();
            NumberFormat numberFormat = objective.getNumberFormatOr(StyledNumberFormat.RED);
            List<SidebarEntry> entries = scoreboard.getScoreboardEntries(objective).stream()
                    .filter(entry -> !entry.hidden())
                    .sorted(ENTRY_COMPARATOR)
                    .limit(MAX_ENTRIES)
                    .map(entry -> createSidebarEntry(scoreboard, numberFormat, entry, textRenderer))
                    .toList();

            Text title = objective.getDisplayName();
            int titleWidth = textRenderer.getWidth(title);
            int colonWidth = textRenderer.getWidth(":");
            int contentWidth = titleWidth;
            for (SidebarEntry entry : entries) {
                int rowWidth = textRenderer.getWidth(entry.name());
                if (entry.scoreWidth() > 0) {
                    rowWidth += colonWidth + entry.scoreWidth();
                }
                contentWidth = Math.max(contentWidth, rowWidth);
            }

            int totalRowsHeight = entries.size() * 9;
            int bottom = WindowUtil.getWindowY() / 2 + totalRowsHeight / 3;
            int titleBaseline = bottom - totalRowsHeight;
            int left = WindowUtil.getWindowX() - contentWidth - HORIZONTAL_MARGIN - 2;
            int titleTop = titleBaseline - 10;
            int width = contentWidth + 4;
            int height = totalRowsHeight + 10;
            int vanillaBottomOffset = WindowUtil.getWindowY() - height - titleTop;
            return new Layout(left, titleTop, vanillaBottomOffset, width, height);
        }
    }
}

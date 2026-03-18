package com.xybaka.flowing.modules.render;

import com.xybaka.flowing.event.features.WorldRenderEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.modules.settings.NumberSetting;
import com.xybaka.flowing.util.ColorUtil;
import com.xybaka.flowing.util.RenderUtil;
import com.xybaka.flowing.util.TargetUtil;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class NameTags extends Module {
    private static final int BRACKET_COLOR = ColorUtil.gray;
    private static final int DISTANCE_COLOR = ColorUtil.cyan;
    private static final int GOOD_COLOR = ColorUtil.green;
    private static final int MEDIUM_COLOR = ColorUtil.yellow;
    private static final int BAD_COLOR = ColorUtil.red;
    private static final int TEXT_COLOR = ColorUtil.white;
    private static final int BACKGROUND_COLOR = ColorUtil.rgba(0, 0, 0, 102);
    private static final int LINE_SPACING = 10;
    private static final int PADDING = 2;
    private static final Map<UUID, ProjectedTag> PROJECTED_TAGS = new LinkedHashMap<>();

    private final NumberSetting scale = number("Scale", 1.0D, 0.5D, 3.0D, 0.05D);

    public NameTags() {
        super("NameTags", Category.RENDER, GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onWorldRender(WorldRenderEvent event) {
        renderWorld(event.getContext());
    }

    private static void renderWorld(WorldRenderContext context) {
        PROJECTED_TAGS.clear();

        NameTags module = com.xybaka.flowing.modules.ModuleManager.getModule(NameTags.class);
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.isEnabled() || client.world == null || client.player == null) {
            return;
        }

        float tickDelta = context.tickCounter().getTickDelta(true);
        for (PlayerEntity player : client.world.getPlayers()) {
            if (!module.shouldRender(player)) {
                continue;
            }

            Vec3d worldPos = module.getRenderPosition(player, tickDelta);
            if (worldPos == null || context.camera().getPos().squaredDistanceTo(worldPos) > 4096.0D) {
                continue;
            }

            RenderUtil.Projection projection = RenderUtil.project(context.positionMatrix(), context.projectionMatrix(), worldPos);
            if (!projection.visible()) {
                continue;
            }

            PROJECTED_TAGS.put(player.getUuid(), new ProjectedTag(
                    projection.position().x,
                    projection.position().y,
                    module.buildStatsLine(player),
                    player.getDisplayName().copy(),
                    module.getScale()
            ));
        }
    }

    public static void renderHud(DrawContext context) {
        NameTags module = com.xybaka.flowing.modules.ModuleManager.getModule(NameTags.class);
        MinecraftClient client = MinecraftClient.getInstance();
        if (module == null || !module.isEnabled() || client.player == null || PROJECTED_TAGS.isEmpty()) {
            return;
        }

        for (ProjectedTag tag : PROJECTED_TAGS.values()) {
            module.renderProjectedTag(context, tag);
        }
    }

    public double getScale() {
        return scale.getValue();
    }

    public boolean shouldRender(Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return false;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (player == client.player && client.options.getPerspective() == Perspective.FIRST_PERSON) {
            return false;
        }

        return TargetUtil.isVisionPlayerRenderTarget(player);
    }

    private void renderProjectedTag(DrawContext context, ProjectedTag tag) {
        MinecraftClient client = MinecraftClient.getInstance();
        int statsWidth = client.textRenderer.getWidth(tag.statsLine());
        int nameWidth = mc.textRenderer.getWidth(tag.nameLine());
        int maxWidth = Math.max(statsWidth, nameWidth);
        float scale = (float) tag.scale();
        float lineHeight = LINE_SPACING;
        float totalHeight = lineHeight * 2.0F;

        context.getMatrices().push();
        context.getMatrices().translate((float) tag.screenX(), (float) tag.screenY(), 200.0F);
        context.getMatrices().scale(scale, scale, 1.0F);

        int left = Math.round(-maxWidth / 2.0F - PADDING);
        int right = Math.round(maxWidth / 2.0F + PADDING);
        int top = Math.round(-totalHeight - PADDING);
        int bottom = Math.round(PADDING);
        context.fill(left, top, right, bottom, BACKGROUND_COLOR);

        int statsX = Math.round(-statsWidth / 2.0F);
        int nameX = Math.round(-nameWidth / 2.0F);
        context.drawTextWithShadow(mc.textRenderer, tag.statsLine(), statsX, Math.round(-totalHeight), TEXT_COLOR);
        context.drawTextWithShadow(mc.textRenderer, tag.nameLine(), nameX, Math.round(-lineHeight), TEXT_COLOR);
        context.getMatrices().pop();
    }

    private Vec3d getRenderPosition(PlayerEntity player, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (player == client.player) {
            return player.getLerpedPos(tickDelta).add(0.0D, player.getHeight() + 0.55D, 0.0D);
        }

        Vec3d attachment = player.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, player.getLerpedYaw(tickDelta));
        if (attachment != null) {
            return player.getLerpedPos(tickDelta).add(attachment).add(0.0D, 0.5D, 0.0D);
        }

        return player.getLerpedPos(tickDelta).add(0.0D, player.getHeight() + 0.55D, 0.0D);
    }

    private Text buildStatsLine(PlayerEntity player) {
        MutableText text = Text.empty();
        text.append(token(formatHealth(player.getHealth() + player.getAbsorptionAmount()), getHealthColor(player.getHealth() + player.getAbsorptionAmount())));
        text.append(Text.literal(" "));
        text.append(token(formatDistance(player), DISTANCE_COLOR));
        text.append(Text.literal(" "));
        text.append(token(formatPing(player), getPingColor(getPing(player))));
        return text;
    }

    private Text token(String value, int color) {
        return ColorUtil.literal("[", BRACKET_COLOR)
                .append(ColorUtil.literal(value, color))
                .append(ColorUtil.literal("]", BRACKET_COLOR));
    }

    private String formatHealth(float health) {
        if (Math.abs(health - Math.round(health)) < 0.05F) {
            return Integer.toString(Math.round(health));
        }
        return String.format(Locale.ROOT, "%.1f", health);
    }

    private String formatDistance(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        return Integer.toString(Math.round(client.player.distanceTo(player))) + "m";
    }

    private String formatPing(PlayerEntity player) {
        return getPing(player) + "ms";
    }

    private int getPing(PlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.getNetworkHandler() == null) {
            return 0;
        }

        PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(player.getUuid());
        return entry == null ? 0 : Math.max(0, entry.getLatency());
    }

    private int getHealthColor(float health) {
        if (health <= 6.0F) {
            return BAD_COLOR;
        }
        if (health <= 12.0F) {
            return MEDIUM_COLOR;
        }
        return GOOD_COLOR;
    }

    private int getPingColor(int ping) {
        if (ping >= 180) {
            return BAD_COLOR;
        }
        if (ping >= 100) {
            return MEDIUM_COLOR;
        }
        return GOOD_COLOR;
    }

    private record ProjectedTag(double screenX, double screenY, Text statsLine, Text nameLine, double scale) {
    }
}

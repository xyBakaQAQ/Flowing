package com.xybaka.flowing.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class RenderUtil {
    private RenderUtil() {
    }

    public static void renderBillboardText(WorldRenderContext context, Vec3d worldPos, double scale, int lineSpacing, int textColor, int backgroundColor, Text... lines) {
        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        MinecraftClient client = MinecraftClient.getInstance();
        if (matrices == null || consumers == null || client.player == null || lines.length == 0) {
            return;
        }

        TextRenderer textRenderer = client.textRenderer;
        int[] widths = new int[lines.length];
        int visibleLines = 0;
        for (int index = 0; index < lines.length; index++) {
            Text line = lines[index];
            widths[index] = textRenderer.getWidth(line);
            if (!line.getString().isEmpty()) {
                visibleLines++;
            }
        }

        if (visibleLines == 0) {
            return;
        }

        float baseScale = (float) (0.025F * scale);
        float startY = -(visibleLines - 1) * lineSpacing / 2.0F;
        Vec3d cameraPos = context.camera().getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        matrices.push();
        matrices.translate(worldPos.x - cameraPos.x, worldPos.y - cameraPos.y, worldPos.z - cameraPos.z);
        matrices.multiply(client.getEntityRenderDispatcher().getRotation());
        matrices.scale(baseScale, -baseScale, baseScale);

        Matrix4f positionMatrix = matrices.peek().getPositionMatrix();
        int renderedLineIndex = 0;
        for (int index = 0; index < lines.length; index++) {
            Text line = lines[index];
            if (line.getString().isEmpty()) {
                continue;
            }

            float x = -widths[index] / 2.0F;
            float y = startY + renderedLineIndex * lineSpacing;
            textRenderer.draw(line, x, y, textColor, false, positionMatrix, consumers, TextRenderer.TextLayerType.SEE_THROUGH, backgroundColor, LightmapTextureManager.MAX_LIGHT_COORDINATE);
            renderedLineIndex++;
        }
        matrices.pop();

        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            immediate.draw();
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static Projection project(Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d worldPos) {
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d cameraRelativePos = worldPos.subtract(client.gameRenderer.getCamera().getPos());
        Vector4f vector = new Vector4f((float) cameraRelativePos.x, (float) cameraRelativePos.y, (float) cameraRelativePos.z, 1.0F);

        vector.mul(positionMatrix);
        vector.mul(projectionMatrix);

        boolean visible = vector.w() > 0.0F;
        if (vector.w() != 0.0F) {
            vector.x /= vector.w();
            vector.y /= vector.w();
            vector.z /= vector.w();
        }

        double screenX = (vector.x() * 0.5D + 0.5D) * client.getWindow().getScaledWidth();
        double screenY = (0.5D - vector.y() * 0.5D) * client.getWindow().getScaledHeight();
        if (screenX < 0.0D || screenX > client.getWindow().getScaledWidth() || screenY < 0.0D || screenY > client.getWindow().getScaledHeight()) {
            visible = false;
        }

        return new Projection(new Vec3d(screenX, screenY, vector.z()), visible);
    }

    public record Projection(Vec3d position, boolean visible) {
    }
}

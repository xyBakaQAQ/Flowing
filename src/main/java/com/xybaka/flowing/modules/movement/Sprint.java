package com.xybaka.flowing.modules.movement;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    @Override
    public void onTick(TickEvent event) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }

        boolean movingForward = player.input.movementForward > 0.0F;
        if (player.isSneaking() || player.horizontalCollision || !movingForward) {
            return;
        }

        player.setSprinting(true);
    }
}
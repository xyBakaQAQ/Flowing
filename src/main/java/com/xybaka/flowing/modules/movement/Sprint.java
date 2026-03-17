package com.xybaka.flowing.modules.movement;

import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import com.xybaka.flowing.util.MoveUtil;
import net.minecraft.client.network.ClientPlayerEntity;
import org.lwjgl.glfw.GLFW;

public final class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT, GLFW.GLFW_KEY_UNKNOWN, true);
    }

    @Override
    public void onTick(TickEvent event) {
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }

        if (player.isSneaking() || player.horizontalCollision || !MoveUtil.isMovingForward(mc)) {
            return;
        }

        player.setSprinting(true);
    }
}
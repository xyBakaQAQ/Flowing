package com.xybaka.flowing.modules.client;

import com.xybaka.flowing.gui.clickgui.ClickGuiScreen;
import com.xybaka.flowing.modules.Category;
import com.xybaka.flowing.modules.Module;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class ClickGUI extends Module {
    public ClickGUI() {
        super("ClickGUI", Category.CLIENT, GLFW.GLFW_KEY_RIGHT_SHIFT, false);
    }

    @Override
    protected void onEnable() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen instanceof ClickGuiScreen) {
            client.setScreen(null);
        } else {
            client.setScreen(new ClickGuiScreen());
        }

        disable();
    }
}

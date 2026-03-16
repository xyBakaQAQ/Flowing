package com.xybaka.flowing;

import com.xybaka.flowing.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Flowing implements ClientModInitializer {
    public static final String MOD_ID = "flowing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> ModuleManager.onTick());
        LOGGER.info("Flowing initialized with {} modules", ModuleManager.getModules().size());
    }
}

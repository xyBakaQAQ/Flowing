package com.xybaka.flowing;

import com.xybaka.flowing.config.ConfigManager;
import com.xybaka.flowing.event.EventManager;
import com.xybaka.flowing.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Flowing implements ClientModInitializer {
    public static final String MOD_ID = "flowing";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ModuleManager.init();
        ConfigManager.init();
        EventManager.init();
        LOGGER.info("Flowing initialized with {} modules", ModuleManager.getModules().size());
    }
}

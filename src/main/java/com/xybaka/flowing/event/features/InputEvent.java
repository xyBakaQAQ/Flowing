package com.xybaka.flowing.event.features;

import com.xybaka.flowing.event.Event;
import com.xybaka.flowing.event.EventListener;
import net.minecraft.client.MinecraftClient;

public final class InputEvent extends Event {
    private final MinecraftClient client;

    public InputEvent(MinecraftClient client) {
        this.client = client;
    }

    public MinecraftClient getClient() {
        return client;
    }

    @Override
    public void call(EventListener listener) {
        listener.onInput(this);
    }
}

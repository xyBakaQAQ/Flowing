package com.xybaka.flowing.event.features;

import com.xybaka.flowing.event.Event;
import com.xybaka.flowing.event.EventListener;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public final class WorldRenderEvent extends Event {
    private final WorldRenderContext context;

    public WorldRenderEvent(WorldRenderContext context) {
        this.context = context;
    }

    public WorldRenderContext getContext() {
        return context;
    }

    @Override
    public void call(EventListener listener) {
        listener.onWorldRender(this);
    }
}

package com.xybaka.flowing.event;

import com.xybaka.flowing.event.features.InputEvent;
import com.xybaka.flowing.event.features.KeyboardEvent;
import com.xybaka.flowing.event.features.TickEvent;
import com.xybaka.flowing.event.features.WorldRenderEvent;

public interface EventListener {
    default void onTick(TickEvent event) {
    }

    default void onInput(InputEvent event) {
    }

    default void onKey(KeyboardEvent event) {
    }

    default void onWorldRender(WorldRenderEvent event) {
    }
}

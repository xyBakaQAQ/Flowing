package com.xybaka.flowing.event.features;

import com.xybaka.flowing.event.Event;
import com.xybaka.flowing.event.EventListener;

public final class TickEvent extends Event {
    @Override
    public void call(EventListener listener) {
        listener.onTick(this);
    }
}

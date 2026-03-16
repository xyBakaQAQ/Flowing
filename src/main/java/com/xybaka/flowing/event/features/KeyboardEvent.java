package com.xybaka.flowing.event.features;

import com.xybaka.flowing.event.Event;
import com.xybaka.flowing.event.EventListener;

public final class KeyboardEvent extends Event {
    private final int key;
    private final int action;

    public KeyboardEvent(int key, int action) {
        this.key = key;
        this.action = action;
    }

    public int getKey() {
        return key;
    }

    public int getAction() {
        return action;
    }

    @Override
    public void call(EventListener listener) {
        listener.onKey(this);
    }
}
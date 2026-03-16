package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.modules.Module;

public abstract class Setting {
    private final String name;
    private final Module parent;

    protected Setting(String name, Module parent) {
        this.name = name;
        this.parent = parent;
    }

    public final String getName() {
        return name;
    }

    public final Module getParent() {
        return parent;
    }
}
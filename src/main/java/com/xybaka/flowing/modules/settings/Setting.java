package com.xybaka.flowing.modules.settings;

import com.xybaka.flowing.modules.Module;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public abstract class Setting {
    private final String name;
    private final Module parent;
    private BooleanSupplier visibleCondition;
    private int indentLevel;

    protected Setting(String name, Module parent) {
        this.name = name;
        this.parent = parent;
        this.visibleCondition = () -> true;
        this.indentLevel = 0;
    }

    public final String getName() {
        return name;
    }

    public final Module getParent() {
        return parent;
    }

    public final boolean isVisible() {
        return visibleCondition.getAsBoolean();
    }

    public final int getIndentLevel() {
        return indentLevel;
    }

    @SuppressWarnings("unchecked")
    public final <T extends Setting> T visibleWhen(BooleanSupplier visibleCondition) {
        this.visibleCondition = Objects.requireNonNull(visibleCondition, "visibleCondition");
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public final <T extends Setting> T childOf(Setting parentSetting) {
        this.indentLevel = Objects.requireNonNull(parentSetting, "parentSetting").getIndentLevel() + 1;
        return (T) this;
    }
}

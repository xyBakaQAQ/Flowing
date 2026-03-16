package com.xybaka.flowing.gui.component;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public final class HudPositionManager {
    private static final HudPositionManager INSTANCE = new HudPositionManager();

    private final Map<String, Position> positions = new HashMap<>();

    private HudPositionManager() {
    }

    public static HudPositionManager getInstance() {
        return INSTANCE;
    }

    public boolean hasPosition(String id) {
        return positions.containsKey(id);
    }

    public int getX(String id, int defaultX, int defaultY) {
        return getOrCreate(id, defaultX, defaultY).x();
    }

    public int getY(String id, int defaultX, int defaultY) {
        return getOrCreate(id, defaultX, defaultY).y();
    }

    public void setPosition(String id, int x, int y) {
        Position current = positions.get(id);
        int safeX = Math.max(0, x);
        int safeY = Math.max(0, y);
        if (current != null && current.x() == safeX && current.y() == safeY) {
            return;
        }
        positions.put(id, new Position(safeX, safeY));
    }

    public void load(JsonObject componentsObject) {
        positions.clear();
        if (componentsObject == null) {
            return;
        }

        for (String key : componentsObject.keySet()) {
            JsonObject componentObject = componentsObject.getAsJsonObject(key);
            if (componentObject == null) {
                continue;
            }

            int x = componentObject.has("x") ? componentObject.get("x").getAsInt() : 0;
            int y = componentObject.has("y") ? componentObject.get("y").getAsInt() : 0;
            positions.put(key, new Position(Math.max(0, x), Math.max(0, y)));
        }
    }

    public JsonObject save() {
        JsonObject componentsObject = new JsonObject();
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            JsonObject componentObject = new JsonObject();
            componentObject.addProperty("x", entry.getValue().x());
            componentObject.addProperty("y", entry.getValue().y());
            componentsObject.add(entry.getKey(), componentObject);
        }
        return componentsObject;
    }

    private Position getOrCreate(String id, int defaultX, int defaultY) {
        return positions.computeIfAbsent(id, key -> new Position(Math.max(0, defaultX), Math.max(0, defaultY)));
    }

    private record Position(int x, int y) {
    }
}

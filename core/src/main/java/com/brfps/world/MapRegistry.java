package com.brfps.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/** Registry of playable maps, loaded from assets/maps/index.json (adding a map = JSON only). */
public class MapRegistry {

    private static final String INDEX_PATH = "maps/index.json";

    private final ObjectMap<String, String> displayNames = new ObjectMap<>();
    private final ObjectMap<String, FileHandle> files = new ObjectMap<>();

    /** Parses the map index file; clears any previous entries first. */
    public void load() {
        displayNames.clear();
        files.clear();
        FileHandle index = Gdx.files.internal(INDEX_PATH);
        if (!index.exists()) {
            Gdx.app.error("MapRegistry", "missing " + INDEX_PATH);
            return;
        }
        JsonValue root;
        try {
            root = new JsonReader().parse(index);
        } catch (Exception e) {
            Gdx.app.error("MapRegistry", "bad JSON in " + INDEX_PATH, e);
            return;
        }
        JsonValue maps = root.get("maps");
        if (maps == null) {
            Gdx.app.error("MapRegistry", INDEX_PATH + " has no \"maps\" array");
            return;
        }
        for (JsonValue entry : maps) {
            String id = entry.getString("id", null);
            String file = entry.getString("file", null);
            if (id == null || file == null) {
                Gdx.app.error("MapRegistry", "index entry missing id/file, skipped");
                continue;
            }
            displayNames.put(id, entry.getString("name", id));
            files.put(id, Gdx.files.internal("maps/" + file));
        }
    }

    /** All registered map ids in index order. */
    public Array<String> getMapIds() {
        return files.keys().toArray();
    }

    /** Human-readable name for a map id. */
    public String getDisplayName(String id) {
        return displayNames.get(id, id);
    }

    /** Parses the layout for a map id; logs errors and returns null on failure. */
    public MapLayout loadMap(String id) {
        if (id == null) {
            return null;
        }
        FileHandle file = files.get(id);
        if (file == null) {
            Gdx.app.error("MapRegistry", "unknown map id " + id);
            return null;
        }
        if (!file.exists()) {
            Gdx.app.error("MapRegistry", "missing layout file " + file.path());
            return null;
        }
        return MapLayout.load(file);
    }

    /** Id of the map to launch with ("island" when registered, else the first entry). */
    public String getDefaultMapId() {
        if (files.containsKey("island")) {
            return "island";
        }
        return files.size == 0 ? null : files.keys().next();
    }
}

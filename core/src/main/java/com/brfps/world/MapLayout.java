package com.brfps.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Parsed *.layout map definition (see assets/maps/island.layout for the
 * schema). Unknown JSON fields are ignored so new maps stay
 * forward-compatible with older code.
 */
public class MapLayout {

    /** Rectangular zone definition. */
    public static class ZoneDef {
        public String id = "";
        public String name = "";
        public float x;
        public float z;
        public float width;
        public float depth;
        public String ground = "grass";
    }

    /** Straight road segment. */
    public static class RoadDef {
        public float fromX;
        public float fromZ;
        public float toX;
        public float toZ;
        public float width = 5f;
    }

    /** Polyline river channel (x/z pairs in {@link #points}; bridge comes in M2d). */
    public static class RiverDef {
        public final FloatArray points = new FloatArray();
        public float width = 8f;
        public float depth;
    }

    /** Player/bot spawn position. */
    public static class SpawnDef {
        public float x;
        public float z;
        public String zone = "";
    }

    /** Ground loot position (used from M6a). */
    public static class LootSpawnDef {
        public float x;
        public float z;
    }

    /** Building placement (used from M2b). */
    public static class BuildingDef {
        public String type = "";
        public float x;
        public float z;
        public float rotation;
    }

    /** Prop placement (used from M2d). */
    public static class PropDef {
        public String type = "";
        public float x;
        public float z;
        public float rotation;
        public float scale = 1f;
    }

    public String id = "unknown";
    public String name = "Unnamed";
    public int version = 1;
    public float size = 300f;
    public String terrain = "tropical";
    public boolean water;
    public float waterLevel = -0.2f;
    public float waterMargin = 60f;
    public float beachWidth = 6f;

    public final Array<ZoneDef> zones = new Array<>();
    public final Array<RoadDef> roads = new Array<>();
    public final Array<RiverDef> rivers = new Array<>();
    public final Array<BuildingDef> buildings = new Array<>();
    public final Array<PropDef> props = new Array<>();
    public final Array<SpawnDef> spawnPoints = new Array<>();
    public final Array<LootSpawnDef> lootSpawns = new Array<>();

    /** Parses a layout JSON file; logs errors and returns null on fatal problems. */
    public static MapLayout load(FileHandle file) {
        JsonValue root;
        try {
            root = new JsonReader().parse(file);
        } catch (Exception e) {
            Gdx.app.error("MapLayout", "bad JSON in " + file.path(), e);
            return null;
        }

        MapLayout map = new MapLayout();
        map.id = root.getString("id", map.id);
        map.name = root.getString("name", map.name);
        map.version = root.getInt("version", 1);
        map.size = root.getFloat("size", map.size);
        map.terrain = root.getString("terrain", map.terrain);
        map.water = root.getBoolean("water", false);
        map.waterLevel = root.getFloat("waterLevel", map.waterLevel);
        map.waterMargin = root.getFloat("waterMargin", map.size * 0.2f);
        map.beachWidth = root.getFloat("beachWidth", 6f);

        if (map.size <= 0f) {
            Gdx.app.error("MapLayout", file.name() + ": size must be positive");
            return null;
        }

        parseZones(root, map);
        parseRoads(root, map);
        parseRivers(root, map);
        parseBuildings(root, map);
        parseProps(root, map);
        parseSpawns(root, map);
        parseLootSpawns(root, map);
        return map;
    }

    /** Converts parsed spawn defs into SpawnPoint instances for the game. */
    public Array<SpawnPoint> createSpawnPoints() {
        Array<SpawnPoint> out = new Array<>();
        for (SpawnDef def : spawnPoints) {
            out.add(new SpawnPoint(def.x, def.z, def.zone));
        }
        return out;
    }

    private static void parseZones(JsonValue root, MapLayout map) {
        JsonValue list = root.get("zones");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            ZoneDef zone = new ZoneDef();
            zone.id = v.getString("id", zone.id);
            zone.name = v.getString("name", zone.id);
            zone.x = v.getFloat("x", 0f);
            zone.z = v.getFloat("z", 0f);
            zone.width = v.getFloat("width", 0f);
            zone.depth = v.getFloat("depth", 0f);
            zone.ground = v.getString("ground", zone.ground);
            map.zones.add(zone);
        }
    }

    private static void parseRoads(JsonValue root, MapLayout map) {
        JsonValue list = root.get("roads");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            JsonValue from = v.get("from");
            JsonValue to = v.get("to");
            if (from == null || to == null || from.size < 2 || to.size < 2) {
                Gdx.app.error("MapLayout", "road entry missing from/to points, skipped");
                continue;
            }
            RoadDef road = new RoadDef();
            road.fromX = from.child.asFloat();
            road.fromZ = from.child.next.asFloat();
            road.toX = to.child.asFloat();
            road.toZ = to.child.next.asFloat();
            road.width = v.getFloat("width", road.width);
            map.roads.add(road);
        }
    }

    private static void parseRivers(JsonValue root, MapLayout map) {
        JsonValue list = root.get("rivers");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            RiverDef river = new RiverDef();
            JsonValue points = v.get("points");
            if (points != null) {
                for (JsonValue point : points) {
                    if (point.size < 2) {
                        continue;
                    }
                    river.points.add(point.child.asFloat());
                    river.points.add(point.child.next.asFloat());
                }
            }
            if (river.points.size < 4) {
                Gdx.app.error("MapLayout", "river needs at least 2 points, skipped");
                continue;
            }
            river.width = v.getFloat("width", river.width);
            river.depth = v.getFloat("depth", river.depth);
            map.rivers.add(river);
        }
    }

    private static void parseBuildings(JsonValue root, MapLayout map) {
        JsonValue list = root.get("buildings");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            BuildingDef def = new BuildingDef();
            def.type = v.getString("type", def.type);
            def.x = v.getFloat("x", 0f);
            def.z = v.getFloat("z", 0f);
            def.rotation = v.getFloat("rotation", 0f);
            map.buildings.add(def);
        }
    }

    private static void parseProps(JsonValue root, MapLayout map) {
        JsonValue list = root.get("props");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            PropDef def = new PropDef();
            def.type = v.getString("type", def.type);
            def.x = v.getFloat("x", 0f);
            def.z = v.getFloat("z", 0f);
            def.rotation = v.getFloat("rotation", 0f);
            def.scale = v.getFloat("scale", def.scale);
            map.props.add(def);
        }
    }

    private static void parseSpawns(JsonValue root, MapLayout map) {
        JsonValue list = root.get("spawnPoints");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            SpawnDef def = new SpawnDef();
            def.x = v.getFloat("x", 0f);
            def.z = v.getFloat("z", 0f);
            def.zone = v.getString("zone", def.zone);
            map.spawnPoints.add(def);
        }
    }

    private static void parseLootSpawns(JsonValue root, MapLayout map) {
        JsonValue list = root.get("lootSpawns");
        if (list == null) {
            return;
        }
        for (JsonValue v : list) {
            LootSpawnDef def = new LootSpawnDef();
            def.x = v.getFloat("x", 0f);
            def.z = v.getFloat("z", 0f);
            map.lootSpawns.add(def);
        }
    }
}

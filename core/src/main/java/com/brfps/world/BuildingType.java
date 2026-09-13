package com.brfps.world;

import com.badlogic.gdx.graphics.Color;
import com.brfps.util.Constants;

/**
 * The fixed 8-building kit: footprint, wall height, roof style, plinth and palette
 * for every building a map layout may place (per-type art data lives here, shared
 * dimensions such as door/window/wall thickness live in Constants — R7).
 */
public enum BuildingType {

    /** Small one-room house, pitched roof, shell only. */
    HOUSE_SMALL("house_small", 6f, 5f, 3.2f, Roof.PITCHED, false, 0f,
            new Color(0.86f, 0.80f, 0.68f, 1f), new Color(0.62f, 0.30f, 0.22f, 1f),
            new Color(0.55f, 0.42f, 0.28f, 1f)),

    /** Two-storey house, pitched roof, walkable interior + mid floor. */
    HOUSE_TWO_STOREY("house_two_storey", 7f, 6f, 6.4f, Roof.PITCHED, true, 0f,
            new Color(0.72f, 0.76f, 0.80f, 1f), new Color(0.50f, 0.24f, 0.20f, 1f),
            new Color(0.52f, 0.40f, 0.30f, 1f)),

    /** Shop with a flat roof and a front awning, walkable interior. */
    SHOP("shop", 9f, 6f, 3.8f, Roof.FLAT, true, 0f,
            new Color(0.90f, 0.78f, 0.45f, 1f), new Color(0.45f, 0.45f, 0.48f, 1f),
            new Color(0.62f, 0.62f, 0.60f, 1f)),

    /** Village hut, steep thatch roof, shell only. */
    HUT("hut", 4f, 4f, 2.6f, Roof.PITCHED, false, 0f,
            new Color(0.62f, 0.50f, 0.34f, 1f), new Color(0.72f, 0.60f, 0.30f, 1f),
            new Color(0.48f, 0.38f, 0.26f, 1f)),

    /** Temple on a plinth with a stepped tower, walkable interior. */
    TEMPLE("temple", 10f, 8f, 5.4f, Roof.FLAT, true, 0.5f,
            new Color(0.88f, 0.85f, 0.78f, 1f), new Color(0.90f, 0.55f, 0.20f, 1f),
            new Color(0.70f, 0.68f, 0.62f, 1f)),

    /** Industrial warehouse (placed from M2c / master M16), flat roof, big door. */
    WAREHOUSE("warehouse", 16f, 10f, 6f, Roof.FLAT, true, 0f,
            new Color(0.45f, 0.55f, 0.65f, 1f), new Color(0.35f, 0.36f, 0.38f, 1f),
            new Color(0.55f, 0.55f, 0.55f, 1f)),

    /** Factory with a chimney (placed from M2c / master M16), flat roof. */
    FACTORY("factory", 14f, 12f, 8f, Roof.FLAT, true, 0f,
            new Color(0.60f, 0.42f, 0.36f, 1f), new Color(0.38f, 0.38f, 0.40f, 1f),
            new Color(0.50f, 0.50f, 0.50f, 1f)),

    /** Military barracks (placed from M2c / master M17), flat roof, big door. */
    BARRACKS("barracks", 12f, 6f, 3.6f, Roof.FLAT, true, 0f,
            new Color(0.50f, 0.55f, 0.38f, 1f), new Color(0.35f, 0.40f, 0.28f, 1f),
            new Color(0.52f, 0.50f, 0.44f, 1f));

    /** Roof shape: flat slab or pitched ridge along the local X axis. */
    public enum Roof {
        FLAT, PITCHED
    }

    /** String used by `buildings[].type` in a map layout file. */
    public final String id;
    /** Footprint width along local X, in meters. */
    public final float width;
    /** Footprint depth along local Z, in meters. */
    public final float depth;
    /** Wall height above the plinth, in meters. */
    public final float wallHeight;
    /** Flat or pitched. */
    public final Roof roof;
    /** True when the inside is modelled (floor, partition, open doorway). */
    public final boolean hasInterior;
    /** Height of the stone plinth under the walls (0 for most types). */
    public final float plinthHeight;
    /** Wall color. */
    public final Color wall;
    /** Roof color. */
    public final Color roofColor;
    /** Floor / plinth color. */
    public final Color floor;

    BuildingType(String id, float width, float depth, float wallHeight, Roof roof,
                 boolean hasInterior, float plinthHeight,
                 Color wall, Color roofColor, Color floor) {
        this.id = id;
        this.width = width;
        this.depth = depth;
        this.wallHeight = wallHeight;
        this.roof = roof;
        this.hasInterior = hasInterior;
        this.plinthHeight = plinthHeight;
        this.wall = wall;
        this.roofColor = roofColor;
        this.floor = floor;
    }

    /** Looks a type up by its layout id; returns null when the id is unknown. */
    public static BuildingType fromId(String id) {
        if (id == null) {
            return null;
        }
        for (BuildingType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }

    /** Door width for this footprint: wide types get a double door. */
    public float doorWidth() {
        return width >= 10f ? Constants.DOOR_WIDTH * 2f : Constants.DOOR_WIDTH;
    }
}

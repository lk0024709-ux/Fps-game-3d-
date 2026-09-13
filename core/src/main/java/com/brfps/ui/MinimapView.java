package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.brfps.util.Constants;
import com.brfps.world.BuildingType;
import com.brfps.world.MapLayout;

/**
 * Circular minimap, top-left (HUD phase A3): building footprints, the dotted
 * safe-zone ring and a player arrow that rotates with the aim, all drawn in the HUD
 * batch (R28). North is up (-Z, matching the compass). Tapping it toggles between
 * the whole map and a zoomed player-centred view; the fullscreen map is Phase B.
 * Building rects are baked once from the layout, so drawing allocates nothing (R6).
 */
public class MinimapView {

    /** Floats per baked building: world x, world z, width, depth. */
    private static final int FIELDS = 4;

    private final float[] baked;
    private final float mapSize;
    private boolean zoomed;

    /** Bakes the layout's buildings into flat rects (rotation swaps w/d at 90/270). */
    public MinimapView(Array<MapLayout.BuildingDef> buildings, float mapSize) {
        this.mapSize = mapSize;
        baked = new float[buildings.size * FIELDS];
        for (int i = 0; i < buildings.size; i++) {
            MapLayout.BuildingDef def = buildings.get(i);
            BuildingType type = BuildingType.fromId(def.type);
            float w = type == null ? 6f : type.width;
            float d = type == null ? 6f : type.depth;
            if (Math.abs(Math.round(def.rotation / 90f)) % 2 == 1) {
                float swap = w;
                w = d;
                d = swap;
            }
            int o = i * FIELDS;
            baked[o] = def.x;
            baked[o + 1] = def.z;
            baked[o + 2] = w;
            baked[o + 3] = d;
        }
    }

    /** Minimap diameter in pixels for the current screen. */
    public static float diameter(float width, float height) {
        return Math.min(width, height) * Constants.MINIMAP_SIZE;
    }

    /** Centre x in pixels (batch coords). */
    public static float centerX(float width, float height) {
        return Math.min(width, height) * Constants.TOUCH_MARGIN
                + diameter(width, height) * 0.5f;
    }

    /** Centre y in pixels (batch y-up coords). */
    public static float centerY(float width, float height) {
        return height - Math.min(width, height) * Constants.TOUCH_MARGIN
                - diameter(width, height) * 0.5f;
    }

    /** True when a touch point (y-down pixels) falls inside the disc. */
    public static boolean hit(float x, float y, float width, float height) {
        float dx = x - centerX(width, height);
        float dy = (height - y) - centerY(width, height);
        float radius = diameter(width, height) * 0.5f;
        return dx * dx + dy * dy <= radius * radius;
    }

    /** Toggles whole-map / zoomed view (minimap tap or M key). */
    public void toggleZoom() {
        zoomed = !zoomed;
    }

    public boolean isZoomed() {
        return zoomed;
    }

    /** Draws the disc; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, TextureRegion whiteRegion,
                       Texture circle, HudData data, float width, float height) {
        float size = diameter(width, height);
        float cx = centerX(width, height);
        float cy = centerY(width, height);
        float radius = size * 0.5f;

        batch.setColor(1f, 1f, 1f, 0.30f); // pale rim behind the dark disc
        batch.draw(circle, cx - radius - 2f, cy - radius - 2f, size + 4f, size + 4f);
        batch.setColor(0.04f, 0.07f, 0.11f, 0.72f);
        batch.draw(circle, cx - radius, cy - radius, size, size);

        float scale = size / mapSize * (zoomed ? Constants.MINIMAP_ZOOM : 1f);
        float focusX = zoomed ? data.positionX : 0f;
        float focusZ = zoomed ? data.positionZ : 0f;
        drawBuildings(batch, white, cx, cy, radius, scale, focusX, focusZ);
        drawZone(batch, white, data, cx, cy, radius, scale, focusX, focusZ);
        drawPlayer(batch, whiteRegion, circle, data, cx, cy, radius, scale,
                focusX, focusZ);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** Grey footprint per building, skipped outside the disc. */
    private void drawBuildings(SpriteBatch batch, Texture white, float cx, float cy,
                               float radius, float scale, float focusX, float focusZ) {
        batch.setColor(0.55f, 0.55f, 0.57f, 0.85f);
        for (int o = 0; o < baked.length; o += FIELDS) {
            float sx = cx + (baked[o] - focusX) * scale;
            float sy = cy - (baked[o + 1] - focusZ) * scale;
            float dx = sx - cx;
            float dy = sy - cy;
            if (dx * dx + dy * dy > radius * radius) {
                continue;
            }
            float w = Math.max(2f, baked[o + 2] * scale);
            float d = Math.max(2f, baked[o + 3] * scale);
            batch.draw(white, sx - w * 0.5f, sy - d * 0.5f, w, d);
        }
    }

    /** Safe-zone ring as dots (only the ones landing inside the disc). */
    private void drawZone(SpriteBatch batch, Texture white, HudData data,
                          float cx, float cy, float radius, float scale,
                          float focusX, float focusZ) {
        batch.setColor(0.25f, 0.55f, 1f, 0.9f);
        float dot = Math.max(1.5f, radius * 0.03f);
        for (int k = 0; k < Constants.MINIMAP_DOTS; k++) {
            double angle = k * Math.PI * 2.0 / Constants.MINIMAP_DOTS;
            float worldX = data.zoneCenterX + data.zoneRadius * (float) Math.cos(angle);
            float worldZ = data.zoneCenterZ + data.zoneRadius * (float) Math.sin(angle);
            float sx = cx + (worldX - focusX) * scale;
            float sy = cy - (worldZ - focusZ) * scale;
            float dx = sx - cx;
            float dy = sy - cy;
            if (dx * dx + dy * dy > (radius - dot) * (radius - dot)) {
                continue;
            }
            batch.draw(white, sx - dot * 0.5f, sy - dot * 0.5f, dot, dot);
        }
    }

    /** White dot plus a tick pointing along the aim yaw (north = up). */
    private void drawPlayer(SpriteBatch batch, TextureRegion whiteRegion, Texture circle,
                            HudData data, float cx, float cy, float radius, float scale,
                            float focusX, float focusZ) {
        float sx = cx + (data.positionX - focusX) * scale;
        float sy = cy - (data.positionZ - focusZ) * scale;
        float dx = sx - cx;
        float dy = sy - cy;
        if (dx * dx + dy * dy > radius * radius) {
            return; // zoomed out further than the disc (should not happen)
        }
        float length = radius * 0.30f;
        float thick = Math.max(2f, radius * 0.035f);
        batch.setColor(1f, 1f, 1f, 0.95f);
        // Rotating the up-vector by -(yaw + 90) points it along the aim on a
        // north-up map: yaw 0 (+X) reads right, yaw 90 (+Z) reads down.
        batch.draw(whiteRegion, sx - thick * 0.5f, sy, thick * 0.5f, 0f,
                thick, length, 1f, 1f, -(data.yawDegrees + 90f));
        float dot = thick * 2f;
        batch.draw(circle, sx - dot * 0.5f, sy - dot * 0.5f, dot, dot);
    }
}

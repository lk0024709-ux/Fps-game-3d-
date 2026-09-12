package com.brfps.world;

import com.badlogic.gdx.math.Vector2;
import com.brfps.util.Constants;

/**
 * Battle-royale ring. Shrinks one step every ZONE_SHRINK_INTERVAL seconds and
 * damages anything left outside.
 */
public class SafeZone {

    private final Vector2 center = new Vector2();
    private float radius = Constants.ZONE_START_RADIUS;
    private float targetRadius = Constants.ZONE_START_RADIUS;
    private float timer;
    private int stage;

    public SafeZone(float centerX, float centerZ) {
        center.set(centerX, centerZ);
    }

    public Vector2 getCenter() {
        return center;
    }

    public float getRadius() {
        return radius;
    }

    public int getStage() {
        return stage;
    }

    /** Seconds until the next shrink begins. */
    public float getTimeToNextShrink() {
        return Math.max(0f, Constants.ZONE_SHRINK_INTERVAL - timer);
    }

    public void update(float delta) {
        timer += delta;
        if (timer >= Constants.ZONE_SHRINK_INTERVAL) {
            timer = 0f;
            stage++;
            targetRadius = Math.max(Constants.ZONE_MIN_RADIUS, targetRadius * Constants.ZONE_SHRINK_FACTOR);
        }
        if (radius > targetRadius) {
            // Shrink smoothly over roughly 20 seconds.
            float step = (Constants.ZONE_START_RADIUS / 20f) * delta;
            radius = Math.max(targetRadius, radius - step);
        }
    }

    public boolean contains(float x, float z) {
        float dx = x - center.x;
        float dz = z - center.y;
        return dx * dx + dz * dz <= radius * radius;
    }

    /** Damage to apply this frame for a position outside the ring. */
    public float damageFor(float x, float z, float delta) {
        return contains(x, z) ? 0f : Constants.ZONE_DAMAGE_PER_SECOND * delta;
    }

    public void reset() {
        radius = Constants.ZONE_START_RADIUS;
        targetRadius = Constants.ZONE_START_RADIUS;
        timer = 0f;
        stage = 0;
    }
}

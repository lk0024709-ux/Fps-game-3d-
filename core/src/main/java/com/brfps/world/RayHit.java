package com.brfps.world;

/**
 * Result of one hitscan ray, reused for every shot so firing never allocates (R6).
 * `accept` keeps the nearest candidate, so walls, plinth tops and ground can be
 * tested in any order.
 */
public class RayHit {

    /** What a shot can end on. */
    public enum Surface { NONE, WALL, GROUND, PLINTH }

    /** True when something was hit within the weapon's range. */
    public boolean hit;

    /** Distance from the ray origin to the impact point, in meters. */
    public float distance;

    /** Impact point. */
    public float x;
    public float y;
    public float z;

    /** Unit surface normal at the impact point (points back at the shooter). */
    public float normalX;
    public float normalY;
    public float normalZ;

    /** Which kind of surface was hit. */
    public Surface surface = Surface.NONE;

    /** Resets to "no hit". */
    public void clear() {
        hit = false;
        distance = 0f;
        x = 0f;
        y = 0f;
        z = 0f;
        normalX = 0f;
        normalY = 0f;
        normalZ = 0f;
        surface = Surface.NONE;
    }

    /** Records this candidate when it is nearer than the current hit. */
    public boolean accept(float distance, float x, float y, float z,
                          float normalX, float normalY, float normalZ, Surface surface) {
        if (hit && distance >= this.distance) {
            return false;
        }
        this.hit = true;
        this.distance = distance;
        this.x = x;
        this.y = y;
        this.z = z;
        this.normalX = normalX;
        this.normalY = normalY;
        this.normalZ = normalZ;
        this.surface = surface;
        return true;
    }
}

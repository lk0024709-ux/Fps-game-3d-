package com.brfps.world;

import com.badlogic.gdx.math.Vector3;

/** A named drop/spawn position on the map, tagged with its zone id. */
public class SpawnPoint {

    public final float x;
    public final float z;
    public final String zoneId;
    private final Vector3 position = new Vector3();

    public SpawnPoint(float x, float z, String zoneId) {
        this.x = x;
        this.z = z;
        this.zoneId = zoneId == null ? "" : zoneId;
        position.set(x, 0f, z);
    }

    /** World position (y is the terrain height; flat 0 until the M2e heightfield). */
    public Vector3 getPosition() {
        return position;
    }
}

package com.brfps.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.brfps.util.Constants;
import com.brfps.world.MapLayout.BuildingDef;

/**
 * Static collision for buildings: every wall segment (the front wall is split into two
 * door jambs, so doorways stay walkable), every interior partition and every walkable
 * plinth is baked once into flat float arrays of oriented boxes. A circle is then pushed
 * out of them each frame, with no allocation and no per-frame geometry rebuilds
 * (R6, R30). Boxes mirror BuildingFactory's geometry exactly, in the same local space.
 */
public class BuildingCollider {

    /** cx, cz, cos, sin, halfWidth, halfDepth, topY (0 for walls, height for plinths). */
    private static final int FIELDS = 7;

    private final float[] walls;
    private final int wallCount;
    private final float[] plinths;
    private final int plinthCount;

    public BuildingCollider(Array<BuildingDef> defs) {
        FloatArray wallData = new FloatArray(1024);
        FloatArray plinthData = new FloatArray(64);
        for (int i = 0; i < defs.size; i++) {
            BuildingDef def = defs.get(i);
            BuildingType type = BuildingType.fromId(def.type);
            if (type == null) {
                continue; // same skip rule as the batcher: unknown ids are ignored
            }
            float cos = MathUtils.cosDeg(def.rotation);
            float sin = MathUtils.sinDeg(def.rotation);
            float hw = type.width * 0.5f;
            float hd = type.depth * 0.5f;
            float t = Constants.WALL_THICKNESS;

            addWall(wallData, def, cos, sin, 0f, -hd + t * 0.5f, hw, t * 0.5f);
            addWall(wallData, def, cos, sin, hw - t * 0.5f, 0f, t * 0.5f, hd);
            addWall(wallData, def, cos, sin, -hw + t * 0.5f, 0f, t * 0.5f, hd);
            addDoorJambs(wallData, def, cos, sin, type, hw, hd, t);
            if (type.hasInterior) {
                addPartition(wallData, def, cos, sin, hw, hd, t);
            }
            if (type.plinthHeight > 0f) {
                addPlinths(plinthData, def, cos, sin, type, hw, hd);
            }
        }
        walls = wallData.toArray();
        wallCount = wallData.size / FIELDS;
        plinths = plinthData.toArray();
        plinthCount = plinthData.size / FIELDS;
    }

    /** Back wall (-Z), full width. */
    private static void addWall(FloatArray out, BuildingDef def, float cos, float sin,
                                float localX, float localZ, float halfWidth, float halfDepth) {
        out.add(def.x + localX * cos - localZ * sin);
        out.add(def.z + localX * sin + localZ * cos);
        out.add(cos);
        out.add(sin);
        out.add(halfWidth);
        out.add(halfDepth);
        out.add(0f);
    }

    /** Front wall (+Z) as two jambs either side of the doorway; the lintel is head-high. */
    private static void addDoorJambs(FloatArray out, BuildingDef def, float cos, float sin,
                                     BuildingType type, float hw, float hd, float t) {
        float doorHalf = type.doorWidth() * 0.5f;
        float jambHalf = (hw - doorHalf) * 0.5f;
        if (jambHalf <= 0.01f) {
            return; // wall entirely door
        }
        float jambCenter = (hw + doorHalf) * 0.5f;
        addWall(out, def, cos, sin, -jambCenter, hd - t * 0.5f, jambHalf, t * 0.5f);
        addWall(out, def, cos, sin, jambCenter, hd - t * 0.5f, jambHalf, t * 0.5f);
    }

    /** Interior partition wall, matching BuildingFactory's gap on the +Z side. */
    private static void addPartition(FloatArray out, BuildingDef def, float cos, float sin,
                                     float hw, float hd, float t) {
        float partX = -hw * 0.3f;
        float zStart = -hd + t;
        float zEnd = hd * 0.25f;
        addWall(out, def, cos, sin, partX + t * 0.5f, (zStart + zEnd) * 0.5f,
                t * 0.5f, (zEnd - zStart) * 0.5f);
    }

    /** Plinth slab plus its front steps, both walkable ground for the player. */
    private static void addPlinths(FloatArray out, BuildingDef def, float cos, float sin,
                                   BuildingType type, float hw, float hd) {
        addPlinth(out, def, cos, sin, 0f, 0f, hw + 0.8f, hd + 0.8f, type.plinthHeight);
        float stepHalf = type.doorWidth() * 0.5f + 0.6f;
        addPlinth(out, def, cos, sin, 0f, hd + 1.3f, stepHalf, 0.5f, type.plinthHeight * 0.5f);
    }

    private static void addPlinth(FloatArray out, BuildingDef def, float cos, float sin,
                                  float localX, float localZ, float halfWidth, float halfDepth,
                                  float height) {
        out.add(def.x + localX * cos - localZ * sin);
        out.add(def.z + localX * sin + localZ * cos);
        out.add(cos);
        out.add(sin);
        out.add(halfWidth);
        out.add(halfDepth);
        out.add(height);
    }

    /**
     * Pushes a circle out of every wall it overlaps, along the axis of least
     * penetration, so sliding along a wall feels natural.
     */
    public void resolve(Vector3 position, float radius) {
        for (int i = 0; i < wallCount; i++) {
            int o = i * FIELDS;
            float cx = walls[o];
            float cz = walls[o + 1];
            float dx = position.x - cx;
            float dz = position.z - cz;
            float reach = walls[o + 4] + walls[o + 5] + radius;
            if (dx * dx + dz * dz > reach * reach) {
                continue; // broad-phase reject
            }
            float cos = walls[o + 2];
            float sin = walls[o + 3];
            float localX = dx * cos + dz * sin;
            float localZ = -dx * sin + dz * cos;
            float penetrationX = walls[o + 4] + radius - Math.abs(localX);
            float penetrationZ = walls[o + 5] + radius - Math.abs(localZ);
            if (penetrationX <= 0f || penetrationZ <= 0f) {
                continue;
            }
            if (penetrationX < penetrationZ) {
                localX += localX < 0f ? -penetrationX : penetrationX;
            } else {
                localZ += localZ < 0f ? -penetrationZ : penetrationZ;
            }
            position.x = cx + localX * cos - localZ * sin;
            position.z = cz + localX * sin + localZ * cos;
        }
    }

    /** Ground height under a point: walkable plinths and steps, 0 everywhere else. */
    public float groundHeightAt(float x, float z) {
        float best = 0f;
        for (int i = 0; i < plinthCount; i++) {
            int o = i * FIELDS;
            float dx = x - plinths[o];
            float dz = z - plinths[o + 1];
            if (plinths[o + 6] <= best) {
                continue;
            }
            float reach = plinths[o + 4] + plinths[o + 5];
            if (dx * dx + dz * dz > reach * reach) {
                continue;
            }
            float cos = plinths[o + 2];
            float sin = plinths[o + 3];
            float localX = dx * cos + dz * sin;
            float localZ = -dx * sin + dz * cos;
            if (Math.abs(localX) <= plinths[o + 4] && Math.abs(localZ) <= plinths[o + 5]) {
                best = plinths[o + 6];
            }
        }
        return best;
    }

    /** Wall boxes built (debug/HUD). */
    public int getWallCount() {
        return wallCount;
    }

    /** Walkable plinth boxes built (debug/HUD). */
    public int getPlinthCount() {
        return plinthCount;
    }
}

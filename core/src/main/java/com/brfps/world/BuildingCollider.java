package com.brfps.world;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.brfps.world.MapLayout.BuildingDef;

/**
 * Queries the map's static collision boxes, baked once by {@link ColliderBoxes}:
 * pushes the player's circle out of walls, reports walkable ground height under a
 * point, and answers hitscan rays. All three run without allocation (R6, R30), so
 * they are safe to call every frame or every shot.
 */
public class BuildingCollider {

    /** Rays more parallel to a slab than this slide along it instead of dividing by 0. */
    private static final float EPSILON = 1e-6f;

    private final ColliderBoxes boxes;
    private final float halfSize;

    public BuildingCollider(Array<BuildingDef> defs, float mapSize) {
        this.boxes = new ColliderBoxes(defs);
        this.halfSize = mapSize * 0.5f;
    }

    /**
     * Pushes a circle out of every wall it overlaps, along the axis of least
     * penetration, so sliding along a wall feels natural.
     */
    public void resolve(Vector3 position, float radius) {
        float[] walls = boxes.getWalls();
        int count = boxes.getWallCount();
        for (int i = 0; i < count; i++) {
            int o = i * ColliderBoxes.FIELDS;
            float centerX = walls[o];
            float centerZ = walls[o + 1];
            float dx = position.x - centerX;
            float dz = position.z - centerZ;
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
            position.x = centerX + localX * cos - localZ * sin;
            position.z = centerZ + localX * sin + localZ * cos;
        }
    }

    /** Ground height under a point: walkable plinths and steps, 0 everywhere else. */
    public float groundHeightAt(float x, float z) {
        float[] plinths = boxes.getPlinths();
        int count = boxes.getPlinthCount();
        float best = 0f;
        for (int i = 0; i < count; i++) {
            int o = i * ColliderBoxes.FIELDS;
            float height = plinths[o + 7];
            if (height <= best) {
                continue;
            }
            float dx = x - plinths[o];
            float dz = z - plinths[o + 1];
            float reach = plinths[o + 4] + plinths[o + 5];
            if (dx * dx + dz * dz > reach * reach) {
                continue;
            }
            if (insideBox(plinths, o, dx, dz)) {
                best = height;
            }
        }
        return best;
    }

    /** True when an offset (dx, dz) from box o falls inside its footprint. */
    private static boolean insideBox(float[] data, int o, float dx, float dz) {
        float cos = data[o + 2];
        float sin = data[o + 3];
        float localX = dx * cos + dz * sin;
        float localZ = -dx * sin + dz * cos;
        return Math.abs(localX) <= data[o + 4] && Math.abs(localZ) <= data[o + 5];
    }

    /**
     * Nearest hit of a hitscan ray: wall boxes over their real height range, plinth
     * tops and the flat island ground inside the map bounds. The direction must be a
     * unit vector so that {@code distance} is in meters.
     *
     * @param maxDistance   the weapon's range; hits beyond it are ignored
     * @param out           reused result, cleared first (never allocated per shot)
     * @return true when something was hit within maxDistance
     */
    public boolean raycast(float originX, float originY, float originZ,
                           float dirX, float dirY, float dirZ,
                           float maxDistance, RayHit out) {
        out.clear();
        castWalls(originX, originY, originZ, dirX, dirY, dirZ, maxDistance, out);
        castGround(originX, originY, originZ, dirX, dirY, dirZ, maxDistance, out);
        return out.hit;
    }

    /** Slab test per wall box, done in the box's own local space. */
    private void castWalls(float originX, float originY, float originZ,
                           float dirX, float dirY, float dirZ,
                           float maxDistance, RayHit out) {
        float[] walls = boxes.getWalls();
        int count = boxes.getWallCount();
        for (int i = 0; i < count; i++) {
            int o = i * ColliderBoxes.FIELDS;
            float cos = walls[o + 2];
            float sin = walls[o + 3];
            float relX = originX - walls[o];
            float relZ = originZ - walls[o + 1];
            float localOriginX = relX * cos + relZ * sin;
            float localOriginZ = -relX * sin + relZ * cos;
            float localDirX = dirX * cos + dirZ * sin;
            float localDirZ = -dirX * sin + dirZ * cos;

            float enterX;
            float exitX;
            if (localDirX > -EPSILON && localDirX < EPSILON) {
                if (localOriginX < -walls[o + 4] || localOriginX > walls[o + 4]) {
                    continue;
                }
                enterX = Float.NEGATIVE_INFINITY;
                exitX = Float.POSITIVE_INFINITY;
            } else {
                float inverse = 1f / localDirX;
                float t1 = (-walls[o + 4] - localOriginX) * inverse;
                float t2 = (walls[o + 4] - localOriginX) * inverse;
                enterX = Math.min(t1, t2);
                exitX = Math.max(t1, t2);
            }
            float enterZ;
            float exitZ;
            if (localDirZ > -EPSILON && localDirZ < EPSILON) {
                if (localOriginZ < -walls[o + 5] || localOriginZ > walls[o + 5]) {
                    continue;
                }
                enterZ = Float.NEGATIVE_INFINITY;
                exitZ = Float.POSITIVE_INFINITY;
            } else {
                float inverse = 1f / localDirZ;
                float t1 = (-walls[o + 5] - localOriginZ) * inverse;
                float t2 = (walls[o + 5] - localOriginZ) * inverse;
                enterZ = Math.min(t1, t2);
                exitZ = Math.max(t1, t2);
            }

            float enter = Math.max(enterX, enterZ);
            if (enter > Math.min(exitX, exitZ) || enter > maxDistance) {
                continue;
            }
            float distance = enter < 0f ? 0f : enter;
            float hitY = originY + dirY * distance;
            if (hitY < walls[o + 6] || hitY > walls[o + 7]) {
                continue; // over the wall, or below its base
            }
            float localNormalX;
            float localNormalZ;
            if (enter == enterX) {
                localNormalX = localDirX > 0f ? -1f : 1f;
                localNormalZ = 0f;
            } else {
                localNormalX = 0f;
                localNormalZ = localDirZ > 0f ? -1f : 1f;
            }
            out.accept(distance, originX + dirX * distance, hitY, originZ + dirZ * distance,
                    localNormalX * cos - localNormalZ * sin, 0f,
                    localNormalX * sin + localNormalZ * cos, RayHit.Surface.WALL);
        }
    }

    /** Flat island ground at y = 0 plus the tops of plinths and steps. */
    private void castGround(float originX, float originY, float originZ,
                            float dirX, float dirY, float dirZ,
                            float maxDistance, RayHit out) {
        if (dirY >= -EPSILON || originY <= 0f) {
            return; // level or upward shots never reach the ground
        }
        float[] plinths = boxes.getPlinths();
        int count = boxes.getPlinthCount();
        for (int i = 0; i < count; i++) {
            int o = i * ColliderBoxes.FIELDS;
            float height = plinths[o + 7];
            if (originY <= height) {
                continue;
            }
            float distance = (height - originY) / dirY;
            if (distance <= 0f || distance > maxDistance) {
                continue;
            }
            float hitX = originX + dirX * distance;
            float hitZ = originZ + dirZ * distance;
            if (insideBox(plinths, o, hitX - plinths[o], hitZ - plinths[o + 1])) {
                out.accept(distance, hitX, height, hitZ, 0f, 1f, 0f, RayHit.Surface.PLINTH);
            }
        }
        float distance = -originY / dirY;
        if (distance <= 0f || distance > maxDistance) {
            return;
        }
        float groundX = originX + dirX * distance;
        float groundZ = originZ + dirZ * distance;
        if (Math.abs(groundX) <= halfSize && Math.abs(groundZ) <= halfSize) {
            out.accept(distance, groundX, 0f, groundZ, 0f, 1f, 0f, RayHit.Surface.GROUND);
        }
    }

    /** Wall boxes in this map (debug/HUD). */
    public int getWallCount() {
        return boxes.getWallCount();
    }

    /** Walkable plinth boxes in this map (debug/HUD). */
    public int getPlinthCount() {
        return boxes.getPlinthCount();
    }
}

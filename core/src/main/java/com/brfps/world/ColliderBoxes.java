package com.brfps.world;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.brfps.util.Constants;
import com.brfps.world.MapLayout.BuildingDef;

/**
 * Bakes a map layout's collision boxes into flat float arrays: {@link #FIELDS} floats
 * per oriented box (centre, rotation, half extents, base and top height). Every wall
 * segment of every building becomes a box — the front wall is split into two door
 * jambs so doorways stay walkable, interiors add their partition, and plinths land in
 * a separate array because they are walkable ground rather than obstacles.
 *
 * <p>Boxes mirror {@link BuildingFactory}'s geometry exactly and live in the same
 * local space (local +Z is the front, rotated by the layout's rotation). Build-time
 * only: nothing here runs per frame (R6, R30).
 */
public class ColliderBoxes {

    /** cx, cz, cos, sin, halfWidth, halfDepth, baseY, topY (plinths: 0 and height). */
    public static final int FIELDS = 8;

    private final float[] walls;
    private final int wallCount;
    private final float[] plinths;
    private final int plinthCount;

    public ColliderBoxes(Array<BuildingDef> defs) {
        FloatArray wallData = new FloatArray(1400);
        FloatArray plinthData = new FloatArray(64);
        for (int i = 0; i < defs.size; i++) {
            BuildingDef def = defs.get(i);
            BuildingType type = BuildingType.fromId(def.type);
            if (type == null) {
                continue; // same skip rule as the batcher: unknown ids are ignored
            }
            float cos = MathUtils.cosDeg(def.rotation);
            float sin = MathUtils.sinDeg(def.rotation);
            float halfWidth = type.width * 0.5f;
            float halfDepth = type.depth * 0.5f;
            float thickness = Constants.WALL_THICKNESS;
            float base = type.plinthHeight;
            float top = base + type.wallHeight;

            addBox(wallData, def, cos, sin, 0f, -halfDepth + thickness * 0.5f,
                    halfWidth, thickness * 0.5f, base, top);
            addBox(wallData, def, cos, sin, halfWidth - thickness * 0.5f, 0f,
                    thickness * 0.5f, halfDepth, base, top);
            addBox(wallData, def, cos, sin, -halfWidth + thickness * 0.5f, 0f,
                    thickness * 0.5f, halfDepth, base, top);
            addDoorJambs(wallData, def, cos, sin, type, halfWidth, halfDepth, thickness,
                    base, top);
            if (type.hasInterior) {
                addPartition(wallData, def, cos, sin, halfWidth, halfDepth, thickness,
                        base, top - 0.1f);
            }
            if (type.plinthHeight > 0f) {
                addPlinths(plinthData, def, cos, sin, type, halfWidth, halfDepth);
            }
        }
        walls = wallData.toArray();
        wallCount = wallData.size / FIELDS;
        plinths = plinthData.toArray();
        plinthCount = plinthData.size / FIELDS;
    }

    /** One box at a local offset, rotated into world space by the building's yaw. */
    private static void addBox(FloatArray out, BuildingDef def, float cos, float sin,
                               float localX, float localZ, float halfWidth, float halfDepth,
                               float baseY, float topY) {
        out.add(def.x + localX * cos - localZ * sin);
        out.add(def.z + localX * sin + localZ * cos);
        out.add(cos);
        out.add(sin);
        out.add(halfWidth);
        out.add(halfDepth);
        out.add(baseY);
        out.add(topY);
    }

    /** Front wall (+Z) as two jambs either side of the doorway; the lintel is head-high. */
    private static void addDoorJambs(FloatArray out, BuildingDef def, float cos, float sin,
                                     BuildingType type, float halfWidth, float halfDepth,
                                     float thickness, float base, float top) {
        float doorHalf = type.doorWidth() * 0.5f;
        float jambHalf = (halfWidth - doorHalf) * 0.5f;
        if (jambHalf <= 0.01f) {
            return; // wall entirely door
        }
        float jambCenter = (halfWidth + doorHalf) * 0.5f;
        addBox(out, def, cos, sin, -jambCenter, halfDepth - thickness * 0.5f,
                jambHalf, thickness * 0.5f, base, top);
        addBox(out, def, cos, sin, jambCenter, halfDepth - thickness * 0.5f,
                jambHalf, thickness * 0.5f, base, top);
    }

    /** Interior partition wall, matching BuildingFactory's gap on the +Z side. */
    private static void addPartition(FloatArray out, BuildingDef def, float cos, float sin,
                                     float halfWidth, float halfDepth, float thickness,
                                     float baseY, float topY) {
        float partX = -halfWidth * 0.3f;
        float zStart = -halfDepth + thickness;
        float zEnd = halfDepth * 0.25f;
        addBox(out, def, cos, sin, partX + thickness * 0.5f, (zStart + zEnd) * 0.5f,
                thickness * 0.5f, (zEnd - zStart) * 0.5f, baseY, topY);
    }

    /** Plinth slab plus its front steps, both walkable ground for the player. */
    private static void addPlinths(FloatArray out, BuildingDef def, float cos, float sin,
                                   BuildingType type, float halfWidth, float halfDepth) {
        addBox(out, def, cos, sin, 0f, 0f, halfWidth + 0.8f, halfDepth + 0.8f,
                0f, type.plinthHeight);
        float stepHalf = type.doorWidth() * 0.5f + 0.6f;
        addBox(out, def, cos, sin, 0f, halfDepth + 1.3f, stepHalf, 0.5f,
                0f, type.plinthHeight * 0.5f);
    }

    /** Wall boxes: obstacles the player cannot walk through. */
    public float[] getWalls() {
        return walls;
    }

    public int getWallCount() {
        return wallCount;
    }

    /** Plinth boxes: walkable ground with a height. */
    public float[] getPlinths() {
        return plinths;
    }

    public int getPlinthCount() {
        return plinthCount;
    }
}

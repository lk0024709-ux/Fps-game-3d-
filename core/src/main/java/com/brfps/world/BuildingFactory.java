package com.brfps.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.brfps.util.Constants;

/**
 * Emits one building's vertex-colored geometry (plinth, walls, doorway, windows,
 * roof, interior and per-type extras) into a MeshKit; everything is authored in
 * local space and rotated on the way out, so placement stays pure map data (R23).
 */
public final class BuildingFactory {

    private static final Color WINDOW_COLOR = new Color(0.22f, 0.34f, 0.44f, 1f);
    private static final Color DOOR_COLOR = new Color(0.34f, 0.23f, 0.15f, 1f);
    private static final Color AWNING_COLOR = new Color(0.80f, 0.26f, 0.24f, 1f);
    private static final Color PARTITION_COLOR = new Color(0.74f, 0.70f, 0.62f, 1f);
    private static final Color CHIMNEY_COLOR = new Color(0.42f, 0.40f, 0.40f, 1f);

    /** Reused for shading so no Color is allocated while baking (R6). */
    private static final Color SCRATCH = new Color();
    private static final Xform XF = new Xform();

    /** Reusable local-to-world yaw transform (one instance, no allocation). */
    private static final class Xform {
        private float cx;
        private float cz;
        private float cos = 1f;
        private float sin;

        void set(float centerX, float centerZ, float degrees) {
            cx = centerX;
            cz = centerZ;
            cos = MathUtils.cosDeg(degrees);
            sin = MathUtils.sinDeg(degrees);
        }

        float x(float lx, float lz) {
            return cx + lx * cos - lz * sin;
        }

        float z(float lx, float lz) {
            return cz + lx * sin + lz * cos;
        }
    }

    private BuildingFactory() {
    }

    /** Appends one building to the kit; local +Z is the front, yawed by rotationDeg. */
    public static void emit(MeshKit kit, BuildingType type, float centerX, float centerZ,
                            float rotationDeg) {
        XF.set(centerX, centerZ, rotationDeg);
        float hw = type.width * 0.5f;
        float hd = type.depth * 0.5f;
        float t = Constants.WALL_THICKNESS;
        float base = type.plinthHeight;
        float top = base + type.wallHeight;

        if (base > 0f) {
            box(kit, -hw - 0.8f, hw + 0.8f, -hd - 0.8f, hd + 0.8f, 0f, base,
                    type.floor, type.floor, type.floor);
            float dhx = type.doorWidth() * 0.5f;
            box(kit, -dhx - 0.6f, dhx + 0.6f, hd + 0.8f, hd + 1.8f, 0f, base * 0.5f,
                    type.floor, type.floor, type.floor);
        }

        box(kit, -hw, hw, -hd, -hd + t, base, top, type.wall, type.wall, type.wall);
        box(kit, hw - t, hw, -hd, hd, base, top, type.wall, type.wall, type.wall);
        box(kit, -hw, -hw + t, -hd, hd, base, top, type.wall, type.wall, type.wall);
        frontWall(kit, type, hw, hd, t, base, top);
        windows(kit, type, hw, hd, base);

        if (type.hasInterior) {
            interior(kit, type, hw, hd, t, base, top);
        }
        if (type.roof == BuildingType.Roof.PITCHED) {
            pitchedRoof(kit, type, hw, hd, top);
        } else {
            box(kit, -hw - 0.3f, hw + 0.3f, -hd - 0.3f, hd + 0.3f, top, top + Constants.ROOF_SLAB,
                    type.roofColor, type.roofColor, type.roofColor);
        }
        extras(kit, type, hw, hd, top);
    }

    /** Front wall (+Z) split around the doorway, with a lintel above the opening. */
    private static void frontWall(MeshKit kit, BuildingType type, float hw, float hd, float t,
                                  float base, float top) {
        float dhx = type.doorWidth() * 0.5f;
        float doorH = Math.min(Constants.DOOR_HEIGHT, type.wallHeight * 0.8f);
        float z0 = hd - t;
        box(kit, -hw, -dhx, z0, hd, base, top, type.wall, type.wall, type.wall);
        box(kit, dhx, hw, z0, hd, base, top, type.wall, type.wall, type.wall);
        box(kit, -dhx, dhx, z0, hd, base + doorH, top, type.wall, type.wall, type.wall);
        if (!type.hasInterior) {
            // Shell-only buildings get a closed door panel; interiors stay walkable.
            float bits = DOOR_COLOR.toFloatBits();
            float z = hd - t * 0.5f;
            quad(kit, bits,
                    -dhx + 0.05f, base, z, dhx - 0.05f, base, z,
                    dhx - 0.05f, base + doorH - 0.05f, z, -dhx + 0.05f, base + doorH - 0.05f, z);
        }
    }

    /** Window bands: one row on short walls, two rows on walls 5 m or taller. */
    private static void windows(MeshKit kit, BuildingType type, float hw, float hd, float base) {
        int rows = type.wallHeight >= 5f ? 2 : 1;
        float rowHeight = type.wallHeight / rows;
        for (int row = 0; row < rows; row++) {
            windowRow(kit, type, hw, hd, base + row * rowHeight, rowHeight);
        }
    }

    /** One horizontal band of window quads, proud of each wall face by 2 cm. */
    private static void windowRow(MeshKit kit, BuildingType type, float hw, float hd,
                                  float floorY, float roomHeight) {
        if (roomHeight < Constants.WINDOW_SILL + Constants.WINDOW_HEIGHT + 0.3f) {
            return;
        }
        float bits = WINDOW_COLOR.toFloatBits();
        float sill = floorY + Constants.WINDOW_SILL;
        float head = sill + Constants.WINDOW_HEIGHT;
        float halfW = Constants.WINDOW_WIDTH * 0.5f;
        float o = 0.02f;
        int perSide = type.depth >= 6f ? 2 : 1;
        for (int i = 0; i < perSide; i++) {
            float along = perSide == 1 ? 0f : (i == 0 ? -hd * 0.45f : hd * 0.45f);
            quad(kit, bits,
                    hw + o, sill, along - halfW, hw + o, sill, along + halfW,
                    hw + o, head, along + halfW, hw + o, head, along - halfW);
            quad(kit, bits,
                    -hw - o, sill, along + halfW, -hw - o, sill, along - halfW,
                    -hw - o, head, along - halfW, -hw - o, head, along + halfW);
        }
        quad(kit, bits,
                -halfW, sill, -hd - o, halfW, sill, -hd - o,
                halfW, head, -hd - o, -halfW, head, -hd - o);
        float dhx = type.doorWidth() * 0.5f;
        if (hw - dhx > Constants.WINDOW_WIDTH + 0.5f) {
            float fx = dhx + (hw - dhx) * 0.5f;
            quad(kit, bits,
                    fx - halfW, sill, hd + o, fx + halfW, sill, hd + o,
                    fx + halfW, head, hd + o, fx - halfW, head, hd + o);
            quad(kit, bits,
                    -fx - halfW, sill, hd + o, -fx + halfW, sill, hd + o,
                    -fx + halfW, head, hd + o, -fx - halfW, head, hd + o);
        }
    }

    /** Floor slab, a partition with a gap, and a mid floor in tall buildings. */
    private static void interior(MeshKit kit, BuildingType type, float hw, float hd, float t,
                                 float base, float top) {
        float y = base + Constants.BUILDING_FLOOR_OFFSET;
        quad(kit, type.floor.toFloatBits(),
                -hw + t, y, -hd + t, -hw + t, y, hd - t,
                hw - t, y, hd - t, hw - t, y, -hd + t);
        float partX = -hw * 0.3f;
        box(kit, partX, partX + t, -hd + t, hd * 0.25f, base, top - 0.1f,
                PARTITION_COLOR, PARTITION_COLOR, PARTITION_COLOR);
        if (type.wallHeight >= 5f) {
            float mid = base + type.wallHeight * 0.5f;
            box(kit, -hw + t, hw - t, -hd + t, hd - t, mid - 0.15f, mid,
                    type.floor, type.floor, type.floor);
        }
    }

    /** Pitched roof: two slopes plus two gable triangles, ridge along local X. */
    private static void pitchedRoof(MeshKit kit, BuildingType type, float hw, float hd, float top) {
        float roofBits = type.roofColor.toFloatBits();
        float wallBits = type.wall.toFloatBits();
        float ex = hw + Constants.ROOF_OVERHANG;
        float ez = hd + Constants.ROOF_OVERHANG;
        float ridge = top + Constants.ROOF_PITCH * hd;
        quad(kit, roofBits,
                -ex, top, ez, ex, top, ez, ex, ridge, 0f, -ex, ridge, 0f);
        quad(kit, shade(type.roofColor, 0.82f),
                ex, top, -ez, -ex, top, -ez, -ex, ridge, 0f, ex, ridge, 0f);
        quad(kit, wallBits,
                hw, top, -hd, hw, top, hd, hw, ridge, 0f, hw, ridge, 0f);
        quad(kit, shade(type.wall, 0.9f),
                -hw, top, hd, -hw, top, -hd, -hw, ridge, 0f, -hw, ridge, 0f);
    }

    /** Per-type extras: shop awning, temple tower, factory chimney. */
    private static void extras(MeshKit kit, BuildingType type, float hw, float hd, float top) {
        float roofTop = type.roof == BuildingType.Roof.PITCHED ? top : top + Constants.ROOF_SLAB;
        switch (type) {
            case SHOP:
                awning(kit, hw, hd);
                break;
            case TEMPLE:
                tower(kit, type, hw, hd, roofTop);
                break;
            case FACTORY:
                chimney(kit, type, hw, hd, roofTop);
                break;
            default:
                break;
        }
    }

    /** Slanted awning over the shop front, held up by two thin posts. */
    private static void awning(MeshKit kit, float hw, float hd) {
        float out = hd + 1.5f;
        quad(kit, AWNING_COLOR.toFloatBits(),
                -hw + 0.4f, 2.5f, hd, hw - 0.4f, 2.5f, hd,
                hw - 0.4f, 2.1f, out, -hw + 0.4f, 2.1f, out);
        box(kit, -hw + 0.6f, -hw + 0.75f, out - 0.15f, out, 0f, 2.1f,
                AWNING_COLOR, AWNING_COLOR, AWNING_COLOR);
        box(kit, hw - 0.75f, hw - 0.6f, out - 0.15f, out, 0f, 2.1f,
                AWNING_COLOR, AWNING_COLOR, AWNING_COLOR);
    }

    /** Three shrinking steps ending in a colored cap: the temple landmark. */
    private static void tower(MeshKit kit, BuildingType type, float hw, float hd, float roofTop) {
        float y = roofTop;
        float w = hw * 0.5f;
        float d = hd * 0.5f;
        float cz = -hd * 0.2f;
        for (int step = 0; step < 3; step++) {
            float h = 1.1f - step * 0.25f;
            Color cap = step == 2 ? type.roofColor : type.wall;
            box(kit, -w, w, cz - d, cz + d, y, y + h, cap, type.wall, type.wall);
            y += h;
            w *= 0.72f;
            d *= 0.72f;
        }
    }

    /** Chimney stack on the factory roof corner. */
    private static void chimney(MeshKit kit, BuildingType type, float hw, float hd, float roofTop) {
        box(kit, hw * 0.45f, hw * 0.75f, -hd * 0.75f, -hd * 0.45f,
                roofTop, roofTop + type.wallHeight * 0.5f,
                CHIMNEY_COLOR, CHIMNEY_COLOR, CHIMNEY_COLOR);
    }

    /** Local-space box: four side faces plus a top, and a bottom when it floats. */
    private static void box(MeshKit kit, float x0, float x1, float z0, float z1,
                            float y0, float y1, Color top, Color side, Color bottom) {
        float sideBits = side.toFloatBits();
        quad(kit, shade(side, 0.76f),
                x1, y0, z1, x1, y0, z0, x1, y1, z0, x1, y1, z1);
        quad(kit, shade(side, 0.88f),
                x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0);
        quad(kit, sideBits,
                x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1);
        quad(kit, shade(side, 0.94f),
                x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0);
        quad(kit, top.toFloatBits(),
                x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        if (y0 > Constants.FLOATING_FACE_MIN_Y) {
            quad(kit, shade(bottom, 0.72f),
                    x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        }
    }

    /** Emits one quad from four local-space corners (x, y, z each). */
    private static void quad(MeshKit kit, float bits,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz) {
        vertex(kit, ax, ay, az, bits);
        vertex(kit, bx, by, bz, bits);
        vertex(kit, cx, cy, cz, bits);
        vertex(kit, dx, dy, dz, bits);
        kit.closeQuad();
    }

    /** Transforms one local-space vertex into the world and adds it to the kit. */
    private static void vertex(MeshKit kit, float lx, float ly, float lz, float bits) {
        kit.vertex(XF.x(lx, lz), ly, XF.z(lx, lz), bits);
    }

    /** Packed color with rgb multiplied by factor; alpha untouched, nothing allocated. */
    private static float shade(Color src, float factor) {
        SCRATCH.set(src);
        SCRATCH.r *= factor;
        SCRATCH.g *= factor;
        SCRATCH.b *= factor;
        return SCRATCH.toFloatBits();
    }
}

package com.brfps.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.brfps.util.Constants;
import com.brfps.world.MapLayout.ZoneDef;

/**
 * Builds and renders the static world geometry (terrain, water, rivers, roads,
 * zone markers) described by a MapLayout. All meshes are baked once at
 * construction; the render path allocates nothing.
 */
public class Arena {

    // Map-specific palette lives here, NOT in Constants (colors are per-map data).
    private static final ObjectMap<String, Color> GROUND_COLORS = new ObjectMap<>();

    static {
        GROUND_COLORS.put("grass", new Color(0.32f, 0.55f, 0.28f, 1f));
        GROUND_COLORS.put("dry_grass", new Color(0.62f, 0.58f, 0.30f, 1f));
        GROUND_COLORS.put("dirt", new Color(0.52f, 0.38f, 0.24f, 1f));
        GROUND_COLORS.put("concrete", new Color(0.58f, 0.58f, 0.57f, 1f));
        GROUND_COLORS.put("asphalt", new Color(0.35f, 0.35f, 0.37f, 1f));
    }

    private static final Color WATER_COLOR = new Color(0.16f, 0.42f, 0.75f, Constants.WATER_ALPHA);
    private static final Color RIVER_COLOR = new Color(0.13f, 0.36f, 0.68f, 0.85f);
    private static final Color ROAD_COLOR = new Color(0.46f, 0.46f, 0.48f, 1f);
    private static final Color BEACH_COLOR = new Color(0.91f, 0.83f, 0.60f, 1f);
    private static final Color SPAWN_COLOR = new Color(0.20f, 0.90f, 0.95f, 1f);
    private static final Color GRID_COLOR = new Color(0.10f, 0.16f, 0.10f, 1f);
    private static final Color BASE_GRASS = GROUND_COLORS.get("grass");

    private final MapLayout layout;
    private final Array<SpawnPoint> spawnPoints;
    private final Color buildColor = new Color();
    private final FlatShader flatShader = new FlatShader();
    private final Mesh terrainMesh;
    private final Mesh roadMesh;
    private final Mesh waterMesh;
    private final Mesh riverMesh;
    private final Mesh markerMesh;
    private final Mesh spawnMesh;
    private final Mesh gridMesh;
    private final BuildingBatcher buildings;
    private int drawCalls;
    private int trianglesRendered;

    public Arena(MapLayout layout) {
        this.layout = layout;
        this.spawnPoints = layout.createSpawnPoints();
        for (ZoneDef zone : layout.zones) {
            if (!GROUND_COLORS.containsKey(zone.ground)) {
                Gdx.app.error("Arena", "unknown ground '" + zone.ground + "' in zone "
                        + zone.id + ", falling back to grass");
            }
        }
        terrainMesh = buildTerrain();
        roadMesh = buildRoads();
        waterMesh = layout.water ? buildWaterPlane() : null;
        riverMesh = layout.rivers.size > 0 ? buildRivers() : null;
        markerMesh = Constants.DEBUG_SHOW_ZONE_MARKERS ? buildZoneMarkers() : null;
        spawnMesh = Constants.DEBUG_SHOW_SPAWN_MARKERS ? buildSpawnMarkers() : null;
        gridMesh = Constants.DEBUG_SHOW_CHUNK_GRID ? buildChunkGrid() : null;
        buildings = new BuildingBatcher(layout.buildings);
    }

    /** Renders the whole static world; call once per frame after clearing. */
    public void render(PerspectiveCamera camera) {
        drawCalls = 0;
        trianglesRendered = 0;
        flatShader.begin(camera);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        // Opaque, painter order (terrain first).
        draw(terrainMesh, GL20.GL_TRIANGLES);
        draw(roadMesh, GL20.GL_TRIANGLES);
        draw(markerMesh, GL20.GL_TRIANGLES);
        draw(spawnMesh, GL20.GL_TRIANGLES);
        draw(gridMesh, GL20.GL_LINES);
        buildings.render(flatShader.program());
        drawCalls += buildings.getDrawCalls();
        trianglesRendered += buildings.getTrianglesRendered();
        // Translucent on top, without writing depth.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(false);
        draw(riverMesh, GL20.GL_TRIANGLES);
        draw(waterMesh, GL20.GL_TRIANGLES);
        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /** Draw calls issued during the last render() (for the debug HUD). */
    public int getDrawCalls() {
        return drawCalls;
    }

    /** Triangles drawn during the last render() (lines excluded). */
    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    /** Spawn points read from the layout (used by landing M4 and bots M7). */
    public Array<SpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    public MapLayout getLayout() {
        return layout;
    }

    public void dispose() {
        for (Mesh mesh : new Mesh[] {terrainMesh, roadMesh, waterMesh, riverMesh,
                markerMesh, spawnMesh, gridMesh}) {
            if (mesh != null) {
                mesh.dispose();
            }
        }
        flatShader.dispose();
        buildings.dispose();
    }

    private void draw(Mesh mesh, int primitiveType) {
        if (mesh == null) {
            return;
        }
        mesh.render(flatShader.program(), primitiveType);
        drawCalls++;
        if (primitiveType == GL20.GL_TRIANGLES) {
            trianglesRendered += mesh.getNumIndices() / 3;
        }
    }

    private Mesh buildTerrain() {
        int segments = Constants.TERRAIN_SEGMENTS;
        float half = layout.size * 0.5f;
        float step = layout.size / segments;
        MeshKit kit = new MeshKit();
        for (int iz = 0; iz < segments; iz++) {
            for (int ix = 0; ix < segments; ix++) {
                float x0 = -half + ix * step;
                float z0 = -half + iz * step;
                float x1 = x0 + step;
                float z1 = z0 + step;
                kit.topQuad(x0, z0, x1, z1, 0f,
                        sampleGround(x0, z0).toFloatBits(),
                        sampleGround(x0, z1).toFloatBits(),
                        sampleGround(x1, z1).toFloatBits(),
                        sampleGround(x1, z0).toFloatBits());
            }
        }
        return kit.bake();
    }

    /** Ground color at (x, z): beach ring, then zone fills, then base grass. */
    private Color sampleGround(float x, float z) {
        float half = layout.size * 0.5f;
        float beach = layout.beachWidth;
        if (x > half - beach || x < -half + beach || z > half - beach || z < -half + beach) {
            return buildColor.set(BEACH_COLOR);
        }
        for (ZoneDef zone : layout.zones) {
            if (Math.abs(x - zone.x) <= zone.width * 0.5f
                    && Math.abs(z - zone.z) <= zone.depth * 0.5f) {
                buildColor.set(GROUND_COLORS.get(zone.ground, BASE_GRASS));
                return applyChecker(x, z);
            }
        }
        buildColor.set(BASE_GRASS);
        return applyChecker(x, z);
    }

    /** Subtle 10 m two-tone so large zones do not read as perfectly flat color. */
    private Color applyChecker(float x, float z) {
        if ((((int) (x / 10f)) + ((int) (z / 10f))) % 2 == 0) {
            buildColor.mul(0.96f);
        }
        return buildColor;
    }

    private Mesh buildRoads() {
        MeshKit kit = new MeshKit();
        float y = Constants.ROAD_Y_OFFSET;
        for (MapLayout.RoadDef road : layout.roads) {
            float dx = road.toX - road.fromX;
            float dz = road.toZ - road.fromZ;
            float length = (float) Math.sqrt(dx * dx + dz * dz);
            if (length < 0.01f) {
                continue;
            }
            float px = -dz / length * road.width * 0.5f;
            float pz = dx / length * road.width * 0.5f;
            // Corners CCW seen from above: A+perp, B+perp, B-perp, A-perp.
            kit.quad(road.fromX + px, road.fromZ + pz,
                     road.toX + px, road.toZ + pz,
                     road.toX - px, road.toZ - pz,
                     road.fromX - px, road.fromZ - pz, y, ROAD_COLOR.toFloatBits());
        }
        return kit.bake();
    }

    private Mesh buildWaterPlane() {
        MeshKit kit = new MeshKit();
        float half = layout.size * 0.5f + layout.waterMargin;
        kit.topQuad(-half, -half, half, half, layout.waterLevel, WATER_COLOR.toFloatBits());
        return kit.bake();
    }

    private Mesh buildRivers() {
        MeshKit kit = new MeshKit();
        float y = Constants.RIVER_Y_OFFSET;
        for (MapLayout.RiverDef river : layout.rivers) {
            FloatArray points = river.points;
            for (int i = 0; i + 3 < points.size; i += 2) {
                addStripSegment(kit, points.items[i], points.items[i + 1],
                        points.items[i + 2], points.items[i + 3],
                        river.width, y, RIVER_COLOR.toFloatBits());
            }
        }
        return kit.bake();
    }

    /** One quad of a polyline strip between two points, width w around the segment. */
    private void addStripSegment(MeshKit kit, float ax, float az, float bx, float bz,
                                 float width, float y, float colorBits) {
        float dx = bx - ax;
        float dz = bz - az;
        float length = (float) Math.sqrt(dx * dx + dz * dz);
        if (length < 0.01f) {
            return;
        }
        float px = -dz / length * width * 0.5f;
        float pz = dx / length * width * 0.5f;
        kit.quad(ax + px, az + pz, bx + px, bz + pz,
                 bx - px, bz - pz, ax - px, az - pz, y, colorBits);
    }

    private Mesh buildZoneMarkers() {
        MeshKit kit = new MeshKit();
        float bitsTop;
        float bitsSide;
        for (ZoneDef zone : layout.zones) {
            buildColor.set(GROUND_COLORS.get(zone.ground, BASE_GRASS)).lerp(Color.WHITE, 0.35f);
            bitsTop = buildColor.toFloatBits();
            bitsSide = buildColor.cpy().mul(0.75f).toFloatBits();
            float hw = zone.width * 0.5f;
            float hd = zone.depth * 0.5f;
            kit.box(zone.x - hw, zone.z - hd, Constants.MARKER_POST_SIDE * 0.5f,
                    Constants.MARKER_POST_HEIGHT, bitsTop, bitsSide);
            kit.box(zone.x + hw, zone.z - hd, Constants.MARKER_POST_SIDE * 0.5f,
                    Constants.MARKER_POST_HEIGHT, bitsTop, bitsSide);
            kit.box(zone.x - hw, zone.z + hd, Constants.MARKER_POST_SIDE * 0.5f,
                    Constants.MARKER_POST_HEIGHT, bitsTop, bitsSide);
            kit.box(zone.x + hw, zone.z + hd, Constants.MARKER_POST_SIDE * 0.5f,
                    Constants.MARKER_POST_HEIGHT, bitsTop, bitsSide);
        }
        return kit.bake();
    }

    private Mesh buildSpawnMarkers() {
        MeshKit kit = new MeshKit();
        for (SpawnPoint spawn : spawnPoints) {
            kit.topQuad(spawn.x - 0.4f, spawn.z - 0.4f, spawn.x + 0.4f, spawn.z + 0.4f,
                    Constants.SPAWN_MARK_Y_OFFSET, SPAWN_COLOR.toFloatBits());
        }
        return kit.bake();
    }

    private Mesh buildChunkGrid() {
        MeshKit kit = new MeshKit();
        float half = layout.size * 0.5f;
        float step = layout.size / 10f; // preview of the M2e 10x10 chunk grid
        float bits = GRID_COLOR.toFloatBits();
        for (int i = 0; i <= 10; i++) {
            float pos = -half + i * step;
            kit.line(pos, Constants.GRID_Y_OFFSET, -half,
                     pos, Constants.GRID_Y_OFFSET, half, bits);
            kit.line(-half, Constants.GRID_Y_OFFSET, pos,
                     half, Constants.GRID_Y_OFFSET, pos, bits);
        }
        return kit.bake();
    }
}

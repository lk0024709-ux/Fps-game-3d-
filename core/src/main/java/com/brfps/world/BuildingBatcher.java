package com.brfps.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.brfps.util.Constants;
import com.brfps.world.MapLayout.BuildingDef;

/**
 * Bakes every building of a layout into the fewest possible static meshes and
 * renders them with an already-bound program, so a whole town costs one draw
 * call (R28) and nothing is allocated after construction (R6, R30).
 */
public class BuildingBatcher {

    private final Array<Mesh> meshes = new Array<>();
    private int placed;
    private int skipped;
    private int drawCalls;
    private int triangles;

    /** Bakes all meshes now; unknown building types are logged and skipped (R25). */
    public BuildingBatcher(Array<BuildingDef> defs) {
        MeshKit kit = new MeshKit();
        for (int i = 0; i < defs.size; i++) {
            BuildingDef def = defs.get(i);
            BuildingType type = BuildingType.fromId(def.type);
            if (type == null) {
                Gdx.app.error("BuildingBatcher", "unknown building type '" + def.type
                        + "' at (" + def.x + ", " + def.z + ") — skipped");
                skipped++;
                continue;
            }
            if (kit.vertices.size / 4 > Constants.BUILDING_VERTEX_LIMIT) {
                Mesh full = kit.bake();
                if (full != null) {
                    meshes.add(full);
                }
                kit = new MeshKit();
            }
            BuildingFactory.emit(kit, type, def.x, def.z, def.rotation);
            placed++;
        }
        Mesh tail = kit.bake();
        if (tail != null) {
            meshes.add(tail);
        }
        Gdx.app.log("BuildingBatcher", placed + " buildings baked into " + meshes.size
                + " mesh(es), " + skipped + " skipped");
    }

    /** Draws every building mesh with the caller's bound program (opaque pass). */
    public void render(ShaderProgram program) {
        drawCalls = 0;
        triangles = 0;
        for (int i = 0; i < meshes.size; i++) {
            Mesh mesh = meshes.get(i);
            mesh.render(program, GL20.GL_TRIANGLES);
            drawCalls++;
            triangles += mesh.getNumIndices() / 3;
        }
    }

    /** Draw calls issued by the last render(). */
    public int getDrawCalls() {
        return drawCalls;
    }

    /** Triangles drawn by the last render(). */
    public int getTrianglesRendered() {
        return triangles;
    }

    /** Buildings successfully baked (debug HUD, verification). */
    public int getPlacedCount() {
        return placed;
    }

    /** Layout entries whose type was not in the building kit. */
    public int getSkippedCount() {
        return skipped;
    }

    public void dispose() {
        for (int i = 0; i < meshes.size; i++) {
            meshes.get(i).dispose();
        }
        meshes.clear();
    }
}

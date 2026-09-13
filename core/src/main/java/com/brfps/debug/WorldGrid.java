package com.brfps.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.brfps.util.Constants;
import com.brfps.world.MeshKit;

/**
 * A flat editor grid baked once as GL_LINES: minor lines every 10 m, major lines
 * every 50 m and a bright center cross, covering the whole map (R6, R30).
 */
public class WorldGrid {

    private static final Color MINOR = new Color(1f, 1f, 1f, 0.16f);
    private static final Color MAJOR = new Color(1f, 1f, 1f, 0.40f);
    private static final Color CENTER = new Color(0.95f, 0.85f, 0.30f, 0.75f);

    private final Mesh mesh;
    private final int lines;

    /** Bakes the grid for a square map of the given side length. */
    public WorldGrid(float mapSize) {
        MeshKit kit = new MeshKit();
        float half = mapSize * 0.5f;
        float y = Constants.EDITOR_GRID_Y;
        int count = 0;
        for (float v = -half; v <= half + 0.001f; v += Constants.GRID_MINOR_STEP) {
            float bits = colorFor(v).toFloatBits();
            kit.line(v, y, -half, v, y, half, bits);
            kit.line(-half, y, v, half, y, v, bits);
            count += 2;
        }
        mesh = kit.bake();
        lines = count;
    }

    private static Color colorFor(float v) {
        if (Math.abs(v) < 0.001f) {
            return CENTER;
        }
        return Math.abs(v % Constants.GRID_MAJOR_STEP) < 0.001f ? MAJOR : MINOR;
    }

    /** Draws the grid with an already-bound program. */
    public void render(ShaderProgram program) {
        if (mesh != null) {
            mesh.render(program, GL20.GL_LINES);
        }
    }

    /** Line segments in the grid (debug HUD). */
    public int getLines() {
        return lines;
    }

    public void dispose() {
        if (mesh != null) {
            mesh.dispose();
        }
    }
}

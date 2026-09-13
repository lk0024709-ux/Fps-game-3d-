package com.brfps.debug;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.brfps.util.Constants;
import com.brfps.world.MeshKit;

/**
 * World-origin axis gizmo baked once as GL_LINES: X red, Y green, Z blue, so map
 * coordinates can be read off the scene at a glance.
 */
public class AxisGizmo {

    private static final Color X_COLOR = new Color(0.95f, 0.25f, 0.25f, 1f);
    private static final Color Y_COLOR = new Color(0.35f, 0.90f, 0.35f, 1f);
    private static final Color Z_COLOR = new Color(0.30f, 0.55f, 0.98f, 1f);

    private final Mesh mesh;

    /** Bakes the three axis lines. */
    public AxisGizmo() {
        MeshKit kit = new MeshKit();
        float y = Constants.AXIS_GIZMO_Y;
        float len = Constants.AXIS_LENGTH;
        kit.line(0f, y, 0f, len, y, 0f, X_COLOR.toFloatBits());
        kit.line(0f, y, 0f, 0f, y + len, 0f, Y_COLOR.toFloatBits());
        kit.line(0f, y, 0f, 0f, y, len, Z_COLOR.toFloatBits());
        mesh = kit.bake();
    }

    /** Draws the gizmo with an already-bound program. */
    public void render(ShaderProgram program) {
        if (mesh != null) {
            mesh.render(program, GL20.GL_LINES);
        }
    }

    public void dispose() {
        if (mesh != null) {
            mesh.dispose();
        }
    }
}

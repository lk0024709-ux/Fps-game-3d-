package com.brfps.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.brfps.util.Constants;
import com.brfps.world.MeshKit;

/**
 * Screen-corner orientation gizmo: the world XYZ axes drawn into a small square
 * viewport using only the world camera's rotation, so it always shows which way
 * the editor camera faces. All matrices and the box size are viewport-relative
 * (R46) and reused every frame (R6).
 */
public class CornerAxisIndicator {

    private static final float ORTHO_SPAN = 2.8f;
    private static final Color X_COLOR = new Color(0.95f, 0.25f, 0.25f, 1f);
    private static final Color Y_COLOR = new Color(0.35f, 0.90f, 0.35f, 1f);
    private static final Color Z_COLOR = new Color(0.30f, 0.55f, 0.98f, 1f);

    private final Mesh mesh;
    private final OrthographicCamera ortho = new OrthographicCamera();
    private final Matrix4 rotationOnly = new Matrix4();
    private final Matrix4 projView = new Matrix4();

    /** Bakes a unit-length axis triplet. */
    public CornerAxisIndicator() {
        MeshKit kit = new MeshKit();
        kit.line(0f, 0f, 0f, 1f, 0f, 0f, X_COLOR.toFloatBits());
        kit.line(0f, 0f, 0f, 0f, 1f, 0f, Y_COLOR.toFloatBits());
        kit.line(0f, 0f, 0f, 0f, 0f, 1f, Z_COLOR.toFloatBits());
        mesh = kit.bake();
        ortho.near = -20f;
        ortho.far = 20f;
    }

    /** Draws the gizmo into the bottom-left corner and restores GL state. */
    public void render(DebugShader shader, PerspectiveCamera world, int screenWidth, int screenHeight) {
        if (mesh == null) {
            return;
        }
        float unit = Math.min(screenWidth, screenHeight);
        int size = (int) (unit * Constants.AXIS_INDICATOR_SIZE);
        int margin = (int) (unit * Constants.EDITOR_MARGIN);
        if (size < 12) {
            return;
        }
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glViewport(margin, margin, size, size);
        ortho.viewportWidth = ORTHO_SPAN;
        ortho.viewportHeight = ORTHO_SPAN;
        ortho.position.set(0f, 0f, 0f);
        ortho.update();
        rotationOnly.set(world.view);
        // Matrix4.val is column-major: indices 12, 13 and 14 hold the translation.
        rotationOnly.val[12] = 0f;
        rotationOnly.val[13] = 0f;
        rotationOnly.val[14] = 0f;
        projView.set(ortho.combined).mul(rotationOnly);
        shader.bind(projView);
        mesh.render(shader.program(), GL20.GL_LINES);
        Gdx.gl.glViewport(0, 0, screenWidth, screenHeight);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
    }

    public void dispose() {
        if (mesh != null) {
            mesh.dispose();
        }
    }
}

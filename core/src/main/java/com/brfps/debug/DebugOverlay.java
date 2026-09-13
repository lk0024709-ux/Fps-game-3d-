package com.brfps.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.brfps.util.Constants;

/**
 * Owns the debug line shader and the editor visuals — world grid, origin axis
 * gizmo, corner orientation gizmo — plus the on-screen CAM mode button hit test.
 * All geometry is baked once; only the corner gizmo touches GL state per frame
 * (R6, R30, R44).
 */
public class DebugOverlay {

    private final DebugShader shader = new DebugShader();
    private final WorldGrid grid;
    private final AxisGizmo axes;
    private final CornerAxisIndicator indicator = new CornerAxisIndicator();
    private int drawCalls;

    /** Bakes the grid and axis gizmo for a square map of the given side length. */
    public DebugOverlay(float mapSize) {
        grid = new WorldGrid(mapSize);
        axes = new AxisGizmo();
    }

    /** True when the user asked to flip camera mode (F1 on desktop, CAM button on touch). */
    public boolean toggleRequested() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            return true;
        }
        if (!Gdx.input.justTouched()) {
            return false;
        }
        float margin = EditorCamera.buttonMargin();
        float x = Gdx.input.getX();
        float y = Gdx.input.getY();
        return x >= margin && x <= margin + modeButtonWidth()
                && y >= margin && y <= margin + modeButtonHeight();
    }

    /** Width of the top-left CAM button in pixels (viewport-relative, R46). */
    public static float modeButtonWidth() {
        return minSide() * Constants.MODE_BUTTON_SIZE * 2.6f;
    }

    /** Height of the top-left CAM button in pixels. */
    public static float modeButtonHeight() {
        return minSide() * Constants.MODE_BUTTON_SIZE;
    }

    private static float minSide() {
        return Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Draws grid + world axes + corner gizmo; call after the opaque world pass. */
    public void render(PerspectiveCamera camera) {
        drawCalls = 0;
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        shader.bind(camera);
        grid.render(shader.program());
        drawCalls++;
        axes.render(shader.program());
        drawCalls++;
        indicator.render(shader, camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        drawCalls++;
    }

    /** Line segments baked into the grid (debug HUD). */
    public int getGridLines() {
        return grid.getLines();
    }

    /** Draw calls issued by the last render(). */
    public int getDrawCalls() {
        return drawCalls;
    }

    public void dispose() {
        grid.dispose();
        axes.dispose();
        indicator.dispose();
        shader.dispose();
    }
}

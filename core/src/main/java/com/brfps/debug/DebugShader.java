package com.brfps.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

/** Minimal position + packed-color shader for the debug line meshes (R44). */
final class DebugShader {

    private static final String VERTEX_SHADER =
            "attribute vec4 a_position;\n"
            + "attribute vec4 a_color;\n"
            + "uniform mat4 u_projView;\n"
            + "varying vec4 v_color;\n"
            + "void main() {\n"
            + "  v_color = a_color;\n"
            + "  gl_Position = u_projView * a_position;\n"
            + "}\n";

    private static final String FRAGMENT_SHADER =
            "#ifdef GL_ES\n"
            + "precision mediump float;\n"
            + "#endif\n"
            + "varying vec4 v_color;\n"
            + "void main() {\n"
            + "  gl_FragColor = v_color;\n"
            + "}\n";

    private final ShaderProgram program = new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER);

    DebugShader() {
        if (!program.isCompiled()) {
            Gdx.app.error("DebugShader", "compile failed: " + program.getLog());
        }
    }

    /** Binds the program and uploads a world-space view-projection matrix. */
    void bind(PerspectiveCamera camera) {
        bind(camera.combined);
    }

    /** Binds the program and uploads an arbitrary matrix (used by the corner gizmo). */
    void bind(Matrix4 projView) {
        program.bind();
        program.setUniformMatrix("u_projView", projView);
    }

    ShaderProgram program() {
        return program;
    }

    void dispose() {
        program.dispose();
    }
}

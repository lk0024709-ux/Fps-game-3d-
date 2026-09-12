package com.brfps.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/** Minimal position + packed-color shader used to render the baked world meshes. */
final class FlatShader {

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

    FlatShader() {
        if (!program.isCompiled()) {
            Gdx.app.error("FlatShader", "compile failed: " + program.getLog());
        }
    }

    /** Binds the program and uploads the view-projection matrix. */
    void begin(PerspectiveCamera camera) {
        program.bind();
        program.setUniformMatrix("u_projView", camera.combined);
    }

    ShaderProgram program() {
        return program;
    }

    void dispose() {
        program.dispose();
    }
}

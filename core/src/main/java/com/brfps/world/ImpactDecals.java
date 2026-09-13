package com.brfps.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.brfps.util.Constants;

/**
 * Bullet holes: a ring buffer of small quads living in ONE dynamic mesh, so every
 * impact together costs a single draw call (R28). Each quad is oriented to the surface
 * it landed on and pushed a couple of centimetres off it to avoid z-fighting; adding a
 * decal allocates nothing (R6). The oldest decal is overwritten once the pool is full.
 */
public class ImpactDecals {

    private static final Color DECAL_COLOR = new Color(0.07f, 0.06f, 0.05f, 1f);

    private final Mesh mesh;
    private final float[] vertices;
    private final float[] quad = new float[16];
    private final int poolSize;
    private int nextSlot;
    private int used;
    private int drawCalls;
    private int triangles;

    public ImpactDecals() {
        poolSize = Constants.DECAL_POOL_SIZE;
        vertices = new float[poolSize * 16];
        short[] indices = new short[poolSize * 6];
        for (int i = 0; i < poolSize; i++) {
            int vertex = i * 4;
            int index = i * 6;
            indices[index] = (short) vertex;
            indices[index + 1] = (short) (vertex + 1);
            indices[index + 2] = (short) (vertex + 2);
            indices[index + 3] = (short) vertex;
            indices[index + 4] = (short) (vertex + 2);
            indices[index + 5] = (short) (vertex + 3);
        }
        // Dynamic VBO: only the 16 floats of the newest quad are re-uploaded per shot.
        mesh = new Mesh(false, poolSize * 4, poolSize * 6, MeshKit.attributes());
        mesh.setIndices(indices);
        mesh.setVertices(vertices); // all zero: unused slots are degenerate triangles
    }

    /**
     * Adds one bullet hole at an impact point, facing back along the surface normal.
     *
     * @param normalX unit surface normal (Y component is 0 for walls, 1 for ground)
     */
    public void add(float x, float y, float z, float normalX, float normalY, float normalZ) {
        // Tangent basis: any axis not parallel to the normal works as the helper.
        float helperX = Math.abs(normalY) < 0.9f ? 0f : 1f;
        float helperY = Math.abs(normalY) < 0.9f ? 1f : 0f;
        float ux = normalY * 0f - normalZ * helperY;
        float uy = normalZ * helperX - normalX * 0f;
        float uz = normalX * helperY - normalY * helperX;
        float uLength = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (uLength < 1e-5f) {
            return; // degenerate normal: skip rather than draw a broken quad
        }
        float inverse = 1f / uLength;
        ux *= inverse;
        uy *= inverse;
        uz *= inverse;
        // v = normal x u, already unit length because both inputs are unit and orthogonal.
        float vx = normalY * uz - normalZ * uy;
        float vy = normalZ * ux - normalX * uz;
        float vz = normalX * uy - normalY * ux;

        float half = Constants.DECAL_SIZE * 0.5f;
        float px = x + normalX * Constants.DECAL_OFFSET;
        float py = y + normalY * Constants.DECAL_OFFSET;
        float pz = z + normalZ * Constants.DECAL_OFFSET;
        float bits = DECAL_COLOR.toFloatBits();
        corner(0, px, py, pz, -ux * half - vx * half, -uy * half - vy * half,
                -uz * half - vz * half, bits);
        corner(4, px, py, pz, ux * half - vx * half, uy * half - vy * half,
                uz * half - vz * half, bits);
        corner(8, px, py, pz, ux * half + vx * half, uy * half + vy * half,
                uz * half + vz * half, bits);
        corner(12, px, py, pz, -ux * half + vx * half, -uy * half + vy * half,
                -uz * half + vz * half, bits);

        mesh.updateVertices(nextSlot * 16, quad, 0, 16);
        nextSlot = (nextSlot + 1) % poolSize;
        if (used < poolSize) {
            used++;
        }
    }

    /** Writes one corner of the pending quad into the scratch array. */
    private void corner(int offset, float px, float py, float pz,
                        float dx, float dy, float dz, float colorBits) {
        quad[offset] = px + dx;
        quad[offset + 1] = py + dy;
        quad[offset + 2] = pz + dz;
        quad[offset + 3] = colorBits;
    }

    /** Draws every decal with the world's already-bound program (one draw call). */
    public void render(ShaderProgram program) {
        if (used == 0) {
            drawCalls = 0;
            triangles = 0;
            return;
        }
        mesh.render(program, GL20.GL_TRIANGLES);
        drawCalls = 1;
        triangles = used * 2;
    }

    /** Draw calls issued by the last render(). */
    public int getDrawCalls() {
        return drawCalls;
    }

    /** Triangles drawn by the last render(). */
    public int getTrianglesRendered() {
        return triangles;
    }

    /** Bullet holes currently in the pool. */
    public int getUsedCount() {
        return used;
    }

    public void dispose() {
        mesh.dispose();
    }
}

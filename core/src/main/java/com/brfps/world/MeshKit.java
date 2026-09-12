package com.brfps.world;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * Collects position + packed-color vertices and bakes them into an indexed
 * Mesh (build-time only; nothing here runs per frame).
 */
public class MeshKit {

    private static final VertexAttributes ATTRIBUTES = new VertexAttributes(
            new VertexAttribute(Usage.Position, 3, "a_position"),
            VertexAttribute.ColorPacked());

    public final FloatArray vertices = new FloatArray();
    public final ShortArray indices = new ShortArray();

    /** Adds a single vertex. */
    public void vertex(float x, float y, float z, float colorBits) {
        if (vertices.size / 4 >= Short.MAX_VALUE) {
            throw new IllegalStateException("MeshKit: too many vertices for short indices");
        }
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(colorBits);
    }

    /** Closes the quad formed by the last 4 vertices (triangles 0-1-2 and 0-2-3). */
    public void closeQuad() {
        int v = vertices.size / 4 - 4;
        indices.add((short) v);
        indices.add((short) (v + 1));
        indices.add((short) (v + 2));
        indices.add((short) v);
        indices.add((short) (v + 2));
        indices.add((short) (v + 3));
    }

    /** Adds an axis-aligned horizontal quad with one color (corners CCW seen from above). */
    public void topQuad(float x0, float z0, float x1, float z1, float y, float colorBits) {
        topQuad(x0, z0, x1, z1, y, colorBits, colorBits, colorBits, colorBits);
    }

    /** Adds an axis-aligned horizontal quad with per-corner colors (00, 01, 11, 10). */
    public void topQuad(float x0, float z0, float x1, float z1, float y,
                        float c00, float c01, float c11, float c10) {
        vertex(x0, y, z0, c00);
        vertex(x0, y, z1, c01);
        vertex(x1, y, z1, c11);
        vertex(x1, y, z0, c10);
        closeQuad();
    }

    /** Adds a horizontal quad from 4 explicit corners, which must be CCW seen from above. */
    public void quad(float ax, float az, float bx, float bz, float cx, float cz,
                     float dx, float dz, float y, float colorBits) {
        vertex(ax, y, az, colorBits);
        vertex(bx, y, bz, colorBits);
        vertex(cx, y, cz, colorBits);
        vertex(dx, y, dz, colorBits);
        closeQuad();
    }

    /** Adds a vertical quad from 4 explicit corners, which must be CCW seen from the front. */
    public void verticalQuad(float x0, float y0, float z0, float x1, float y1, float z1,
                             float x2, float y2, float z2, float x3, float y3, float z3,
                             float colorBits) {
        vertex(x0, y0, z0, colorBits);
        vertex(x1, y1, z1, colorBits);
        vertex(x2, y2, z2, colorBits);
        vertex(x3, y3, z3, colorBits);
        closeQuad();
    }

    /** Adds a 5-face box (no bottom) centered on (cx, cz), standing on y=0. */
    public void box(float cx, float cz, float halfWidth, float height,
                    float topBits, float sideBits) {
        float x0 = cx - halfWidth;
        float x1 = cx + halfWidth;
        float z0 = cz - halfWidth;
        float z1 = cz + halfWidth;
        // +X face
        verticalQuad(x1, 0f, z1, x1, 0f, z0, x1, height, z0, x1, height, z1, sideBits);
        // -X face
        verticalQuad(x0, 0f, z0, x0, 0f, z1, x0, height, z1, x0, height, z0, sideBits);
        // +Z face
        verticalQuad(x0, 0f, z1, x1, 0f, z1, x1, height, z1, x0, height, z1, sideBits);
        // -Z face
        verticalQuad(x1, 0f, z0, x0, 0f, z0, x0, height, z0, x1, height, z0, sideBits);
        // Top face
        topQuad(x0, z0, x1, z1, height, topBits);
    }

    /** Adds one line segment (2 vertices + 2 indices, for GL_LINES meshes). */
    public void line(float x0, float y0, float z0, float x1, float y1, float z1, float colorBits) {
        if (vertices.size / 4 >= Short.MAX_VALUE) {
            throw new IllegalStateException("MeshKit: too many vertices for short indices");
        }
        indices.add((short) (vertices.size / 4));
        indices.add((short) (vertices.size / 4 + 1));
        vertex(x0, y0, z0, colorBits);
        vertex(x1, y1, z1, colorBits);
    }

    /** Bakes the collected geometry into a static Mesh (null when empty). */
    public Mesh bake() {
        if (indices.size == 0) {
            return null;
        }
        Mesh mesh = new Mesh(true, vertices.size / 4, indices.size, ATTRIBUTES);
        mesh.setVertices(vertices.items, 0, vertices.size);
        mesh.setIndices(indices.items, 0, indices.size);
        return mesh;
    }
}

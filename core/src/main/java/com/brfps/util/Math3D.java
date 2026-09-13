package com.brfps.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Small allocation-free 3D helpers. Never allocate in the render loop. */
public final class Math3D {

    private Math3D() {
    }

    /**
     * Writes a unit look direction for yaw/pitch in degrees into out, in this project's
     * convention: yaw 0 looks along +X, yaw 90 along +Z, positive pitch is up. Both
     * cameras go through here, and so must every AI aim calculation (R7) — one formula,
     * no second convention.
     */
    public static Vector3 direction(float yawDeg, float pitchDeg, Vector3 out) {
        float cosPitch = MathUtils.cosDeg(pitchDeg);
        out.set(MathUtils.cosDeg(yawDeg) * cosPitch, MathUtils.sinDeg(pitchDeg),
                MathUtils.sinDeg(yawDeg) * cosPitch);
        if (out.x == 0f && out.y == 0f && out.z == 0f) {
            out.set(1f, 0f, 0f); // degenerate: fall back to +X rather than a zero vector
        }
        return out.nor();
    }

    /** Horizontal (XZ) distance between two points. */
    public static float distanceXZ(Vector3 a, Vector3 b) {
        float dx = a.x - b.x;
        float dz = a.z - b.z;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }

    /** Horizontal squared distance - use when only comparing distances. */
    public static float distanceXZSquared(Vector3 a, Vector3 b) {
        float dx = a.x - b.x;
        float dz = a.z - b.z;
        return dx * dx + dz * dz;
    }

    public static float clamp(float value, float min, float max) {
        return value < min ? min : (value > max ? max : value);
    }

    /** Frame-rate independent smoothing factor. */
    public static float damp(float current, float target, float lambda, float delta) {
        return MathUtils.lerp(current, target, 1f - (float) Math.exp(-lambda * delta));
    }
}

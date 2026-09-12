package com.brfps.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/** Small allocation-free 3D helpers. Never allocate in the render loop. */
public final class Math3D {

    private Math3D() {
    }

    /** Writes a unit direction vector for the given yaw/pitch (degrees) into out. */
    public static Vector3 direction(float yawDeg, float pitchDeg, Vector3 out) {
        float yaw = yawDeg * MathUtils.degreesToRadians;
        float pitch = pitchDeg * MathUtils.degreesToRadians;
        float cosPitch = MathUtils.cos(pitch);
        out.set(MathUtils.sin(yaw) * cosPitch, MathUtils.sin(pitch), -MathUtils.cos(yaw) * cosPitch);
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

package com.brfps.player;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.brfps.util.Constants;
import com.brfps.util.Math3D;

/**
 * First-person view: yaw/pitch look plus the stance-dependent eye height, applied to
 * a shared PerspectiveCamera. Yaw 0 looks along +X and yaw 90 along +Z (the same
 * convention as the editor camera), pitch is clamped by Constants.PITCH_MIN/MAX.
 */
public class FirstPersonCamera {

    private final PerspectiveCamera camera;
    private final Vector3 direction = new Vector3();
    private float yaw = 90f;
    private float pitch;

    public FirstPersonCamera(PerspectiveCamera camera) {
        this.camera = camera;
    }

    /**
     * Applies a look drag.
     *
     * @param deltaX      horizontal drag in pixels (right is positive)
     * @param deltaY      vertical drag in pixels (down is positive, looking down)
     * @param sensitivity degrees per pixel for the active control scheme
     */
    public void look(float deltaX, float deltaY, float sensitivity) {
        yaw += deltaX * sensitivity;
        pitch -= deltaY * sensitivity;
        if (yaw > 360f) {
            yaw -= 360f;
        } else if (yaw < -360f) {
            yaw += 360f;
        }
        if (pitch > Constants.PITCH_MAX) {
            pitch = Constants.PITCH_MAX;
        } else if (pitch < Constants.PITCH_MIN) {
            pitch = Constants.PITCH_MIN;
        }
    }

    /** Moves the camera to the player's eyes and points it along yaw/pitch. */
    public void apply(Player player) {
        Vector3 feet = player.getPosition();
        camera.position.set(feet.x, feet.y + player.eyeHeight(), feet.z);
        Math3D.direction(yaw, pitch, direction);
        camera.direction.set(direction);
        camera.up.set(0f, 1f, 0f);
        camera.update();
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}

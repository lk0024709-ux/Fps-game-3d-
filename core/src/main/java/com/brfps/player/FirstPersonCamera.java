package com.brfps.player;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.brfps.util.Constants;
import com.brfps.util.Math3D;

/**
 * First-person view: yaw/pitch look plus the stance-dependent eye height, applied to
 * a shared PerspectiveCamera. Yaw 0 looks along +X and yaw 90 along +Z (the same
 * convention as the editor camera), pitch is clamped by Constants.PITCH_MIN/MAX.
 * Since master M5 {@link #apply} also takes the transient recoil / shake / roll offsets,
 * which live outside the stored aim so they always recover fully.
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

    /**
     * Eases the field of view toward the sprint value while sprinting and back to the
     * base value otherwise; takes effect in the next {@link #apply} (M34 HUD phase A1).
     */
    public void updateFov(float delta, boolean sprinting) {
        float target = sprinting ? Constants.SPRINT_FOV_DEGREES : Constants.FOV_DEGREES;
        float range = Constants.SPRINT_FOV_DEGREES - Constants.FOV_DEGREES;
        float step = range / Constants.FOV_TRANSITION_TIME * delta;
        float current = camera.fieldOfView;
        if (current < target) {
            camera.fieldOfView = Math.min(target, current + step);
        } else if (current > target) {
            camera.fieldOfView = Math.max(target, current - step);
        }
    }

    /**
     * Moves the camera to the player's eyes and points it along yaw/pitch plus this
     * frame's transient view offsets: recoil (master M5, full recovery) and camera
     * shake. The offsets are added to the look direction only — they are never written
     * back into yaw/pitch, which is what makes the recovery exact — and roll tilts the
     * up vector, so it never contaminates the aim.
     *
     * @param yawOffset   degrees, from ScreenShake
     * @param pitchOffset degrees, from RecoilState + ScreenShake
     * @param rollDegrees degrees of camera roll, from ScreenShake
     */
    public void apply(Player player, float yawOffset, float pitchOffset, float rollDegrees) {
        Vector3 feet = player.getPosition();
        camera.position.set(feet.x, feet.y + player.eyeHeight(), feet.z);
        Math3D.direction(yaw + yawOffset,
                MathUtils.clamp(pitch + pitchOffset, Constants.PITCH_MIN, Constants.PITCH_MAX),
                direction);
        camera.direction.set(direction);
        camera.up.set(0f, 1f, 0f);
        if (rollDegrees != 0f) {
            camera.up.rotate(rollDegrees, direction.x, direction.y, direction.z);
        }
        camera.update();
    }

    /** The aim without any recoil or shake — what movement and bots must use. */
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

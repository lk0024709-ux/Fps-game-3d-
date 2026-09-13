package com.brfps.weapons;

import com.badlogic.gdx.math.MathUtils;
import com.brfps.util.Constants;

/**
 * Transient recoil offset in degrees, added to the aim only while it is alive (master
 * M5). Full recovery, Free Fire style: while the trigger is held the offset only bleeds
 * slowly, so a spray climbs; the moment the shots stop it decays back to zero and the
 * crosshair returns to exactly where the player was aiming. Nothing is ever written back
 * into the camera's yaw/pitch, so there is no partial transfer to tune or to remove.
 * Pure math: no libGDX object is touched, it never allocates (R6) and it can be
 * simulated off device (see HANDOFF -> M5 verification).
 */
public class RecoilState {

    private float pitch;
    private float yaw;
    private float remaining;
    private float duration = 1f; // never 0: guards the division in recovery()
    private int shotsInBurst;
    private float sinceLastShot = Float.MAX_VALUE;

    /** Adds one shot's kick. Every shot pushes the recovery further out. */
    public void kick(float recoilDegrees) {
        sinceLastShot = 0f;
        if (remaining <= 0f) {
            shotsInBurst = 0; // a fresh burst restarts the horizontal pattern
        }
        shotsInBurst++;
        float climb = recoilDegrees;
        if (shotsInBurst > Constants.RECOIL_RAMP_START_SHOT) {
            climb *= Constants.RECOIL_RAMP_SCALE; // a sustained spray climbs harder
        }
        if (pitch + climb > Constants.RECOIL_PITCH_MAX) {
            climb = Constants.RECOIL_PITCH_MAX - pitch; // saturates, never runs away
        }
        pitch += climb;
        yaw = MathUtils.clamp(yaw + horizontalPattern(shotsInBurst),
                -Constants.RECOIL_YAW_MAX, Constants.RECOIL_YAW_MAX);
        duration = MathUtils.clamp(recoilDegrees * Constants.RECOIL_RECOVERY_PER_DEGREE
                        + Constants.RECOIL_RECOVERY_MIN,
                Constants.RECOIL_RECOVERY_MIN, Constants.RECOIL_RECOVERY_MAX);
        remaining = duration;
    }

    /**
     * Horizontal sway for one shot of the burst: a fixed left-right pattern that widens
     * down the burst plus a small random jitter, so a spray traces a readable shape
     * instead of climbing in a perfectly straight line.
     */
    private float horizontalPattern(int shot) {
        float pattern = (shot % 2 == 0 ? 1f : -1f)
                * Constants.RECOIL_YAW_ALTERNATE * (1f + (shot >> 1) * 0.25f);
        return pattern + MathUtils.random(-Constants.RECOIL_YAW_RANDOM,
                Constants.RECOIL_YAW_RANDOM);
    }

    /**
     * Decays the offset toward zero.
     *
     * @param triggerHeld while the player is still spraying the offset only bleeds
     *                    slowly ({@code RECOIL_HOLD_DECAY_PER_SECOND}) and the recovery
     *                    clock is pushed back, which is what makes the spray climb
     */
    public void update(float delta, boolean triggerHeld) {
        sinceLastShot += delta;
        if (remaining <= 0f) {
            return;
        }
        if (triggerHeld) {
            // Keep the kick alive between shots, minus a slow bleed so an endless spray
            // settles into a controllable plateau instead of pinning at the cap.
            float retained = (float) Math.exp(-Constants.RECOIL_HOLD_DECAY_PER_SECOND * delta);
            pitch *= retained;
            yaw *= retained;
            if (sinceLastShot >= Constants.RECOIL_HOLD_TIME) {
                remaining = duration; // still on the trigger: no recovery yet
            }
            return;
        }
        remaining -= delta;
        if (remaining <= 0f) {
            reset();
        } else {
            float retained = (float) Math.exp(-Constants.RECOIL_RECOVERY_SPEED * delta
                    / Math.max(duration, 0.01f));
            pitch *= retained;
            yaw *= retained;
        }
    }

    private void reset() {
        remaining = 0f;
        pitch = 0f;
        yaw = 0f;
        shotsInBurst = 0;
    }

    /** Vertical aim offset in degrees, positive is up. */
    public float pitchOffset() {
        return pitch;
    }

    /** Horizontal aim offset in degrees. */
    public float yawOffset() {
        return yaw;
    }

    /** 0 when settled, 1 at full kick: drives the crosshair bloom. */
    public float intensity() {
        return remaining <= 0f ? 0f : MathUtils.clamp(remaining / duration, 0f, 1f);
    }

    public boolean isActive() {
        return remaining > 0f;
    }

    /** Shots in the current burst, used by the horizontal pattern. */
    public int getShotsInBurst() {
        return shotsInBurst;
    }
}

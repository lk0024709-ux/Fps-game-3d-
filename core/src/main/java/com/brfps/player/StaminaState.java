package com.brfps.player;

import com.brfps.util.Constants;

/**
 * Sprint stamina: drains while the player is really sprinting, regens otherwise.
 * Empty locks sprint until the pool regens past the relock level. Pure math with no
 * libGDX dependency (like weapons.RecoilState), so it can be simulated off device.
 */
public class StaminaState {

    private float stamina = Constants.STAMINA_MAX;
    private boolean locked;

    /**
     * Advances the pool one step.
     *
     * @param delta     clamped frame step in seconds
     * @param sprinting true while the player is really sprinting (intent + moving)
     */
    public void update(float delta, boolean sprinting) {
        if (sprinting && !locked) {
            stamina -= Constants.STAMINA_DRAIN_PER_SECOND * delta;
            if (stamina <= 0f) {
                stamina = 0f;
                locked = true;
            }
        } else if (!sprinting) {
            stamina += Constants.STAMINA_REGEN_PER_SECOND * delta;
            if (stamina >= Constants.STAMINA_MAX) {
                stamina = Constants.STAMINA_MAX;
            }
            if (stamina >= Constants.STAMINA_RELOCK_LEVEL) {
                locked = false;
            }
        }
    }

    /** Current stamina, 0..STAMINA_MAX. */
    public float getStamina() {
        return stamina;
    }

    /** False once the pool hits zero, until it regens past the relock level. */
    public boolean canSprint() {
        return !locked && stamina > 0f;
    }

    public boolean isLocked() {
        return locked;
    }

    public void reset() {
        stamina = Constants.STAMINA_MAX;
        locked = false;
    }
}

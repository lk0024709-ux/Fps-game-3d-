package com.brfps.player;

import com.badlogic.gdx.math.Vector3;
import com.brfps.util.Constants;

/**
 * The local player's body: feet position, vertical velocity, stance, health/armor and
 * sprint stamina. Movement rules live in MovementController, the view in FirstPersonCamera.
 */
public class Player {

    /** Standing, crouched, sitting or prone; changes eye height and speed. */
    public enum Stance { STAND, CROUCH, SIT, PRONE }

    private final Vector3 position = new Vector3();
    private final PlayerStats stats = new PlayerStats();
    private final StaminaState stamina = new StaminaState();
    private Stance stance = Stance.STAND;
    private float verticalVelocity;
    private boolean onGround = true;

    /** Places the player at a spawn point, standing on the ground. */
    public void spawn(float x, float z) {
        position.set(x, 0f, z);
        verticalVelocity = 0f;
        onGround = true;
        stance = Stance.STAND;
        stats.reset();
        stamina.reset();
    }

    /** Feet position, in world units; y is the ground contact point. */
    public Vector3 getPosition() {
        return position;
    }

    public PlayerStats getStats() {
        return stats;
    }

    public StaminaState getStamina() {
        return stamina;
    }

    public Stance getStance() {
        return stance;
    }

    public void setStance(Stance stance) {
        this.stance = stance;
    }

    public boolean isCrouching() {
        return stance == Stance.CROUCH;
    }

    public boolean isSitting() {
        return stance == Stance.SIT;
    }

    public boolean isProne() {
        return stance == Stance.PRONE;
    }

    public float getVerticalVelocity() {
        return verticalVelocity;
    }

    public void setVerticalVelocity(float verticalVelocity) {
        this.verticalVelocity = verticalVelocity;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    /** Eye height for the current stance (1.6 stand, 1.0 crouch, 0.8 sit, 0.5 prone). */
    public float eyeHeight() {
        if (stance == Stance.CROUCH) {
            return Constants.CROUCH_EYE_HEIGHT;
        }
        if (stance == Stance.SIT) {
            return Constants.SIT_EYE_HEIGHT;
        }
        if (stance == Stance.PRONE) {
            return Constants.PRONE_EYE_HEIGHT;
        }
        return Constants.EYE_HEIGHT;
    }
}

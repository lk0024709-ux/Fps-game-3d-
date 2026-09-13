package com.brfps.player;

import com.badlogic.gdx.math.Vector3;
import com.brfps.util.Constants;

/**
 * The local player's body: feet position, vertical velocity, stance, and health/armor.
 * Movement rules live in MovementController, the view in FirstPersonCamera.
 */
public class Player {

    /** Standing or crouched; changes eye height and speed. */
    public enum Stance { STAND, CROUCH }

    private final Vector3 position = new Vector3();
    private final PlayerStats stats = new PlayerStats();
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
    }

    /** Feet position, in world units; y is the ground contact point. */
    public Vector3 getPosition() {
        return position;
    }

    public PlayerStats getStats() {
        return stats;
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

    /** Eye height for the current stance (SPEC: 1.6 m standing, 1.0 m crouched). */
    public float eyeHeight() {
        return stance == Stance.CROUCH ? Constants.CROUCH_EYE_HEIGHT : Constants.EYE_HEIGHT;
    }
}

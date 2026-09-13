package com.brfps.player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.brfps.input.InputState;
import com.brfps.util.Constants;
import com.brfps.world.BuildingCollider;

/**
 * Turns an InputState into player motion: stance, walk/jog/sprint/crouch/prone/sit
 * speed, jump, gravity, ground snapping (plinths count as walkable ground), wall
 * push-out and map bounds. Sprinting drains the stamina pool and an empty pool forces
 * the jog until it regens past the relock level. Fixed-timestep style with a clamped
 * delta so a stalled frame cannot tunnel a player through a wall; allocation-free (R6).
 */
public class MovementController {

    private final Player player;
    /** Null on maps without buildings; the player then only meets the map bounds. */
    private final BuildingCollider collider;
    private final float halfExtent;
    private final Vector3 forward = new Vector3();
    private final Vector3 side = new Vector3();
    private boolean sprinting;

    public MovementController(Player player, BuildingCollider collider, float mapSize) {
        this.player = player;
        this.collider = collider;
        this.halfExtent = mapSize * 0.5f - Constants.BOUNDS_MARGIN;
    }

    /** Advances the player one step along the look direction yaw. */
    public void update(float delta, InputState input, float yaw) {
        float dt = delta > Constants.MAX_STEP_DELTA ? Constants.MAX_STEP_DELTA : delta;
        if (input.sit) {
            player.setStance(Player.Stance.SIT);
        } else if (input.prone) {
            player.setStance(Player.Stance.PRONE);
        } else if (input.crouch) {
            player.setStance(Player.Stance.CROUCH);
        } else {
            player.setStance(Player.Stance.STAND);
        }

        boolean moving = input.moveX != 0f || input.moveY != 0f;
        boolean wantsSprint = input.sprint && player.getStance() == Player.Stance.STAND;
        player.getStamina().update(dt, wantsSprint && moving);
        sprinting = wantsSprint && moving && player.getStamina().canSprint();

        Vector3 position = player.getPosition();
        float speed = speedFor(input);
        float cosYaw = MathUtils.cosDeg(yaw);
        float sinYaw = MathUtils.sinDeg(yaw);
        forward.set(cosYaw, 0f, sinYaw).scl(input.moveY);
        side.set(-sinYaw, 0f, cosYaw).scl(input.moveX);
        forward.add(side);
        if (forward.len2() > 1f) {
            forward.nor(); // keep diagonal movement from being faster
        }
        position.x += forward.x * speed * dt;
        position.z += forward.z * speed * dt;
        if (collider != null) {
            collider.resolve(position, Constants.PLAYER_RADIUS);
        }
        clampToBounds(position);
        applyVertical(position, input, dt);
    }

    /** Gravity, jump, ground contact and step-up onto plinths. */
    private void applyVertical(Vector3 position, InputState input, float dt) {
        float ground = collider != null ? collider.groundHeightAt(position.x, position.z) : 0f;

        if (input.jump && player.isOnGround()) {
            player.setVerticalVelocity(Constants.JUMP_VELOCITY);
            player.setOnGround(false);
        }
        float verticalVelocity = player.getVerticalVelocity() + Constants.GRAVITY * dt;
        float nextY = position.y + verticalVelocity * dt;

        // Step assist: walk up low ledges instead of getting stuck against them.
        if (player.isOnGround() && ground > position.y
                && ground - position.y <= Constants.STEP_UP_HEIGHT) {
            position.y = ground;
            nextY = ground;
            verticalVelocity = 0f;
        }

        if (nextY <= ground) {
            nextY = ground;
            verticalVelocity = 0f;
            player.setOnGround(true);
        } else {
            player.setOnGround(false);
        }
        position.y = nextY;
        player.setVerticalVelocity(verticalVelocity);
    }

    /**
     * Sit and prone beat everything, crouch beats sprint, and sprint needs a standing
     * stance plus stamina; an empty pool forces the jog until it regens (M34 A1).
     */
    private float speedFor(InputState input) {
        if (player.getStance() == Player.Stance.SIT) {
            return Constants.SIT_SPEED;
        }
        if (player.getStance() == Player.Stance.PRONE) {
            return Constants.PRONE_SPEED;
        }
        if (player.isCrouching()) {
            return Constants.CROUCH_SPEED;
        }
        if (sprinting) {
            return Constants.SPRINT_SPEED;
        }
        if (input.sprint && (input.moveX != 0f || input.moveY != 0f)) {
            return Constants.JOG_SPEED; // stamina empty: jog until it regens past 30
        }
        if (input.analogMove) {
            float magnitude = (float) Math.sqrt(input.moveX * input.moveX
                    + input.moveY * input.moveY);
            if (magnitude >= Constants.JOYSTICK_JOG_DEFLECTION) {
                return Constants.JOG_SPEED;
            }
        }
        return Constants.WALK_SPEED;
    }

    /** True while the player is really sprinting (intent + moving + stamina left). */
    public boolean isSprinting() {
        return sprinting;
    }

    private void clampToBounds(Vector3 position) {
        if (position.x > halfExtent) {
            position.x = halfExtent;
        } else if (position.x < -halfExtent) {
            position.x = -halfExtent;
        }
        if (position.z > halfExtent) {
            position.z = halfExtent;
        } else if (position.z < -halfExtent) {
            position.z = -halfExtent;
        }
    }

    /** Collision boxes backing this controller (debug/HUD). */
    public int getColliderBoxCount() {
        return collider == null ? 0 : collider.getWallCount() + collider.getPlinthCount();
    }
}

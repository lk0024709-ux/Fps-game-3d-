package com.brfps.player;

import com.badlogic.gdx.math.MathUtils;
import com.brfps.util.Constants;

/**
 * Additive, exponentially decaying camera shake (master M5). Every impulse raises the
 * amplitude and re-seeds the noise, so two overlapping shots never cancel each other
 * out. The offset is a fixed-frequency noise in degrees applied on top of the aim —
 * unlike recoil it is pure noise and settles back to zero by itself, and it never moves
 * the player's aim permanently (R6: no allocation, no libGDX object touched).
 */
public class ScreenShake {

    private float amplitude;
    private float time;
    private float seedX;
    private float seedY;
    private float seedRoll;
    private float offsetX;
    private float offsetY;
    private float roll;

    /** Adds one shake impulse, in degrees of camera offset. */
    public void add(float amount) {
        if (amount <= 0f) {
            return;
        }
        amplitude = Math.min(amplitude + amount, Constants.SHAKE_MAX_AMPLITUDE);
        seedX = MathUtils.random(0f, 360f);
        seedY = MathUtils.random(0f, 360f);
        seedRoll = MathUtils.random(0f, 360f);
    }

    public void update(float delta) {
        if (amplitude <= 0f) {
            offsetX = 0f;
            offsetY = 0f;
            roll = 0f;
            return;
        }
        time += delta;
        amplitude -= amplitude * Constants.SHAKE_DECAY_PER_SECOND * delta;
        if (amplitude < Constants.SHAKE_EPSILON) {
            amplitude = 0f;
            time = 0f;
        }
        float degrees = time * Constants.SHAKE_FREQUENCY;
        offsetX = amplitude * MathUtils.sinDeg(degrees + seedX);
        offsetY = amplitude * 0.75f * MathUtils.sinDeg(degrees * 1.31f + seedY);
        roll = amplitude * Constants.SHAKE_ROLL_SCALE * MathUtils.sinDeg(degrees * 0.87f + seedRoll);
    }

    /** Yaw offset in degrees to add to the aim for this frame. */
    public float offsetX() {
        return offsetX;
    }

    /** Pitch offset in degrees to add to the aim for this frame. */
    public float offsetY() {
        return offsetY;
    }

    /** Camera roll in degrees; applied to the camera's up vector, not to the aim. */
    public float roll() {
        return roll;
    }

    public boolean isActive() {
        return amplitude > 0f;
    }

    /** Current amplitude in degrees (debug readout). */
    public float getAmplitude() {
        return amplitude;
    }
}

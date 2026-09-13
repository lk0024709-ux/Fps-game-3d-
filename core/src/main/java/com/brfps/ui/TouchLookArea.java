package com.brfps.ui;

/**
 * Right-half drag-to-aim area for touch play (R45): accumulates the pixel delta
 * since the last read and hands it over exactly once, so a frame never loses or
 * double-counts a drag.
 */
public class TouchLookArea {

    private float deltaX;
    private float deltaY;
    private float lastX;
    private float lastY;
    private boolean active;

    /** Finger down: start a new drag from this point. */
    public void begin(float x, float y) {
        lastX = x;
        lastY = y;
        deltaX = 0f;
        deltaY = 0f;
        active = true;
    }

    /** Finger moved: add to the pending delta. */
    public void drag(float x, float y) {
        deltaX += x - lastX;
        deltaY += y - lastY;
        lastX = x;
        lastY = y;
    }

    /** Finger up: drop any unread drag. */
    public void end() {
        active = false;
        deltaX = 0f;
        deltaY = 0f;
    }

    public boolean isActive() {
        return active;
    }

    /** Horizontal pixels since the last read; clears the accumulator. */
    public float consumeDX() {
        float value = deltaX;
        deltaX = 0f;
        return value;
    }

    /** Vertical pixels since the last read; clears the accumulator. */
    public float consumeDY() {
        float value = deltaY;
        deltaY = 0f;
        return value;
    }
}

package com.brfps.input;

/**
 * One frame of player intent: filled by InputManager, consumed by the movement and
 * camera controllers. A plain mutable snapshot so the render loop never allocates (R6).
 */
public class InputState {

    /** Strafe axis, -1 (left) .. 1 (right). */
    public float moveX;

    /** Forward axis, -1 (back) .. 1 (forward). */
    public float moveY;

    /** Look drag since the last frame, in pixels. */
    public float lookDX;

    /** Look drag since the last frame, in pixels. */
    public float lookDY;

    /** Jump requested this frame (touch button or Space). */
    public boolean jump;

    /** Crouch held this frame (touch button, Ctrl or C). */
    public boolean crouch;

    /** Sprint requested (Shift, or the stick pushed to full deflection). */
    public boolean sprint;

    /** Trigger held this frame (FIRE button or left mouse button). */
    public boolean fire;

    /** Reload requested this frame (R key; touch reloads automatically when empty). */
    public boolean reload;

    /** Clears every field; called at the start of each frame. */
    public void reset() {
        moveX = 0f;
        moveY = 0f;
        lookDX = 0f;
        lookDY = 0f;
        jump = false;
        crouch = false;
        sprint = false;
        fire = false;
        reload = false;
    }
}

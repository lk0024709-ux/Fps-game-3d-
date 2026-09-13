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

    /** Crouch held this frame (touch toggle, Ctrl or C). */
    public boolean crouch;

    /** Sit requested this frame (touch SIT toggle or X). */
    public boolean sit;

    /** Prone requested this frame (touch SLP toggle or Z). */
    public boolean prone;

    /** Sprint requested (Shift, the sprint button, or the stick at full deflection). */
    public boolean sprint;

    /** True when moveX/moveY come from the analog stick (jog zone applies). */
    public boolean analogMove;

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
        sit = false;
        prone = false;
        sprint = false;
        analogMove = false;
        fire = false;
        reload = false;
    }
}

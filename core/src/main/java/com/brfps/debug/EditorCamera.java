package com.brfps.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.brfps.util.Constants;
import com.brfps.util.Math3D;

/**
 * Free-fly editor camera: WASD/arrows + Q/E with Shift on desktop, a left-half drag
 * joystick plus two altitude buttons on touch, and a right-half (or right mouse
 * button) drag to look. Drives a PerspectiveCamera, allocates nothing per frame
 * (R6, R30) and keeps every touch zone viewport-relative (R45, R46).
 */
public class EditorCamera {

    private static final int MAX_POINTERS = 4;
    private static final int MODE_MOVE = 0;
    private static final int MODE_LOOK = 1;
    private static final int MODE_UP = 2;
    private static final int MODE_DOWN = 3;
    private static final int MODE_IGNORE = 4;
    private static final float MAX_DELTA = 0.1f;

    private final PerspectiveCamera camera;
    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();
    private final Vector3 step = new Vector3();
    private final boolean[] wasTouched = new boolean[MAX_POINTERS];
    private final float[] lastX = new float[MAX_POINTERS];
    private final float[] lastY = new float[MAX_POINTERS];
    private final float[] originX = new float[MAX_POINTERS];
    private final float[] originY = new float[MAX_POINTERS];
    private final int[] pointerMode = new int[MAX_POINTERS];
    private final float halfExtent;

    private float yaw = 90f;
    private float pitch = -25f;
    private float moveX;
    private float moveY;
    private float vertical;
    private boolean active;

    /** Creates a controller for a square map of the given side length. */
    public EditorCamera(PerspectiveCamera camera, float mapSize) {
        this.camera = camera;
        this.halfExtent = mapSize * 0.5f + 40f;
    }

    public boolean isActive() {
        return active;
    }

    /** Switches the editor camera on or off; entering resets to a map overview. */
    public void setActive(boolean active) {
        this.active = active;
        for (int p = 0; p < MAX_POINTERS; p++) {
            wasTouched[p] = false;
            pointerMode[p] = MODE_MOVE;
        }
        moveX = 0f;
        moveY = 0f;
        vertical = 0f;
        if (active) {
            yaw = 90f;
            pitch = -25f;
            camera.far = Constants.EDITOR_FAR_PLANE;
            camera.position.set(0f, Constants.EDITOR_START_Y, -halfExtent * 0.7f);
            apply();
        } else {
            camera.far = Constants.FAR_PLANE;
        }
    }

    /** Reads input and moves the camera; call once per frame while active. */
    public void update(float delta) {
        if (!active) {
            return;
        }
        readTouch();
        readKeys();
        float dt = delta > MAX_DELTA ? MAX_DELTA : delta;
        float speed = sprint() ? Constants.EDITOR_SPRINT_SPEED : Constants.EDITOR_SPEED;
        direction(forward);
        right.set(MathUtils.cosDeg(yaw + 90f), 0f, MathUtils.sinDeg(yaw + 90f));
        step.set(forward).scl(moveY * speed * dt);
        camera.position.add(step);
        step.set(right).scl(moveX * speed * dt);
        camera.position.add(step);
        camera.position.y += vertical * speed * dt;
        clampPosition();
        apply();
    }

    public float getX() {
        return camera.position.x;
    }

    public float getY() {
        return camera.position.y;
    }

    public float getZ() {
        return camera.position.z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    /** Altitude button edge length in pixels (viewport-relative, R46). */
    public static float buttonSize() {
        return minScreenSide() * Constants.EDITOR_BUTTON_SIZE;
    }

    /** Screen margin in pixels shared by the corner buttons and the axis gizmo. */
    public static float buttonMargin() {
        return minScreenSide() * Constants.EDITOR_MARGIN;
    }

    private static float minScreenSide() {
        return Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void readKeys() {
        float f = 0f;
        float s = 0f;
        float v = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            f += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            f -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            s += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            s -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            v += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            v -= 1f;
        }
        if (f != 0f) {
            moveY = f;
        }
        if (s != 0f) {
            moveX = s;
        }
        if (v != 0f) {
            vertical = v;
        }
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            yaw += Gdx.input.getDeltaX() * Constants.EDITOR_LOOK_SENSITIVITY;
            pitch -= Gdx.input.getDeltaY() * Constants.EDITOR_LOOK_SENSITIVITY;
            clampPitch();
        }
    }

    /** Classifies every active pointer, then applies its drag to move/look/altitude. */
    private void readTouch() {
        moveX = 0f;
        moveY = 0f;
        vertical = 0f;
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float btn = buttonSize();
        float m = buttonMargin();
        float lookSplit = w * 0.5f;
        float btnLeft = w - m - btn;
        float downTop = h - m - btn;
        float upTop = downTop - m - btn;
        for (int p = 0; p < MAX_POINTERS; p++) {
            boolean touched = Gdx.input.isTouched(p);
            float x = Gdx.input.getX(p);
            float y = Gdx.input.getY(p);
            if (touched) {
                if (!wasTouched[p]) {
                    originX[p] = x;
                    originY[p] = y;
                    pointerMode[p] = modeFor(x, y, btnLeft, upTop, downTop, btn, lookSplit);
                } else {
                    applyPointer(p, x, y);
                }
                lastX[p] = x;
                lastY[p] = y;
            }
            wasTouched[p] = touched;
        }
    }

    private static int modeFor(float x, float y, float btnLeft, float upTop, float downTop,
                               float btn, float lookSplit) {
        float margin = buttonMargin();
        if (x >= margin && x <= margin + DebugOverlay.modeButtonWidth()
                && y >= margin && y <= margin + DebugOverlay.modeButtonHeight()) {
            return MODE_IGNORE;   // the CAM toggle button, handled by DebugOverlay
        }
        if (x >= btnLeft && x <= btnLeft + btn) {
            if (y >= upTop && y <= upTop + btn) {
                return MODE_UP;
            }
            if (y >= downTop && y <= downTop + btn) {
                return MODE_DOWN;
            }
        }
        return x >= lookSplit ? MODE_LOOK : MODE_MOVE;
    }

    private void applyPointer(int p, float x, float y) {
        switch (pointerMode[p]) {
            case MODE_LOOK:
                yaw += (x - lastX[p]) * Constants.EDITOR_LOOK_SENSITIVITY;
                pitch -= (y - lastY[p]) * Constants.EDITOR_LOOK_SENSITIVITY;
                clampPitch();
                break;
            case MODE_MOVE:
                moveX += clampUnit((x - originX[p]) / Constants.EDITOR_TOUCH_RADIUS);
                moveY += clampUnit(-(y - originY[p]) / Constants.EDITOR_TOUCH_RADIUS);
                break;
            case MODE_UP:
                vertical += 1f;
                break;
            case MODE_DOWN:
                vertical -= 1f;
                break;
            case MODE_IGNORE:
            default:
                break;
        }
    }

    private static float clampUnit(float value) {
        return value > 1f ? 1f : (value < -1f ? -1f : value);
    }

    private boolean sprint() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private void clampPitch() {
        if (pitch > Constants.PITCH_MAX) {
            pitch = Constants.PITCH_MAX;
        } else if (pitch < Constants.PITCH_MIN) {
            pitch = Constants.PITCH_MIN;
        }
    }

    private void clampPosition() {
        Vector3 p = camera.position;
        if (p.x > halfExtent) {
            p.x = halfExtent;
        } else if (p.x < -halfExtent) {
            p.x = -halfExtent;
        }
        if (p.z > halfExtent) {
            p.z = halfExtent;
        } else if (p.z < -halfExtent) {
            p.z = -halfExtent;
        }
        if (p.y < Constants.EDITOR_MIN_Y) {
            p.y = Constants.EDITOR_MIN_Y;
        } else if (p.y > Constants.EDITOR_MAX_Y) {
            p.y = Constants.EDITOR_MAX_Y;
        }
    }

    private void apply() {
        direction(forward);
        camera.direction.set(forward);
        camera.up.set(0f, 1f, 0f);
        camera.update();
    }

    /** Unit look direction for the current yaw/pitch (yaw 0 = +X, yaw 90 = +Z). */
    private void direction(Vector3 out) {
        Math3D.direction(yaw, pitch, out);
    }
}

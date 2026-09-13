package com.brfps.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Picks the control scheme for the running device and forwards to it: touch devices
 * get the on-screen widgets, desktops get keyboard + mouse (R45). Screens only ever
 * talk to this class, so both schemes stay interchangeable.
 */
public class InputManager {

    private final DesktopInputHandler desktop = new DesktopInputHandler();
    private final TouchInputHandler touch = new TouchInputHandler();
    private final boolean touchDevice;

    public InputManager() {
        touchDevice = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen);
    }

    public boolean isTouchDevice() {
        return touchDevice;
    }

    /** Fills the state for this frame from the active scheme. */
    public void update(InputState out) {
        out.reset();
        if (touchDevice) {
            touch.update(out);
        } else {
            desktop.update(out);
        }
    }

    /** Look sensitivity in degrees per pixel for the active scheme. */
    public float lookSensitivity() {
        return touchDevice ? Constants.TOUCH_LOOK_SENSITIVITY : Constants.LOOK_SENSITIVITY;
    }

    /** Draws the touch widgets; a no-op on desktop. */
    public void render(SpriteBatch batch, Texture circle, BitmapFont font,
                       float width, float height) {
        if (touchDevice) {
            touch.render(batch, circle, font, width, height);
        }
    }

    /** One-line control hint for the HUD. */
    public String helpText() {
        return touchDevice
                ? "stick: move (full = sprint) | drag right: aim | JUMP | CRCH"
                : "WASD move | RMB drag: aim | SPACE jump | SHIFT sprint | CTRL crouch";
    }
}

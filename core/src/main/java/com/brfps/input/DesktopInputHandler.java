package com.brfps.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Desktop keyboard + mouse controls: WASD/arrows to move, a held right mouse button
 * drag to aim, left mouse button to fire, R to reload, Space to jump, Shift to
 * sprint, Ctrl or C to crouch, X to sit, Z to go prone, 1-4 to pick a weapon box,
 * Q to cycle weapons, H to use a medkit, M to zoom the minimap. The right button
 * aims (not the left) so the trigger hand is free, matching the editor camera.
 */
public class DesktopInputHandler {

    /** Fills the state for this frame. */
    public void update(InputState out) {
        float moveX = 0f;
        float moveY = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveY += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveY -= 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += 1f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= 1f;
        }
        out.moveX = moveX;
        out.moveY = moveY;

        out.sprint = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        out.crouch = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.C);
        out.sit = Gdx.input.isKeyPressed(Input.Keys.X);
        out.prone = Gdx.input.isKeyPressed(Input.Keys.Z);
        out.analogMove = false;
        out.jump = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        out.fire = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        out.reload = Gdx.input.isKeyJustPressed(Input.Keys.R);
        out.cycleWeapon = Gdx.input.isKeyJustPressed(Input.Keys.Q);
        out.mediPressed = Gdx.input.isKeyJustPressed(Input.Keys.H);
        out.mapTapped = Gdx.input.isKeyJustPressed(Input.Keys.M);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            out.weaponSlot = 0;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            out.weaponSlot = 1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            out.weaponSlot = 2;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
            out.weaponSlot = 3;
        }

        // Mouse look only while the right button is held; the left button is the trigger.
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            out.lookDX = Gdx.input.getDeltaX();
            out.lookDY = Gdx.input.getDeltaY();
        }
    }
}

package com.brfps.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.ui.TouchLookArea;
import com.brfps.ui.VirtualJoystick;
import com.brfps.util.Constants;

/**
 * Touch controls (R45): left-half virtual joystick to move, right-half drag to aim,
 * plus JUMP and CROUCH buttons. Every rectangle is a fraction of the screen (R46),
 * and each pointer is routed once on press, so a thumb on the stick never also aims.
 * Pointer slots are polled, so nothing is allocated per frame (R6).
 */
public class TouchInputHandler {

    private static final int MAX_POINTERS = 4;
    private static final int OWNER_NONE = 0;
    private static final int OWNER_STICK = 1;
    private static final int OWNER_LOOK = 2;
    private static final int OWNER_JUMP = 3;
    private static final int OWNER_CROUCH = 4;

    private final VirtualJoystick joystick = new VirtualJoystick();
    private final TouchLookArea lookArea = new TouchLookArea();
    private final GlyphLayout label = new GlyphLayout();
    private final int[] owner = new int[MAX_POINTERS];
    private final boolean[] wasTouched = new boolean[MAX_POINTERS];
    private boolean jumpDown;
    private boolean crouchDown;

    /** Routes every pointer for this frame and fills the state. */
    public void update(InputState out) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        jumpDown = false;
        crouchDown = false;

        for (int pointer = 0; pointer < MAX_POINTERS; pointer++) {
            boolean touched = Gdx.input.isTouched(pointer);
            float x = Gdx.input.getX(pointer);
            float y = Gdx.input.getY(pointer);
            if (touched) {
                if (wasTouched[pointer]) {
                    continueHeld(pointer, x, y, width, height);
                } else {
                    beginPointer(pointer, x, y, width, height);
                }
            } else if (wasTouched[pointer]) {
                release(pointer);
            }
            wasTouched[pointer] = touched;
        }

        out.moveX = joystick.getX();
        out.moveY = joystick.getY();
        out.sprint = joystick.magnitude() >= Constants.SPRINT_STICK_DEFLECTION;
        out.jump = jumpDown;
        out.crouch = crouchDown;
        out.lookDX = lookArea.consumeDX();
        out.lookDY = lookArea.consumeDY();
    }

    /** A fresh touch claims whichever widget it landed on. */
    private void beginPointer(int pointer, float x, float y, int width, int height) {
        owner[pointer] = claim(x, y, width, height);
        if (owner[pointer] == OWNER_STICK) {
            joystick.begin(x, y);
        } else if (owner[pointer] == OWNER_LOOK) {
            lookArea.begin(x, y);
        } else if (owner[pointer] == OWNER_JUMP) {
            jumpDown = true;
        } else if (owner[pointer] == OWNER_CROUCH) {
            crouchDown = true;
        }
    }

    /** A held touch feeds its owner; buttons keep reporting while pressed. */
    private void continueHeld(int pointer, float x, float y, int width, int height) {
        if (owner[pointer] == OWNER_STICK) {
            joystick.drag(x, y, VirtualJoystick.radius(width, height));
        } else if (owner[pointer] == OWNER_LOOK) {
            lookArea.drag(x, y);
        } else if (owner[pointer] == OWNER_JUMP) {
            jumpDown = true;
        } else if (owner[pointer] == OWNER_CROUCH) {
            crouchDown = true;
        }
    }

    private void release(int pointer) {
        if (owner[pointer] == OWNER_STICK) {
            joystick.end();
        } else if (owner[pointer] == OWNER_LOOK) {
            lookArea.end();
        }
        owner[pointer] = OWNER_NONE;
    }

    /** Buttons win over the two half-screen areas, so they stay usable. */
    private static int claim(float x, float y, int width, int height) {
        float size = buttonSize(width, height);
        float margin = buttonMargin(width, height);
        if (inside(x, y, width - margin - size, height - margin - size, size)) {
            return OWNER_JUMP;
        }
        if (inside(x, y, width - margin - size * 2.2f, height - margin - size, size)) {
            return OWNER_CROUCH;
        }
        return x < width * 0.5f ? OWNER_STICK : OWNER_LOOK;
    }

    private static boolean inside(float x, float y, float left, float top, float size) {
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    /** Button edge length in pixels. */
    public static float buttonSize(int width, int height) {
        return Math.min(width, height) * Constants.TOUCH_BUTTON_SIZE;
    }

    /** Screen margin in pixels. */
    public static float buttonMargin(int width, int height) {
        return Math.min(width, height) * Constants.TOUCH_MARGIN;
    }

    /** Draws the stick and the two action buttons. */
    public void render(SpriteBatch batch, Texture circle, BitmapFont font,
                       float width, float height) {
        joystick.render(batch, circle, width, height);
        float size = buttonSize((int) width, (int) height);
        float margin = buttonMargin((int) width, (int) height);
        drawButton(batch, circle, font, "JUMP", width - margin - size, margin, size);
        drawButton(batch, circle, font, "CRCH", width - margin - size * 2.2f, margin, size);
    }

    /** Round button with a centred label; x/y is its bottom-left in batch coords. */
    private void drawButton(SpriteBatch batch, Texture circle, BitmapFont font, String text,
                            float x, float y, float size) {
        batch.setColor(1f, 1f, 1f, 0.22f);
        batch.draw(circle, x, y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
        font.setColor(1f, 1f, 1f, 0.9f);
        label.setText(font, text);
        font.draw(batch, label, x + (size - label.width) * 0.5f,
                y + (size + label.height) * 0.5f);
    }
}

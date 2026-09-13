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
 * Touch controls (R45): left-half virtual joystick to move (mid deflection jogs, full
 * sprints), right-half drag to aim, FIRE + JUMP held buttons, CRCH / SIT / SLP stance
 * toggles and a SPR button that toggles on tap and sprints while held. Every
 * rectangle is a fraction of the screen (R46), and each pointer is routed once on
 * press, so a thumb on the stick never also aims. Pointer slots are polled, so
 * nothing is allocated per frame (R6).
 */
public class TouchInputHandler {

    private static final int MAX_POINTERS = 6;
    private static final int OWNER_NONE = 0;
    private static final int OWNER_STICK = 1;
    private static final int OWNER_LOOK = 2;
    private static final int OWNER_FIRE = 3;
    private static final int OWNER_JUMP = 4;
    private static final int OWNER_CROUCH = 5;
    private static final int OWNER_SPRINT = 6;
    private static final int OWNER_SEAT = 7;
    private static final int OWNER_SLEEP = 8;

    private static final int STANCE_STAND = 0;
    private static final int STANCE_CROUCH = 1;
    private static final int STANCE_SIT = 2;
    private static final int STANCE_PRONE = 3;

    private final VirtualJoystick joystick = new VirtualJoystick();
    private final TouchLookArea lookArea = new TouchLookArea();
    private final GlyphLayout label = new GlyphLayout();
    private final int[] owner = new int[MAX_POINTERS];
    private final boolean[] wasTouched = new boolean[MAX_POINTERS];
    private boolean fireDown;
    private boolean jumpDown;
    private boolean sprintHeld;
    private boolean sprintToggle;
    private boolean sprintActive;
    private int touchStance;

    /** Routes every pointer for this frame and fills the state. */
    public void update(InputState out) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        fireDown = false;
        jumpDown = false;
        sprintHeld = false;

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
        out.analogMove = true;
        out.sprint = joystick.magnitude() >= Constants.SPRINT_STICK_DEFLECTION
                || sprintToggle || sprintHeld;
        out.fire = fireDown;
        out.jump = jumpDown;
        out.crouch = touchStance == STANCE_CROUCH;
        out.sit = touchStance == STANCE_SIT;
        out.prone = touchStance == STANCE_PRONE;
        out.lookDX = lookArea.consumeDX();
        out.lookDY = lookArea.consumeDY();
    }

    /** Real sprint state (stamina-gated) for the SPR glow; set before render. */
    public void setSprintingActive(boolean sprinting) {
        sprintActive = sprinting;
    }

    /** A fresh touch claims whichever widget it landed on. */
    private void beginPointer(int pointer, float x, float y, int width, int height) {
        owner[pointer] = claim(x, y, width, height);
        tap(owner[pointer], x, y);
    }

    /** A held touch feeds its owner; FIRE/JUMP/SPR keep reporting while pressed. */
    private void continueHeld(int pointer, float x, float y, int width, int height) {
        if (owner[pointer] == OWNER_STICK) {
            joystick.drag(x, y, VirtualJoystick.radius(width, height));
        } else if (owner[pointer] == OWNER_LOOK) {
            lookArea.drag(x, y);
        } else if (owner[pointer] == OWNER_FIRE) {
            fireDown = true;
        } else if (owner[pointer] == OWNER_JUMP) {
            jumpDown = true;
        } else if (owner[pointer] == OWNER_SPRINT) {
            sprintHeld = true;
        }
        // Stance buttons (CRCH/SEAT/SLEEP) are toggles: nothing to do while held.
    }

    /** First frame of a touch: the stick/look claim it, buttons fire or flip. */
    private void tap(int ownerId, float x, float y) {
        if (ownerId == OWNER_STICK) {
            joystick.begin(x, y);
        } else if (ownerId == OWNER_LOOK) {
            lookArea.begin(x, y);
        } else if (ownerId == OWNER_FIRE) {
            fireDown = true;
        } else if (ownerId == OWNER_JUMP) {
            jumpDown = true;
        } else if (ownerId == OWNER_CROUCH) {
            touchStance = touchStance == STANCE_CROUCH ? STANCE_STAND : STANCE_CROUCH;
        } else if (ownerId == OWNER_SEAT) {
            touchStance = touchStance == STANCE_SIT ? STANCE_STAND : STANCE_SIT;
        } else if (ownerId == OWNER_SLEEP) {
            touchStance = touchStance == STANCE_PRONE ? STANCE_STAND : STANCE_PRONE;
        } else if (ownerId == OWNER_SPRINT) {
            sprintToggle = !sprintToggle;
            sprintHeld = true;
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
        float margin = buttonMargin(width, height);
        float fire = fireButtonSize(width, height);
        if (inside(x, y, fireLeft(width, height), height - margin - fire, fire)) {
            return OWNER_FIRE;
        }
        float size = buttonSize(width, height);
        if (inside(x, y, jumpLeft(width, height), height - margin - size, size)) {
            return OWNER_JUMP;
        }
        if (inside(x, y, crouchLeft(width, height), height - margin - size, size)) {
            return OWNER_CROUCH;
        }
        if (inside(x, y, sprintLeft(width, height), sprintTop(width, height), size)) {
            return OWNER_SPRINT;
        }
        if (inside(x, y, seatLeft(width, height), seatTop(width, height), size)) {
            return OWNER_SEAT;
        }
        if (inside(x, y, sleepLeft(width, height), sleepTop(width, height), size)) {
            return OWNER_SLEEP;
        }
        return x < width * 0.5f ? OWNER_STICK : OWNER_LOOK;
    }

    private static boolean inside(float x, float y, float left, float top, float size) {
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    /** Small button edge length in pixels (JUMP, CRCH, SPR, SIT, SLP). */
    public static float buttonSize(int width, int height) {
        return Math.min(width, height) * Constants.TOUCH_BUTTON_SIZE;
    }

    /** Trigger edge length in pixels — bigger than the other buttons. */
    public static float fireButtonSize(int width, int height) {
        return Math.min(width, height) * Constants.FIRE_BUTTON_SIZE;
    }

    /** Screen margin and button gap in pixels. */
    public static float buttonMargin(int width, int height) {
        return Math.min(width, height) * Constants.TOUCH_MARGIN;
    }

    /** Left edges of the three bottom-right buttons, bottoms aligned on the margin. */
    private static float fireLeft(int width, int height) {
        return width - buttonMargin(width, height) - fireButtonSize(width, height);
    }

    private static float jumpLeft(int width, int height) {
        return fireLeft(width, height) - buttonMargin(width, height) - buttonSize(width, height);
    }

    private static float crouchLeft(int width, int height) {
        return jumpLeft(width, height) - buttonMargin(width, height) - buttonSize(width, height);
    }

    /** SPR sits above JUMP, SIT above CRCH, SLP above SIT. */
    private static float sprintLeft(int width, int height) {
        return jumpLeft(width, height);
    }

    private static float seatLeft(int width, int height) {
        return crouchLeft(width, height);
    }

    private static float sleepLeft(int width, int height) {
        return crouchLeft(width, height);
    }

    /** Bottom edge (batch y-up coords) of a stacked row: 0 is the bottom row. */
    private static float stackedBottom(int level, int width, int height) {
        float margin = buttonMargin(width, height);
        float size = buttonSize(width, height);
        return margin + level * (size + margin);
    }

    /** Top edge (touch y-down coords) of a stacked button for the hit test. */
    private static float stackedTop(int level, int width, int height) {
        return height - stackedBottom(level, width, height) - buttonSize(width, height);
    }

    private static float sprintTop(int width, int height) {
        return stackedTop(1, width, height);
    }

    private static float seatTop(int width, int height) {
        return stackedTop(1, width, height);
    }

    private static float sleepTop(int width, int height) {
        return stackedTop(2, width, height);
    }

    /** Draws the stick and the six action buttons. */
    public void render(SpriteBatch batch, Texture circle, BitmapFont font,
                       float width, float height) {
        int screenWidth = (int) width;
        int screenHeight = (int) height;
        joystick.render(batch, circle, width, height);
        float margin = buttonMargin(screenWidth, screenHeight);
        float fire = fireButtonSize(screenWidth, screenHeight);
        float size = buttonSize(screenWidth, screenHeight);
        drawButton(batch, circle, font, "FIRE", fireLeft(screenWidth, screenHeight),
                margin, fire, fireDown);
        drawButton(batch, circle, font, "JUMP", jumpLeft(screenWidth, screenHeight),
                margin, size, jumpDown);
        drawButton(batch, circle, font, "CRCH", crouchLeft(screenWidth, screenHeight),
                margin, size, touchStance == STANCE_CROUCH);
        drawButton(batch, circle, font, "SPR", sprintLeft(screenWidth, screenHeight),
                stackedBottom(1, screenWidth, screenHeight), size, sprintActive);
        drawButton(batch, circle, font, "SIT", seatLeft(screenWidth, screenHeight),
                stackedBottom(1, screenWidth, screenHeight), size,
                touchStance == STANCE_SIT);
        drawButton(batch, circle, font, "SLP", sleepLeft(screenWidth, screenHeight),
                stackedBottom(2, screenWidth, screenHeight), size,
                touchStance == STANCE_PRONE);
    }

    /** Round button with a centred label; x/y is its bottom-left in batch coords. */
    private void drawButton(SpriteBatch batch, Texture circle, BitmapFont font, String text,
                            float x, float y, float size, boolean active) {
        if (active) {
            batch.setColor(1f, 0.9f, 0.25f, 0.55f);
        } else {
            batch.setColor(1f, 1f, 1f, 0.22f);
        }
        batch.draw(circle, x, y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
        font.setColor(1f, 1f, 1f, 0.9f);
        label.setText(font, text);
        font.draw(batch, label, x + (size - label.width) * 0.5f,
                y + (size + label.height) * 0.5f);
    }
}

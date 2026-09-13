package com.brfps.input;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.ui.HudBars;
import com.brfps.ui.TouchButton;
import com.brfps.ui.WeaponPanel;
import com.brfps.util.Constants;

/**
 * HUD-phase-A2 touch widgets, split out of {@link TouchInputHandler} so both files
 * stay inside the 300-line limit (R13): the RELOAD button (right row), the SCOPE
 * button (drawn greyed, never claimed — scopes arrive with master M22), the PACK
 * (cycle weapon) and MEDI buttons (left, below the HP bars), and taps on the four
 * weapon boxes (hit-tested with {@link WeaponPanel#hitSlot}, so areas and pictures
 * share one set of rectangles). Queued taps are momentary: {@link #writeState} copies
 * them into the {@link InputState} and clears them, so a tap fires exactly one frame.
 */
public class HudButtons {

    /** No HUD widget under the finger. */
    public static final int NONE = 0;
    public static final int RELOAD = 1;
    public static final int PACK = 2;
    public static final int MEDI = 3;
    /** Weapon-box taps: SLOT_0 + slot index. */
    public static final int SLOT_0 = 10;

    private final TouchButton drawer = new TouchButton();
    private final StringBuilder mediBuilder = new StringBuilder(12);
    private boolean reloadQueued;
    private boolean cycleQueued;
    private boolean mediQueued;
    private int slotQueued = -1;
    private int heldBits;

    /** Side-button edge length in pixels (RLD, SCOPE, PACK, MEDI). */
    public static float size(int width, int height) {
        return Math.min(width, height) * Constants.HUD_SIDE_BUTTON_SIZE;
    }

    /** Screen margin and button gap in pixels. */
    public static float margin(int width, int height) {
        return Math.min(width, height) * Constants.TOUCH_MARGIN;
    }

    /** Bottom edge (batch y-up) of the right-side RLD/SCOPE row. */
    public static float rightRowBottom(int width, int height) {
        return Math.min(width, height) * Constants.HUD_SIDE_BUTTON_BOTTOM;
    }

    /** RELOAD sits in the JUMP column, centred when the sizes differ. */
    public static float reloadLeft(int width, int height) {
        return TouchInputHandler.jumpLeft(width, height)
                + (TouchInputHandler.buttonSize(width, height) - size(width, height)) * 0.5f;
    }

    /** SCOPE is right-aligned on the same row (greyed until master M22). */
    public static float scopeLeft(int width, int height) {
        return width - margin(width, height) - size(width, height);
    }

    /** Top edge (batch y-up) of the left-side PACK/MEDI row, below the HP bars. */
    public static float leftRowTop(float width, float height) {
        return HudBars.contentBottom(width, height)
                - Math.min(width, height) * Constants.TOUCH_MARGIN;
    }

    /** PACK is the leftmost button of the left row. */
    public static float packLeft(int width, int height) {
        return margin(width, height);
    }

    /** MEDI sits right of PACK on the left row. */
    public static float mediLeft(int width, int height) {
        return margin(width, height) * 2f + size(width, height);
    }

    /**
     * Widget under a fresh touch (y-down pixels), or {@link #NONE}. Buttons win over
     * the weapon boxes, and SCOPE is deliberately never returned (pass-through to
     * the look area while it is disabled).
     */
    public static int hit(float x, float y, int width, int height, int activeSlot) {
        float side = size(width, height);
        float rightTop = height - rightRowBottom(width, height) - side;
        if (TouchButton.inside(x, y, reloadLeft(width, height), rightTop, side)) {
            return RELOAD;
        }
        float leftTop = height - leftRowTop(width, height);
        if (TouchButton.inside(x, y, packLeft(width, height), leftTop, side)) {
            return PACK;
        }
        if (TouchButton.inside(x, y, mediLeft(width, height), leftTop, side)) {
            return MEDI;
        }
        int slot = WeaponPanel.hitSlot(x, y, width, height, activeSlot);
        if (slot >= 0) {
            return SLOT_0 + slot;
        }
        return NONE;
    }

    /** First frame of a touch on a widget: queue its one-frame action. */
    public void tap(int id) {
        if (id == RELOAD) {
            reloadQueued = true;
        } else if (id == PACK) {
            cycleQueued = true;
        } else if (id == MEDI) {
            mediQueued = true;
        } else if (id >= SLOT_0) {
            slotQueued = id - SLOT_0;
        }
    }

    /** Tracks held fingers for the pressed-glow (visual only; taps already queued). */
    public void setHeld(int id, boolean down) {
        if (id == RELOAD || id == PACK || id == MEDI) {
            if (down) {
                heldBits |= 1 << id;
            } else {
                heldBits &= ~(1 << id);
            }
        }
    }

    /** Copies the queued taps into the state and clears them (call every frame). */
    public void writeState(InputState out) {
        out.reload = reloadQueued;
        out.cycleWeapon = cycleQueued;
        out.mediPressed = mediQueued;
        out.weaponSlot = slotQueued;
        reloadQueued = false;
        cycleQueued = false;
        mediQueued = false;
        slotQueued = -1;
    }

    /** Draws the four side buttons (boxes are drawn by the HUD itself). */
    public void render(SpriteBatch batch, Texture circle, BitmapFont font,
                       float width, float height, int medCount) {
        int w = (int) width;
        int h = (int) height;
        float side = size(w, h);
        float rightBottom = rightRowBottom(w, h);
        drawer.draw(batch, circle, font, "RLD", reloadLeft(w, h), rightBottom, side,
                (heldBits & (1 << RELOAD)) != 0 ? TouchButton.ACTIVE : TouchButton.NORMAL);
        drawer.draw(batch, circle, font, "SCOPE", scopeLeft(w, h), rightBottom, side,
                TouchButton.DISABLED);
        float leftBottom = leftRowTop(width, height) - side;
        drawer.draw(batch, circle, font, "PACK", packLeft(w, h), leftBottom, side,
                (heldBits & (1 << PACK)) != 0 ? TouchButton.ACTIVE : TouchButton.NORMAL);
        mediBuilder.setLength(0);
        mediBuilder.append("MEDI x").append(medCount);
        int mediState = medCount <= 0 ? TouchButton.DISABLED
                : (heldBits & (1 << MEDI)) != 0 ? TouchButton.ACTIVE : TouchButton.NORMAL;
        drawer.draw(batch, circle, font, mediBuilder, mediLeft(w, h), leftBottom, side,
                mediState);
    }
}

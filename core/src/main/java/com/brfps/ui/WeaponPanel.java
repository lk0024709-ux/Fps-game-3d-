package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * The four weapon boxes on the right edge (HUD phase A2): Box 1 is big and always
 * shows the ACTIVE weapon (name + ammo + fire mode), boxes 2-4 show the other slots
 * in slot order (name + ammo, name only for fists, "-" when empty). Tapping any box
 * selects that weapon — {@link #hitSlot} maps a touch point to a slot with the same
 * rectangles the draw call uses, so the two can never drift apart (R46). One layout
 * and one builder are reused for every box (R6).
 */
public class WeaponPanel {

    private static final String EMPTY_SLOT = "-";

    private final GlyphLayout layout = new GlyphLayout();
    private final StringBuilder ammoBuilder = new StringBuilder(32);

    /** Panel width in pixels for the current screen. */
    public static float panelWidth(float width, float height) {
        return width * Constants.WEAPON_PANEL_WIDTH;
    }

    /** Panel left edge in pixels (batch coords). */
    public static float panelLeft(float width, float height) {
        float shortSide = Math.min(width, height);
        return width - shortSide * Constants.TOUCH_MARGIN - panelWidth(width, height);
    }

    /** Panel top edge in pixels (batch y-up coords). */
    public static float panelTop(float width, float height) {
        return height - Math.min(width, height) * Constants.WEAPON_PANEL_TOP;
    }

    /** Top edge (batch y-up) of one box: 0 big, 1-2 small, 3 fist. */
    public static float boxTop(int box, float width, float height) {
        float shortSide = Math.min(width, height);
        float box1 = shortSide * Constants.WEAPON_BOX1_HEIGHT;
        float small = shortSide * Constants.WEAPON_BOX_SMALL_HEIGHT;
        float gap = shortSide * Constants.WEAPON_BOX_GAP;
        float top = panelTop(width, height);
        if (box == 0) {
            return top;
        }
        if (box <= 2) {
            return top - box1 - gap;
        }
        return top - box1 - gap - small - gap;
    }

    /** Bottom edge (batch y-up) of one box. */
    public static float boxBottom(int box, float width, float height) {
        float shortSide = Math.min(width, height);
        if (box == 0) {
            return boxTop(0, width, height) - shortSide * Constants.WEAPON_BOX1_HEIGHT;
        }
        if (box <= 2) {
            return boxTop(box, width, height) - shortSide * Constants.WEAPON_BOX_SMALL_HEIGHT;
        }
        return boxTop(3, width, height) - shortSide * Constants.WEAPON_BOX_FIST_HEIGHT;
    }

    /** Left edge of one box (boxes 1-2 split the row). */
    public static float boxLeft(int box, float width, float height) {
        float left = panelLeft(width, height);
        if (box != 2) {
            return left;
        }
        float gap = Math.min(width, height) * Constants.WEAPON_BOX_GAP;
        return left + (panelWidth(width, height) - gap) * 0.5f + gap;
    }

    /** Right edge of one box (boxes 1-2 split the row). */
    public static float boxRight(int box, float width, float height) {
        if (box != 1) {
            return panelLeft(width, height) + panelWidth(width, height);
        }
        float gap = Math.min(width, height) * Constants.WEAPON_BOX_GAP;
        return panelLeft(width, height) + (panelWidth(width, height) - gap) * 0.5f;
    }

    /**
     * Weapon slot shown in a box: box 0 is the active slot, boxes 1-3 are the
     * remaining slots in slot order (slot 3 reads as fists when no melee is carried).
     */
    public static int slotForBox(int box, int activeSlot) {
        if (box <= 0) {
            return activeSlot;
        }
        int seen = 0;
        for (int slot = 0; slot < Constants.WEAPON_SLOT_COUNT; slot++) {
            if (slot == activeSlot) {
                continue;
            }
            if (seen == box - 1) {
                return slot;
            }
            seen++;
        }
        return activeSlot;
    }

    /**
     * Slot index for a touch point (y-down pixels), or -1 when no box was hit.
     * Pass the active slot from the same frame's {@link HudData}.
     */
    public static int hitSlot(float x, float y, float width, float height, int activeSlot) {
        float yUp = height - y;
        for (int box = 0; box < 4; box++) {
            if (x >= boxLeft(box, width, height) && x <= boxRight(box, width, height)
                    && yUp >= boxBottom(box, width, height)
                    && yUp <= boxTop(box, width, height)) {
                return slotForBox(box, activeSlot);
            }
        }
        return -1;
    }

    /** Draws all four boxes; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, BitmapFont font,
                       HudData data, float width, float height) {
        drawBox(batch, white, font, data, 0, true, width, height);
        drawBox(batch, white, font, data, 1, false, width, height);
        drawBox(batch, white, font, data, 2, false, width, height);
        drawBox(batch, white, font, data, 3, false, width, height);
        font.getData().setScale(1f);
    }

    /** One box: dark tray, then the slot's name, ammo and (big box) fire mode. */
    private void drawBox(SpriteBatch batch, Texture white, BitmapFont font,
                         HudData data, int box, boolean big, float width, float height) {
        int slot = slotForBox(box, data.activeSlot);
        float left = boxLeft(box, width, height);
        float bottom = boxBottom(box, width, height);
        float top = boxTop(box, width, height);
        float boxWidth = boxRight(box, width, height) - left;
        batch.setColor(0f, 0f, 0f, Constants.WEAPON_PANEL_BG_ALPHA);
        batch.draw(white, left, bottom, boxWidth, top - bottom);
        if (big) {
            batch.setColor(1f, 0.9f, 0.25f, 0.9f); // gold spine: this one fires
            batch.draw(white, left, bottom, 3f, top - bottom);
        }
        batch.setColor(1f, 1f, 1f, 1f);

        font.getData().setScale(big ? 1f : 0.85f);
        if (!data.slotHas[slot]) {
            drawCentred(batch, font, EMPTY_SLOT, left, bottom, top, boxWidth, 0.45f);
            return;
        }
        String name = data.slotName[slot] == null ? EMPTY_SLOT : data.slotName[slot];
        if (data.slotMag[slot] < 0) { // fists / melee: a name, never ammo numbers
            drawCentred(batch, font, name, left, bottom, top, boxWidth, 0.9f);
            return;
        }
        float y = top - 3f;
        layout.setText(font, name);
        font.setColor(1f, 1f, 1f, 0.92f);
        font.draw(batch, layout, left + (big ? 7f : (boxWidth - layout.width) * 0.5f), y);
        y -= layout.height + 2f;
        ammoBuilder.setLength(0);
        ammoBuilder.append(data.slotMag[slot]).append('/').append(data.slotReserve[slot]);
        layout.setText(font, ammoBuilder);
        font.draw(batch, layout, left + (big ? 7f : (boxWidth - layout.width) * 0.5f), y);
        if (big) {
            y -= layout.height + 2f;
            String mode = data.slotMode[slot] == null ? "" : data.slotMode[slot];
            if (data.reloading && slot == data.activeSlot) {
                ammoBuilder.setLength(0);
                ammoBuilder.append("RLD ").append(Math.round(data.reloadProgress * 100f))
                        .append('%');
                layout.setText(font, ammoBuilder);
            } else {
                layout.setText(font, mode);
            }
            font.setColor(1f, 0.9f, 0.25f, 0.92f);
            font.draw(batch, layout, left + 7f, y);
        }
    }

    /** One centred line for empty slots and fists (no ammo to show). */
    private void drawCentred(SpriteBatch batch, BitmapFont font, String text,
                             float left, float bottom, float top, float boxWidth,
                             float alpha) {
        font.setColor(1f, 1f, 1f, alpha);
        layout.setText(font, text);
        font.draw(batch, layout, left + (boxWidth - layout.width) * 0.5f,
                bottom + (top - bottom + layout.height) * 0.5f);
    }
}

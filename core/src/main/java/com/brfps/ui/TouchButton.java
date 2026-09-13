package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * One reusable circular touch button: a tinted disc with a centred label, drawn in
 * the HUD batch (R28). Hit tests use {@link #inside} with the same x/y/size the draw
 * call uses, so a button's touch area can never drift away from its picture (R46:
 * callers pass viewport-relative sizes). Owns one {@link GlyphLayout}, so drawing
 * allocates nothing (R6).
 */
public class TouchButton {

    /** Idle button. */
    public static final int NORMAL = 0;
    /** Finger down, stance on, or the state the button reports is live. */
    public static final int ACTIVE = 1;
    /** Greyed out: drawn dim and never claimed by the touch routing. */
    public static final int DISABLED = 2;

    private final GlyphLayout label = new GlyphLayout();

    /** True when a touch point (y-down pixels) falls inside the square. */
    public static boolean inside(float x, float y, float left, float top, float size) {
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    /**
     * Draws the button; call between batch.begin() and batch.end().
     *
     * @param x    left edge in batch (y-up) pixels
     * @param y    bottom edge in batch (y-up) pixels
     * @param size edge length in pixels
     * @param state {@link #NORMAL}, {@link #ACTIVE} or {@link #DISABLED}
     * @param text a String or a reused StringBuilder (GlyphLayout takes either, R6)
     */
    public void draw(SpriteBatch batch, Texture circle, BitmapFont font, CharSequence text,
                     float x, float y, float size, int state) {
        if (state == ACTIVE) {
            batch.setColor(1f, 0.9f, 0.25f, 0.55f);
        } else if (state == DISABLED) {
            batch.setColor(0.45f, 0.45f, 0.48f, 0.35f);
        } else {
            batch.setColor(1f, 1f, 1f, 0.22f);
        }
        batch.draw(circle, x, y, size, size);
        batch.setColor(1f, 1f, 1f, 1f);
        font.setColor(1f, 1f, 1f, state == DISABLED ? 0.35f : 0.9f);
        label.setText(font, text);
        font.draw(batch, label, x + (size - label.width) * 0.5f,
                y + (size + label.height) * 0.5f);
    }
}

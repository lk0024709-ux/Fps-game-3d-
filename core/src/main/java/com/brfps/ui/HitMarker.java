package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * The hit confirmation cross (master M5): four short diagonal ticks that pop over the
 * crosshair for a few frames whenever a shot lands on something. Sized as a fraction of
 * the short screen side (R46) and drawn in the existing HUD batch, so it costs no draw
 * call. A kill will reuse the same marker in the damage colour once bots exist
 * (master M10/M36).
 */
public class HitMarker {

    private float remaining;

    /** Shows the marker for one hit. */
    public void show() {
        remaining = Constants.HIT_MARKER_TIME;
    }

    public void update(float delta) {
        if (remaining > 0f) {
            remaining -= delta;
            if (remaining < 0f) {
                remaining = 0f;
            }
        }
    }

    public boolean isActive() {
        return remaining > 0f;
    }

    /** 1 at the instant of the hit, 0 when the marker is gone. */
    public float life() {
        return remaining <= 0f ? 0f : remaining / Constants.HIT_MARKER_TIME;
    }

    /** Draws the marker; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, float screenWidth, float screenHeight) {
        if (remaining <= 0f) {
            return;
        }
        float life = life();
        float centerX = screenWidth * 0.5f;
        float centerY = screenHeight * 0.5f;
        float shortSide = Math.min(screenWidth, screenHeight);
        // The ticks slide outward as they fade, which is what makes the hit read.
        float gap = shortSide * (Constants.HIT_MARKER_GAP
                + Constants.HIT_MARKER_SPREAD * (1f - life));
        float length = shortSide * Constants.HIT_MARKER_LENGTH;
        float thickness = Math.max(1f, shortSide * Constants.HIT_MARKER_THICKNESS);

        batch.setColor(1f, 1f, 1f, life * Constants.HIT_MARKER_ALPHA);
        drawTick(batch, white, centerX + gap, centerY + gap, length, thickness, 45f);
        drawTick(batch, white, centerX - gap, centerY + gap, length, thickness, -45f);
        drawTick(batch, white, centerX + gap, centerY - gap, length, thickness, -45f);
        drawTick(batch, white, centerX - gap, centerY - gap, length, thickness, 45f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** One tick: a thin quad rotated 45 degrees around its own centre. */
    private void drawTick(SpriteBatch batch, Texture white, float x, float y,
                          float length, float thickness, float rotation) {
        float halfX = thickness * 0.5f;
        float halfY = thickness * 0.5f;
        batch.draw(white, x - halfX, y - halfY, halfX, halfY,
                length, thickness, 1f, 1f, rotation);
    }
}

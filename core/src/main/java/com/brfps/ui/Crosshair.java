package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Screen-centre crosshair drawn in the HUD batch: four ticks and a dot, all sized as a
 * fraction of the short screen side (R46). It dims while the weapon is reloading, so
 * the gun's state is readable without any text prompt.
 */
public class Crosshair {

    /** Draws the crosshair; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture white, Texture circle,
                       float screenWidth, float screenHeight, boolean reloading) {
        float centerX = screenWidth * 0.5f;
        float centerY = screenHeight * 0.5f;
        float shortSide = Math.min(screenWidth, screenHeight);
        float gap = shortSide * Constants.CROSSHAIR_GAP;
        float length = shortSide * Constants.CROSSHAIR_LENGTH;
        float thickness = Math.max(1f, shortSide * Constants.CROSSHAIR_THICKNESS);
        batch.setColor(1f, 1f, 1f, reloading
                ? Constants.CROSSHAIR_RELOAD_ALPHA : Constants.CROSSHAIR_ALPHA);

        batch.draw(white, centerX - thickness * 0.5f, centerY + gap, thickness, length);
        batch.draw(white, centerX - thickness * 0.5f, centerY - gap - length,
                thickness, length);
        batch.draw(white, centerX - gap - length, centerY - thickness * 0.5f,
                length, thickness);
        batch.draw(white, centerX + gap, centerY - thickness * 0.5f, length, thickness);

        float dot = thickness * Constants.CROSSHAIR_DOT_SCALE;
        batch.draw(circle, centerX - dot * 0.5f, centerY - dot * 0.5f, dot, dot);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}

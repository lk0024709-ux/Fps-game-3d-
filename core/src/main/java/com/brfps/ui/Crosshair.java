package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.brfps.util.Constants;

/**
 * Screen-centre crosshair drawn in the HUD batch: four ticks and a dot, all sized as a
 * fraction of the short screen side (R46). It dims while the weapon is reloading and —
 * since master M5 — blooms open while the recoil offset is alive, so the player can see
 * how far the gun has climbed without any text. The hit marker is a separate class
 * ({@link HitMarker}) drawn by {@link MatchHud} on top of this.
 */
public class Crosshair {

    /**
     * Draws the crosshair; call between batch.begin() and batch.end().
     *
     * @param reloading dims the crosshair while the magazine is changing
     * @param bloom     0 settled .. 1 full recoil kick; widens the gap between the ticks
     */
    public void render(SpriteBatch batch, Texture white, Texture circle,
                       float screenWidth, float screenHeight, boolean reloading, float bloom) {
        float centerX = screenWidth * 0.5f;
        float centerY = screenHeight * 0.5f;
        float shortSide = Math.min(screenWidth, screenHeight);
        float gap = shortSide * Constants.CROSSHAIR_GAP
                * (1f + bloom * (Constants.RECOIL_BLOOM_MAX - 1f));
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

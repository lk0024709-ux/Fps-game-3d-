package com.brfps.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.brfps.util.Constants;

/**
 * Screen-space muzzle flash (master M5): a soft glow, a hot core and a four-point star
 * that appear for a few frames after every shot, drawn in the existing HUD batch with
 * the generated radial texture — no image file, no extra draw call (R6, R28).
 *
 * <p><b>Deliberate limitation, recorded in SPEC:</b> the flash is anchored to a fixed
 * fraction of the screen because the game has no arms model yet. At master M20 (real
 * asset swap, {@code arms_<weapon>.g3dj}) it moves to the weapon's world-space muzzle
 * attach point and is rendered as a world quad, so this class becomes the fallback only.
 */
public class MuzzleFlash {

    private float remaining;
    private float rotation;
    private float scaleJitter = 1f;

    /** Starts one flash. */
    public void fire() {
        remaining = Constants.MUZZLE_FLASH_TIME;
        rotation = MathUtils.random(0f, 90f);
        scaleJitter = MathUtils.random(Constants.MUZZLE_FLASH_JITTER, 1f);
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

    /** 1 at the instant of the shot, 0 when the flash is gone. */
    public float life() {
        return remaining <= 0f ? 0f : remaining / Constants.MUZZLE_FLASH_TIME;
    }

    /** Draws the flash; call between batch.begin() and batch.end(). */
    public void render(SpriteBatch batch, Texture glow, float screenWidth, float screenHeight) {
        if (remaining <= 0f) {
            return;
        }
        float life = life();
        float shortSide = Math.min(screenWidth, screenHeight);
        float centerX = screenWidth * Constants.MUZZLE_FLASH_X;
        float centerY = screenHeight * Constants.MUZZLE_FLASH_Y;
        // The flash is short enough that it should read as a pop, not a fade: the glow
        // keeps most of its size while its alpha collapses.
        float glowSize = shortSide * Constants.MUZZLE_FLASH_SIZE
                * scaleJitter * (0.7f + 0.3f * life);
        float coreSize = glowSize * Constants.MUZZLE_FLASH_CORE_SCALE;

        batch.setColor(Constants.MUZZLE_FLASH_R, Constants.MUZZLE_FLASH_G,
                Constants.MUZZLE_FLASH_B, life * Constants.MUZZLE_FLASH_ALPHA);
        batch.draw(glow, centerX - glowSize * 0.5f, centerY - glowSize * 0.5f,
                glowSize * 0.5f, glowSize * 0.5f, glowSize, glowSize, 1f, 1f, rotation);

        // Four-point star: the same texture squashed along one axis, then 90 degrees off.
        float starWidth = glowSize * Constants.MUZZLE_FLASH_STAR_LENGTH;
        float starHeight = glowSize * Constants.MUZZLE_FLASH_STAR_THICKNESS;
        batch.setColor(1f, 1f, 0.9f, life * Constants.MUZZLE_FLASH_ALPHA);
        batch.draw(glow, centerX - starWidth * 0.5f, centerY - starHeight * 0.5f,
                starWidth * 0.5f, starHeight * 0.5f, starWidth, starHeight, 1f, 1f, rotation);
        batch.draw(glow, centerX - starHeight * 0.5f, centerY - starWidth * 0.5f,
                starHeight * 0.5f, starWidth * 0.5f, starHeight, starWidth, 1f, 1f, rotation);

        batch.setColor(1f, 1f, 1f, life);
        batch.draw(glow, centerX - coreSize * 0.5f, centerY - coreSize * 0.5f,
                coreSize * 0.5f, coreSize * 0.5f, coreSize, coreSize, 1f, 1f, rotation + 45f);
        batch.setColor(1f, 1f, 1f, 1f);
    }
}

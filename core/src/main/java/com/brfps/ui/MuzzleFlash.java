package com.brfps.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
    public void render(SpriteBatch batch, TextureRegion glow,
                       float screenWidth, float screenHeight) {
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

        // The rotated overload is draw(region, x, y, originX, originY, width, height,
        // scaleX, scaleY, rotation): x/y is the lower-left corner and the origin is
        // relative to it, so centring means x = centerX - width/2 and origin = width/2.
        batch.setColor(Constants.MUZZLE_FLASH_R, Constants.MUZZLE_FLASH_G,
                Constants.MUZZLE_FLASH_B, life * Constants.MUZZLE_FLASH_ALPHA);
        drawCentered(batch, glow, centerX, centerY, glowSize, glowSize, rotation);

        // Four-point star: the same texture squashed along one axis, then 90 degrees off.
        float starWidth = glowSize * Constants.MUZZLE_FLASH_STAR_LENGTH;
        float starHeight = glowSize * Constants.MUZZLE_FLASH_STAR_THICKNESS;
        batch.setColor(1f, 1f, 0.9f, life * Constants.MUZZLE_FLASH_ALPHA);
        drawCentered(batch, glow, centerX, centerY, starWidth, starHeight, rotation);
        drawCentered(batch, glow, centerX, centerY, starHeight, starWidth, rotation);

        batch.setColor(1f, 1f, 1f, life);
        drawCentered(batch, glow, centerX, centerY, coreSize, coreSize, rotation + 45f);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    /** One quad centred on a point, rotated around its own centre. */
    private void drawCentered(SpriteBatch batch, TextureRegion region, float centerX,
                              float centerY, float width, float height, float rotation) {
        batch.draw(region, centerX - width * 0.5f, centerY - height * 0.5f,
                width * 0.5f, height * 0.5f, width, height, 1f, 1f, rotation);
    }
}

package com.brfps.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

/**
 * Central asset holder. Real art is streamed through the AssetManager once it
 * exists on disk; until then generated textures back every placeholder — a 2x2
 * white quad tinted at draw time, a filled circle for the touch widgets and a radial
 * glow for the muzzle flash (master M5) — so the game always renders without shipping
 * any image file.
 */
public class Assets implements Disposable {

    private final AssetManager manager = new AssetManager();
    private Texture white;
    private Texture circle;
    private Texture glow;
    private BitmapFont font;

    /** Loads the handful of assets needed before the first frame. */
    public void loadCoreBlocking() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();

        Pixmap disc = new Pixmap(Constants.CIRCLE_TEXTURE_SIZE, Constants.CIRCLE_TEXTURE_SIZE,
                Pixmap.Format.RGBA8888);
        disc.setBlending(Pixmap.Blending.None);
        disc.setColor(Color.WHITE);
        int radius = Constants.CIRCLE_TEXTURE_SIZE / 2;
        disc.fillCircle(radius, radius, radius - 1);
        circle = new Texture(disc);
        disc.dispose();

        glow = buildGlow();

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
    }

    /**
     * Radial white glow with a quadratic alpha falloff, used by the muzzle flash
     * (squashed along one axis it also makes the flash star). Power-of-two and inside
     * the 512 cap (R12), generated once at startup so no PNG ships in the APK (R25).
     */
    private Texture buildGlow() {
        int size = Constants.MUZZLE_FLASH_TEXTURE_SIZE;
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setBlending(Pixmap.Blending.None);
        float center = (size - 1) * 0.5f;
        float maxDistance = size * 0.5f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = (x - center) / maxDistance;
                float dy = (y - center) / maxDistance;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float falloff = 1f - Math.min(distance, 1f);
                int alpha = Math.round(255f * falloff * falloff);
                pixmap.drawPixel(x, y, (alpha << 24) | 0x00FFFFFF);
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public AssetManager getManager() {
        return manager;
    }

    /** Solid white 2x2 texture, tinted at draw time for placeholder art. */
    public Texture white() {
        return white;
    }

    /** Generated filled circle, tinted at draw time for touch widgets. */
    public Texture circle() {
        return circle;
    }

    /** Generated radial glow for the muzzle flash (master M5). */
    public Texture glow() {
        return glow;
    }

    public BitmapFont font() {
        return font;
    }

    @Override
    public void dispose() {
        if (white != null) {
            white.dispose();
        }
        if (circle != null) {
            circle.dispose();
        }
        if (glow != null) {
            glow.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        manager.dispose();
    }
}

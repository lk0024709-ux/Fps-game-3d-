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
 * white quad tinted at draw time, and a filled circle for the touch widgets — so
 * the game always renders without shipping any image files.
 */
public class Assets implements Disposable {

    private final AssetManager manager = new AssetManager();
    private Texture white;
    private Texture circle;
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

        font = new BitmapFont();
        font.setUseIntegerPositions(false);
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
        if (font != null) {
            font.dispose();
        }
        manager.dispose();
    }
}

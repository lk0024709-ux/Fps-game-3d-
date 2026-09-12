package com.brfps.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Disposable;

/**
 * Central asset holder. Real art is streamed through the AssetManager once it
 * exists on disk; until then a generated 1x1 white texture backs every
 * placeholder so the game always renders.
 */
public class Assets implements Disposable {

    private final AssetManager manager = new AssetManager();
    private Texture white;
    private BitmapFont font;

    /** Loads the handful of assets needed before the first frame. */
    public void loadCoreBlocking() {
        Pixmap pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        white = new Texture(pixmap);
        pixmap.dispose();

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

    public BitmapFont font() {
        return font;
    }

    @Override
    public void dispose() {
        if (white != null) {
            white.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        manager.dispose();
    }
}

package com.brfps;

import com.badlogic.gdx.Game;
import com.brfps.screens.SplashScreen;
import com.brfps.util.Assets;

/**
 * Application entry point shared by all platforms.
 * Owns the global asset holder and the screen stack.
 */
public class BrFpsGame extends Game {

    private Assets assets;

    @Override
    public void create() {
        assets = new Assets();
        assets.loadCoreBlocking();
        setScreen(new SplashScreen(this));
    }

    public Assets getAssets() {
        return assets;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}

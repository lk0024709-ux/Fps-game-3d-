package com.brfps;

import com.badlogic.gdx.Game;
import com.brfps.audio.ShotBeep;
import com.brfps.screens.SplashScreen;
import com.brfps.util.Assets;

/**
 * Application entry point shared by all platforms.
 * Owns the global asset holder, the placeholder gunshot audio (master M5) and the
 * screen stack.
 */
public class BrFpsGame extends Game {

    private Assets assets;
    private ShotBeep shotBeep;

    @Override
    public void create() {
        assets = new Assets();
        assets.loadCoreBlocking();
        shotBeep = new ShotBeep();
        setScreen(new SplashScreen(this));
    }

    public Assets getAssets() {
        return assets;
    }

    /**
     * Placeholder gunshot audio, owned here so every screen shares one worker thread.
     * Master M13 replaces it with {@code SoundManager} and real OGG samples.
     */
    public ShotBeep getShotBeep() {
        return shotBeep;
    }

    @Override
    public void dispose() {
        if (getScreen() != null) {
            getScreen().dispose();
        }
        if (shotBeep != null) {
            shotBeep.dispose();
        }
        if (assets != null) {
            assets.dispose();
        }
    }
}

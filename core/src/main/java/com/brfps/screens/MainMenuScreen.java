package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.util.Constants;

/**
 * Placeholder main menu for M1. Milestone M8 replaces the text with a real
 * scene2d UI (play / lobby / settings).
 */
public class MainMenuScreen implements Screen {

    private final BrFpsGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();

    private float pulse;

    public MainMenuScreen(BrFpsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        pulse = 0f;
    }

    @Override
    public void render(float delta) {
        pulse += delta;

        if (Gdx.input.justTouched()) {
            dispose();
            game.setScreen(new GameScreen(game));
            return;
        }

        Gdx.gl.glClearColor(0.04f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();

        game.getAssets().font().getData().setScale(2.2f);
        game.getAssets().font().setColor(1f, 0.72f, 0.15f, 1f);
        layout.setText(game.getAssets().font(), Constants.GAME_TITLE);
        game.getAssets().font().draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                viewport.getWorldHeight() * 0.72f);

        game.getAssets().font().getData().setScale(1.1f);
        float blink = 0.55f + 0.45f * (float) Math.abs(Math.sin(pulse * 2.0));
        game.getAssets().font().setColor(1f, 1f, 1f, blink);
        layout.setText(game.getAssets().font(), "Tap to inspect the island (M2a)");
        game.getAssets().font().draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                viewport.getWorldHeight() * 0.42f);

        game.getAssets().font().getData().setScale(1f);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}

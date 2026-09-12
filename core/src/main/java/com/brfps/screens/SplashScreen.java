package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.util.Constants;

/** Fades the game title in and out, then hands over to the main menu. */
public class SplashScreen implements Screen {

    private static final float DURATION = 2.2f;

    private final BrFpsGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();

    private float elapsed;

    public SplashScreen(BrFpsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        elapsed = 0f;
    }

    @Override
    public void render(float delta) {
        elapsed += delta;

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float alpha = MathUtils.clamp(elapsed < 0.6f ? elapsed / 0.6f : 1f, 0f, 1f);
        if (elapsed > DURATION - 0.5f) {
            alpha = MathUtils.clamp((DURATION - elapsed) / 0.5f, 0f, 1f);
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        game.getAssets().font().getData().setScale(2f);
        game.getAssets().font().setColor(1f, 0.72f, 0.15f, alpha);
        layout.setText(game.getAssets().font(), Constants.GAME_TITLE);
        game.getAssets().font().draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                (viewport.getWorldHeight() + layout.height) * 0.5f);

        game.getAssets().font().getData().setScale(1f);
        game.getAssets().font().setColor(1f, 1f, 1f, alpha * 0.7f);
        layout.setText(game.getAssets().font(), "v" + Constants.VERSION);
        game.getAssets().font().draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                (viewport.getWorldHeight() * 0.5f) - 40f);
        batch.end();

        if (elapsed >= DURATION || Gdx.input.justTouched()) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
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

package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.debug.EditorScreen;
import com.brfps.util.Constants;

/**
 * Main menu: PLAY starts the first-person match screen (master M3), EDITOR opens the
 * developer world viewer while debug tools are enabled (master M2). Button rectangles
 * are fractions of the screen (R46) and are laid out in resize(), so taps are cheap
 * hit tests with no per-frame allocation. A real scene2d UI arrives with master M44.
 */
public class MainMenuScreen implements Screen {

    private static final String PLAY_LABEL = "PLAY";
    private static final String EDITOR_LABEL = "EDITOR";

    private final BrFpsGame game;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout layout = new GlyphLayout();

    private float playX;
    private float playY;
    private float editorX;
    private float editorY;
    private float buttonWidth;
    private float buttonHeight;
    private float pulse;

    public MainMenuScreen(BrFpsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        pulse = 0f;
        layoutButtons(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /** Places the two buttons (y-up batch coords, centered horizontally). */
    private void layoutButtons(int width, int height) {
        float shortSide = Math.min(width, height);
        buttonWidth = Math.min(width * 0.5f, shortSide * 0.7f);
        buttonHeight = shortSide * Constants.MENU_BUTTON_HEIGHT;
        playX = (width - buttonWidth) * 0.5f;
        playY = height * Constants.MENU_PLAY_Y;
        editorX = playX;
        editorY = height * Constants.MENU_EDITOR_Y;
    }

    @Override
    public void render(float delta) {
        pulse += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            startGame();
            return;
        }
        if (Constants.DEBUG_TOOLS_ENABLED && Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            startEditor();
            return;
        }
        if (Gdx.input.justTouched()) {
            float x = Gdx.input.getX();
            float y = viewport.getWorldHeight() - Gdx.input.getY();
            if (inside(x, y, playX, playY)) {
                startGame();
                return;
            }
            if (Constants.DEBUG_TOOLS_ENABLED && inside(x, y, editorX, editorY)) {
                startEditor();
                return;
            }
        }

        Gdx.gl.glClearColor(0.04f, 0.05f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        BitmapFont font = game.getAssets().font();

        font.getData().setScale(2.2f);
        font.setColor(1f, 0.72f, 0.15f, 1f);
        layout.setText(font, Constants.GAME_TITLE);
        font.draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                viewport.getWorldHeight() * 0.78f);

        drawButton(font, PLAY_LABEL, playX, playY);
        if (Constants.DEBUG_TOOLS_ENABLED) {
            drawButton(font, EDITOR_LABEL, editorX, editorY);
        }

        font.getData().setScale(1f);
        float blink = 0.5f + 0.5f * (float) Math.abs(Math.sin(pulse * 2f));
        font.setColor(1f, 1f, 1f, blink);
        layout.setText(font, Constants.DEBUG_TOOLS_ENABLED
                ? "PLAY: first person on the island  |  EDITOR (F1): fly over the map"
                : "PLAY: first person on the island");
        font.draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                viewport.getWorldHeight() * Constants.MENU_HINT_Y);

        font.setColor(1f, 1f, 1f, 0.45f);
        layout.setText(font, "v" + Constants.VERSION + " | offline | " + Constants.TOTAL_PLAYERS
                + " players");
        font.draw(batch, layout,
                (viewport.getWorldWidth() - layout.width) * 0.5f,
                viewport.getWorldHeight() * 0.08f);
        batch.end();
    }

    /** Translucent button with a centered label. */
    private void drawButton(BitmapFont font, String label, float x, float y) {
        batch.setColor(0.10f, 0.16f, 0.24f, 0.85f);
        batch.draw(game.getAssets().white(), x, y, buttonWidth, buttonHeight);
        batch.setColor(1f, 1f, 1f, 1f);
        font.getData().setScale(1.6f);
        font.setColor(1f, 1f, 1f, 1f);
        layout.setText(font, label);
        font.draw(batch, layout, x + (buttonWidth - layout.width) * 0.5f,
                y + (buttonHeight + layout.height) * 0.5f);
        font.getData().setScale(1f);
    }

    private boolean inside(float x, float y, float left, float bottom) {
        return x >= left && x <= left + buttonWidth
                && y >= bottom && y <= bottom + buttonHeight;
    }

    private void startGame() {
        dispose();
        game.setScreen(new GameScreen(game));
    }

    private void startEditor() {
        dispose();
        game.setScreen(new EditorScreen(game));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        layoutButtons(width, height);
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

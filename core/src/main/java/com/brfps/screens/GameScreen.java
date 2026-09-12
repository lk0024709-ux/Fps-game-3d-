package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.util.Constants;
import com.brfps.util.ScreenshotUtil;
import com.brfps.world.Arena;
import com.brfps.world.MapLayout;
import com.brfps.world.MapRegistry;

/**
 * M2a world viewer: renders the loaded arena with an orbiting debug camera,
 * a performance HUD (FPS / draw calls / triangles) and a one-shot screenshot.
 * Replaced by the real first-person GameScreen in M3.
 */
public class GameScreen implements Screen {

    private final BrFpsGame game;
    private final PerspectiveCamera camera;
    private final Arena arena;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout hudLayout = new GlyphLayout();
    private final StringBuilder hudBuilder = new StringBuilder(48);
    private String hudString = "";
    private int lastFps = -1;
    private int lastDrawCalls = -1;
    private int lastTriangles = -1;

    private float orbitAngle;
    private float elapsed;
    private boolean screenshotTaken;
    private final boolean loadFailed;

    public GameScreen(BrFpsGame game) {
        this.game = game;
        camera = new PerspectiveCamera(Constants.FOV_DEGREES,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = Constants.NEAR_PLANE;
        camera.far = Constants.FAR_PLANE;
        camera.position.set(Constants.ORBIT_RADIUS, Constants.ORBIT_HEIGHT, 0f);

        MapRegistry registry = new MapRegistry();
        registry.load();
        String mapId = registry.getDefaultMapId();
        MapLayout layout = registry.loadMap(mapId);
        if (layout == null) {
            Gdx.app.error("GameScreen", "no map could be loaded (id=" + mapId + ")");
            loadFailed = true;
            arena = null;
            return;
        }
        loadFailed = false;
        arena = new Arena(layout);
    }

    @Override
    public void show() {
        orbitAngle = 0f;
        elapsed = 0f;
        screenshotTaken = false;
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACK)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        updateCamera(delta);

        Gdx.gl.glClearColor(0.55f, 0.75f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (arena != null) {
            arena.render(camera);
        }

        renderHud();
        maybeCaptureScreenshot();
    }

    private void updateCamera(float delta) {
        if (arena == null) {
            return;
        }
        orbitAngle += Constants.ORBIT_SPEED * delta;
        double radians = Math.toRadians(orbitAngle);
        camera.position.set(
                (float) (Constants.ORBIT_RADIUS * Math.cos(radians)),
                Constants.ORBIT_HEIGHT,
                (float) (Constants.ORBIT_RADIUS * Math.sin(radians)));
        camera.lookAt(0f, 0f, 0f);
        camera.update();
    }

    private void renderHud() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        int fps = Gdx.graphics.getFramesPerSecond();
        int drawCalls = arena == null ? 0 : arena.getDrawCalls();
        int triangles = arena == null ? 0 : arena.getTrianglesRendered();
        if (fps != lastFps || drawCalls != lastDrawCalls || triangles != lastTriangles) {
            lastFps = fps;
            lastDrawCalls = drawCalls;
            lastTriangles = triangles;
            hudBuilder.setLength(0);
            hudBuilder.append("FPS: ").append(fps)
                    .append(" | DC: ").append(drawCalls)
                    .append(" | Tri: ").append(formatK(triangles));
            hudString = hudBuilder.toString();
        }

        BitmapFont font = game.getAssets().font();
        font.getData().setScale(1f);
        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        batch.begin();
        hudLayout.setText(font, hudString);
        font.draw(batch, hudLayout,
                viewport.getWorldWidth() - hudLayout.width - 12f,
                viewport.getWorldHeight() - 12f);
        if (loadFailed) {
            hudLayout.setText(font, "Map load failed - see logcat (BACK to return)");
            font.draw(batch, hudLayout,
                    (viewport.getWorldWidth() - hudLayout.width) * 0.5f,
                    viewport.getWorldHeight() * 0.5f);
        }
        batch.end();
    }

    private static String formatK(int value) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        return (value / 1000) + "." + ((value % 1000) / 100) + "k";
    }

    private void maybeCaptureScreenshot() {
        if (screenshotTaken || elapsed < Constants.SCREENSHOT_DELAY) {
            return;
        }
        screenshotTaken = true;
        if (arena != null) {
            ScreenshotUtil.capture("debug/screenshot.png");
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
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
        if (arena != null) {
            arena.dispose();
        }
    }
}

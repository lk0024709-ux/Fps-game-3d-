package com.brfps.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.screens.MainMenuScreen;
import com.brfps.util.Constants;
import com.brfps.util.ScreenshotUtil;
import com.brfps.world.Arena;
import com.brfps.world.MapLayout;
import com.brfps.world.MapRegistry;

/**
 * Developer world viewer, reached from the main menu's EDITOR button while
 * DEBUG_TOOLS_ENABLED is on (master M2, repo M2). Renders the loaded arena with
 * either the orbiting overview camera or the free-fly editor camera with grid and
 * axis gizmos, plus a performance HUD and a one-shot screenshot. Gameplay itself
 * lives in screens.GameScreen; this screen is a tool, not a milestone deliverable.
 */
public class EditorScreen implements Screen {

    private static final String ORBIT_LABEL = "CAM: ORBIT";
    private static final String EDITOR_LABEL = "CAM: EDITOR";

    private final BrFpsGame game;
    private final PerspectiveCamera camera;
    private final Arena arena;
    private final EditorCamera editorCamera;
    private final DebugOverlay debugOverlay;
    private final String helpText;
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout hudLayout = new GlyphLayout();
    private final StringBuilder hudBuilder = new StringBuilder(48);
    private final StringBuilder editorBuilder = new StringBuilder(96);
    private String hudString = "";
    private String editorString = "";
    private int lastFps = -1;
    private int lastDrawCalls = -1;
    private int lastTriangles = -1;
    private int lastPosX = Integer.MIN_VALUE;
    private int lastPosY = Integer.MIN_VALUE;
    private int lastPosZ = Integer.MIN_VALUE;
    private int lastYaw = Integer.MIN_VALUE;
    private int lastPitch = Integer.MIN_VALUE;

    private float orbitAngle;
    private float elapsed;
    private boolean screenshotTaken;
    private final boolean loadFailed;

    public EditorScreen(BrFpsGame game) {
        this.game = game;
        helpText = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen)
                ? "drag left half: fly - drag right half: look - UP/DN: altitude"
                : "WASD/arrows: move - Q/E: down/up - Shift: fast - RMB drag: look";
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
            Gdx.app.error("EditorScreen", "no map could be loaded (id=" + mapId + ")");
            loadFailed = true;
            arena = null;
            editorCamera = null;
            debugOverlay = null;
            return;
        }
        loadFailed = false;
        arena = new Arena(layout);
        editorCamera = new EditorCamera(camera, layout.size);
        debugOverlay = Constants.DEBUG_TOOLS_ENABLED ? new DebugOverlay(layout.size) : null;
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

        if (debugOverlay != null && debugOverlay.toggleRequested()) {
            editorCamera.setActive(!editorCamera.isActive());
        }
        if (editorActive()) {
            editorCamera.update(delta);
        } else {
            updateCamera(delta);
        }

        Gdx.gl.glClearColor(0.55f, 0.75f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (arena != null) {
            arena.render(camera);
        }
        if (editorActive() && debugOverlay != null) {
            debugOverlay.render(camera);
        }

        renderHud();
        maybeCaptureScreenshot();
    }

    /** True while the free-fly editor camera owns the view (master M2). */
    private boolean editorActive() {
        return editorCamera != null && editorCamera.isActive();
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
        if (editorActive() && debugOverlay != null) {
            drawCalls += debugOverlay.getDrawCalls();
        }
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
        batch.begin();
        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        hudLayout.setText(font, hudString);
        font.draw(batch, hudLayout,
                viewport.getWorldWidth() - hudLayout.width - 12f,
                viewport.getWorldHeight() - 12f);
        drawModeButton(font);
        if (editorActive()) {
            drawEditorHud(font);
        }
        if (loadFailed) {
            hudLayout.setText(font, "Map load failed - see logcat (BACK to return)");
            font.draw(batch, hudLayout,
                    (viewport.getWorldWidth() - hudLayout.width) * 0.5f,
                    viewport.getWorldHeight() * 0.5f);
        }
        batch.end();
    }

    /** Top-left CAM toggle: the only way to switch cameras on a touch device. */
    private void drawModeButton(BitmapFont font) {
        if (debugOverlay == null) {
            return;
        }
        float margin = EditorCamera.buttonMargin();
        float w = DebugOverlay.modeButtonWidth();
        float h = DebugOverlay.modeButtonHeight();
        drawButton(font, editorActive() ? EDITOR_LABEL : ORBIT_LABEL,
                margin, viewport.getWorldHeight() - margin - h, w, h);
    }

    /** Editor readout: coordinates, help text and the two altitude buttons. */
    private void drawEditorHud(BitmapFont font) {
        float worldW = viewport.getWorldWidth();
        float worldH = viewport.getWorldHeight();
        int x = Math.round(editorCamera.getX());
        int y = Math.round(editorCamera.getY());
        int z = Math.round(editorCamera.getZ());
        int yaw = Math.round(editorCamera.getYaw());
        int pitch = Math.round(editorCamera.getPitch());
        if (x != lastPosX || y != lastPosY || z != lastPosZ || yaw != lastYaw || pitch != lastPitch) {
            lastPosX = x;
            lastPosY = y;
            lastPosZ = z;
            lastYaw = yaw;
            lastPitch = pitch;
            editorBuilder.setLength(0);
            editorBuilder.append("X ").append(x).append("  Y ").append(y).append("  Z ").append(z)
                    .append("   yaw ").append(yaw).append("  pitch ").append(pitch);
            editorString = editorBuilder.toString();
        }
        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        hudLayout.setText(font, editorString);
        font.draw(batch, hudLayout, worldW - hudLayout.width - 12f, worldH - 34f);
        hudLayout.setText(font, helpText);
        font.draw(batch, hudLayout, worldW - hudLayout.width - 12f, worldH - 54f);

        float margin = EditorCamera.buttonMargin();
        float size = EditorCamera.buttonSize();
        float right = worldW - margin - size;
        drawButton(font, "UP", right, margin * 2f + size, size, size);
        drawButton(font, "DN", right, margin, size, size);
    }

    /** Translucent button background with a centered label (batch coords, y up). */
    private void drawButton(BitmapFont font, String label, float x, float y, float w, float h) {
        batch.setColor(0.06f, 0.10f, 0.16f, 0.38f);
        batch.draw(game.getAssets().white(), x, y, w, h);
        batch.setColor(Color.WHITE);
        font.setColor(1f, 1f, 1f, 0.95f);
        hudLayout.setText(font, label);
        font.draw(batch, hudLayout, x + (w - hudLayout.width) * 0.5f,
                y + (h + hudLayout.height) * 0.5f);
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
        if (debugOverlay != null) {
            debugOverlay.dispose();
        }
        if (arena != null) {
            arena.dispose();
        }
    }
}

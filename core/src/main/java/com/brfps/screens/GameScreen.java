package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.input.InputManager;
import com.brfps.input.InputState;
import com.brfps.player.FirstPersonCamera;
import com.brfps.player.MovementController;
import com.brfps.player.Player;
import com.brfps.util.Constants;
import com.brfps.util.ScreenshotUtil;
import com.brfps.world.Arena;
import com.brfps.world.BuildingCollider;
import com.brfps.world.MapLayout;
import com.brfps.world.MapRegistry;
import com.brfps.world.SpawnPoint;

/**
 * The playable first-person screen (master M3, repo M3a-M3c): loads the map, drops
 * the player on a spawn point, then runs look, movement, building collision and the
 * touch widgets every frame, with a health/perf HUD. Weapons, bots and the match
 * loop arrive in later milestones.
 */
public class GameScreen implements Screen {

    private static final String STAND_LABEL = "STAND";
    private static final String CROUCH_LABEL = "CROUCH";
    private static final String AIR_LABEL = "AIR";

    private final BrFpsGame game;
    private final PerspectiveCamera camera;
    private final Arena arena;
    private final Player player;
    private final FirstPersonCamera view;
    private final MovementController movement;
    private final InputManager input = new InputManager();
    private final InputState inputState = new InputState();
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final GlyphLayout hudLayout = new GlyphLayout();
    private final StringBuilder perfBuilder = new StringBuilder(48);
    private final StringBuilder statsBuilder = new StringBuilder(48);
    private final StringBuilder posBuilder = new StringBuilder(64);
    private final int colliderBoxes;
    private final boolean loadFailed;

    private String perfString = "";
    private String statsString = "";
    private String posString = "";
    private int lastFps = -1;
    private int lastDrawCalls = -1;
    private int lastTriangles = -1;
    private int lastHealth = -1;
    private int lastArmor = -1;
    private String lastStanceLabel = "";
    private int lastX = Integer.MIN_VALUE;
    private int lastZ = Integer.MIN_VALUE;
    private int lastY = Integer.MIN_VALUE;

    private float elapsed;
    private boolean screenshotTaken;

    public GameScreen(BrFpsGame game) {
        this.game = game;
        camera = new PerspectiveCamera(Constants.FOV_DEGREES,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = Constants.NEAR_PLANE;
        camera.far = Constants.FAR_PLANE;

        MapRegistry registry = new MapRegistry();
        registry.load();
        String mapId = registry.getDefaultMapId();
        MapLayout layout = registry.loadMap(mapId);
        if (layout == null) {
            Gdx.app.error("GameScreen", "no map could be loaded (id=" + mapId + ")");
            loadFailed = true;
            arena = null;
            player = null;
            view = null;
            movement = null;
            colliderBoxes = 0;
            return;
        }
        loadFailed = false;

        arena = new Arena(layout);
        player = new Player();
        spawnPlayer();
        view = new FirstPersonCamera(camera);
        BuildingCollider collider = layout.buildings.size > 0
                ? new BuildingCollider(layout.buildings) : null;
        movement = new MovementController(player, collider, layout.size);
        colliderBoxes = movement.getColliderBoxCount();
    }

    /** Drops the player on the first spawn point, facing into the map. */
    private void spawnPlayer() {
        Array<SpawnPoint> spawns = arena.getSpawnPoints();
        if (spawns.size > 0) {
            SpawnPoint spawn = spawns.first();
            player.spawn(spawn.x, spawn.z);
        } else {
            player.spawn(0f, 0f);
        }
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
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

        if (!loadFailed) {
            input.update(inputState);
            view.look(inputState.lookDX, inputState.lookDY, input.lookSensitivity());
            movement.update(delta, inputState, view.getYaw());
            view.apply(player);
        }

        Gdx.gl.glClearColor(0.55f, 0.75f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (arena != null) {
            arena.render(camera);
        }

        renderHud();
        maybeCaptureScreenshot();
    }

    /** Touch widgets, health line and the debug perf/position readout. */
    private void renderHud() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        batch.begin();
        input.render(batch, game.getAssets().circle(), game.getAssets().font(),
                worldWidth, worldHeight);

        BitmapFont font = game.getAssets().font();
        font.getData().setScale(1f);
        font.setColor(0.05f, 0.08f, 0.12f, 1f);
        hudLayout.setText(font, buildStatsString());
        font.draw(batch, hudLayout, 12f, worldHeight - 12f);
        hudLayout.setText(font, buildPerfString());
        font.draw(batch, hudLayout, worldWidth - hudLayout.width - 12f, worldHeight - 12f);

        if (Constants.DEBUG_TOOLS_ENABLED) {
            hudLayout.setText(font, buildPositionString());
            font.draw(batch, hudLayout, worldWidth - hudLayout.width - 12f, worldHeight - 32f);
            hudLayout.setText(font, input.helpText());
            font.draw(batch, hudLayout, 12f, worldHeight - 32f);
        }
        if (loadFailed) {
            hudLayout.setText(font, "Map load failed - see logcat (BACK to return)");
            font.draw(batch, hudLayout,
                    (worldWidth - hudLayout.width) * 0.5f, worldHeight * 0.5f);
        }
        batch.end();
    }

    /** "HP 100 | AR 0 | STAND", rebuilt only when a value actually changes. */
    private String buildStatsString() {
        if (loadFailed) {
            return "";
        }
        int health = Math.round(player.getStats().getHealth());
        int armor = Math.round(player.getStats().getArmor());
        String stance = stanceLabel();
        if (health != lastHealth || armor != lastArmor || !stance.equals(lastStanceLabel)) {
            lastHealth = health;
            lastArmor = armor;
            lastStanceLabel = stance;
            statsBuilder.setLength(0);
            statsBuilder.append("HP ").append(health)
                    .append(" | AR ").append(armor)
                    .append(" | ").append(stance);
            statsString = statsBuilder.toString();
        }
        return statsString;
    }

    private String stanceLabel() {
        if (!player.isOnGround()) {
            return AIR_LABEL;
        }
        return player.isCrouching() ? CROUCH_LABEL : STAND_LABEL;
    }

    /** "FPS 60 | DC 8 | Tri 6.3k", rebuilt only when a counter changes. */
    private String buildPerfString() {
        int fps = Gdx.graphics.getFramesPerSecond();
        int drawCalls = arena == null ? 0 : arena.getDrawCalls();
        int triangles = arena == null ? 0 : arena.getTrianglesRendered();
        if (fps != lastFps || drawCalls != lastDrawCalls || triangles != lastTriangles) {
            lastFps = fps;
            lastDrawCalls = drawCalls;
            lastTriangles = triangles;
            perfBuilder.setLength(0);
            perfBuilder.append("FPS ").append(fps)
                    .append(" | DC ").append(drawCalls)
                    .append(" | Tri ").append(formatK(triangles));
            perfString = perfBuilder.toString();
        }
        return perfString;
    }

    /** Feet position, eye height and the collision box count (debug builds only). */
    private String buildPositionString() {
        if (loadFailed) {
            return "";
        }
        int x = Math.round(player.getPosition().x);
        int z = Math.round(player.getPosition().z);
        int yTenths = Math.round(player.getPosition().y * 10f); // one decimal, no formatting cost
        if (x != lastX || yTenths != lastY || z != lastZ) {
            lastX = x;
            lastY = yTenths;
            lastZ = z;
            posBuilder.setLength(0);
            posBuilder.append("X ").append(x)
                    .append("  Z ").append(z)
                    .append("  Y ").append(yTenths / 10).append('.').append(Math.abs(yTenths % 10))
                    .append(" | BOX ").append(colliderBoxes);
            posString = posBuilder.toString();
        }
        return posString;
    }

    private static String formatK(int value) {
        if (value < 1000) {
            return String.valueOf(value);
        }
        return (value / 1000) + "." + ((value % 1000) / 100) + "k";
    }

    private void maybeCaptureScreenshot() {
        if (screenshotTaken || loadFailed || elapsed < Constants.SCREENSHOT_DELAY) {
            return;
        }
        screenshotTaken = true;
        ScreenshotUtil.capture("debug/firstperson.png");
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

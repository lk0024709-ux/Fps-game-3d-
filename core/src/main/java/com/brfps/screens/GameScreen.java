package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.input.InputManager;
import com.brfps.input.InputState;
import com.brfps.player.FirstPersonCamera;
import com.brfps.player.MovementController;
import com.brfps.player.Player;
import com.brfps.player.PlayerStats;
import com.brfps.ui.HitMarker;
import com.brfps.ui.HudData;
import com.brfps.ui.MatchHud;
import com.brfps.ui.MuzzleFlash;
import com.brfps.util.Constants;
import com.brfps.util.ScreenshotUtil;
import com.brfps.weapons.Weapon;
import com.brfps.weapons.WeaponController;
import com.brfps.weapons.WeaponFeel;
import com.brfps.weapons.WeaponType;
import com.brfps.world.Arena;
import com.brfps.world.BuildingCollider;
import com.brfps.world.ImpactDecals;
import com.brfps.world.MapLayout;
import com.brfps.world.MapRegistry;
import com.brfps.world.SpawnPoint;

/**
 * The playable first-person screen (master M3-M5, repo M3a-M3c / M5a-M5c): loads the
 * map, drops the player on a spawn point, then runs the frame in the fixed effect order
 * <b>look -> movement -> recoil -> fov -> shake -> camera apply -> weapons</b>, so movement
 * always sees the base yaw and a shot can never push the player around. Weapon feel
 * lives in {@code weapons.WeaponFeel} and the HUD in {@code ui.MatchHud} to keep this
 * class inside the 300-line limit (R13). Bots and the match loop arrive later.
 */
public class GameScreen implements Screen {

    private final BrFpsGame game;
    private final PerspectiveCamera camera;
    private final Arena arena;
    private final Player player;
    private final FirstPersonCamera view;
    private final MovementController movement;
    private final ImpactDecals decals;
    private final WeaponController weapons;
    private final WeaponFeel feel = new WeaponFeel();
    private final MuzzleFlash muzzleFlash = new MuzzleFlash();
    private final HitMarker hitMarker = new HitMarker();
    private final InputManager input = new InputManager();
    private final InputState inputState = new InputState();
    private final MatchHud hud = new MatchHud();
    private final HudData hudData = new HudData();
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private final boolean loadFailed;

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
            decals = null;
            weapons = null;
            return;
        }
        loadFailed = false;

        arena = new Arena(layout);
        player = new Player();
        spawnPlayer();
        view = new FirstPersonCamera(camera);
        BuildingCollider collider = new BuildingCollider(layout.buildings, layout.size);
        movement = new MovementController(player, collider, layout.size);
        decals = new ImpactDecals();
        Weapon pistol = new Weapon(WeaponType.PISTOL);
        weapons = new WeaponController(pistol, collider, decals);

        hudData.helpText = input.helpText();
        hudData.colliderBoxes = collider.getWallCount() + collider.getPlinthCount();
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
            updateGameplay(delta);
        }

        Gdx.gl.glClearColor(0.55f, 0.75f, 0.92f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (arena != null) {
            arena.render(camera);
            // Decals draw with the world's still-bound program: one extra draw call.
            decals.render(arena.worldShader());
        }

        renderHud();
        maybeCaptureScreenshot();
    }

    /** One frame of gameplay, in the effect order documented on the class. */
    private void updateGameplay(float delta) {
        input.update(inputState);
        view.look(inputState.lookDX, inputState.lookDY, input.lookSensitivity());
        movement.update(delta, inputState, view.getYaw()); // base yaw: before any offset
        feel.update(delta, inputState.fire); // recoil recovers first, then shake decays
        view.updateFov(delta, movement.isSprinting());
        muzzleFlash.update(delta);
        hitMarker.update(delta);
        view.apply(player, feel.getShake().offsetX(),
                feel.getRecoil().pitchOffset() + feel.getShake().offsetY(),
                feel.getShake().roll());
        weapons.update(delta, inputState.fire, inputState.reload,
                camera.position, camera.direction);
        if (weapons.firedThisFrame()) {
            onShot();
        }
    }

    /** One shot event: camera kick, flash, hit marker and the placeholder crack. */
    private void onShot() {
        feel.onShot(weapons.getLastShotRecoil());
        muzzleFlash.fire();
        if (weapons.getLastHit().hit) {
            hitMarker.show();
        }
        game.getShotBeep().play();
    }

    /** Touch widgets first, then the match HUD on top of them. */
    private void renderHud() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        fillHudData();
        input.setSprinting(!loadFailed && movement.isSprinting());
        batch.begin();
        input.render(batch, game.getAssets().circle(), game.getAssets().font(),
                worldWidth, worldHeight);
        hud.render(batch, game.getAssets(), hudData, muzzleFlash, hitMarker,
                worldWidth, worldHeight);
        batch.end();
    }

    /** Copies this frame's values into the reusable HUD snapshot (no allocation, R6). */
    private void fillHudData() {
        hudData.framesPerSecond = Gdx.graphics.getFramesPerSecond();
        if (loadFailed) {
            hudData.loadFailed = true;
            hudData.drawCalls = 0;
            hudData.triangles = 0;
            return;
        }
        hudData.loadFailed = false;
        PlayerStats stats = player.getStats();
        Vector3 feet = player.getPosition();
        Weapon weapon = weapons.getWeapon();
        hudData.health = Math.round(stats.getHealth());
        hudData.armor = Math.round(stats.getArmor());
        hudData.crouching = player.isCrouching();
        hudData.sitting = player.isSitting();
        hudData.prone = player.isProne();
        hudData.stamina = player.getStamina().getStamina();
        hudData.sprinting = movement.isSprinting();
        hudData.onGround = player.isOnGround();
        hudData.positionX = feet.x;
        hudData.positionY = feet.y;
        hudData.positionZ = feet.z;
        hudData.weaponName = weapon.getType().displayName();
        hudData.ammoInMagazine = weapon.getAmmoInMagazine();
        hudData.reserveAmmo = weapon.getReserveAmmo();
        hudData.reloading = weapon.isReloading();
        hudData.reloadProgress = weapon.getReloadProgress();
        hudData.drawCalls = arena.getDrawCalls() + decals.getDrawCalls();
        hudData.triangles = arena.getTrianglesRendered() + decals.getTrianglesRendered();
        hudData.decals = decals.getUsedCount();
        hudData.shotsFired = weapons.getShotsFired();
        hudData.shotsOnTarget = weapons.getShotsOnTarget();
        hudData.lastHitDistance = weapons.getLastHitDistance();
        hudData.crosshairBloom = feel.getBloom();
        hudData.recoilPitch = feel.getRecoil().pitchOffset();
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
        if (decals != null) {
            decals.dispose();
        }
        if (arena != null) {
            arena.dispose();
        }
    }
}

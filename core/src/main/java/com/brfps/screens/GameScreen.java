package com.brfps.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.brfps.BrFpsGame;
import com.brfps.input.InputManager;
import com.brfps.input.InputState;
import com.brfps.player.FirstPersonCamera;
import com.brfps.player.MovementController;
import com.brfps.player.Player;
import com.brfps.player.PlayerInventory;
import com.brfps.ui.HitMarker;
import com.brfps.ui.HudData;
import com.brfps.ui.HudDataBinder;
import com.brfps.ui.MatchHud;
import com.brfps.ui.MuzzleFlash;
import com.brfps.util.Constants;
import com.brfps.weapons.WeaponController;
import com.brfps.weapons.WeaponFeel;
import com.brfps.world.Arena;
import com.brfps.world.BuildingCollider;
import com.brfps.world.ImpactDecals;
import com.brfps.world.MapLayout;
import com.brfps.world.MapRegistry;
import com.brfps.world.SafeZone;

/**
 * The playable first-person screen (master M3-M5 + M34 A1-A2): fixed effect order
 * input -&gt; taps -&gt; look -&gt; move -&gt; recoil -&gt; fov -&gt; shake -&gt; apply
 * -&gt; weapons. Feel, HUD and wiring live in WeaponFeel/MatchHud/HudDataBinder (R13).
 */
public class GameScreen implements Screen {

    private final BrFpsGame game;
    private final PerspectiveCamera camera;
    private final PlayerInventory inventory = new PlayerInventory();
    private final SafeZone zone = new SafeZone(0f, 0f);
    private final WeaponFeel feel = new WeaponFeel();
    private final MuzzleFlash muzzleFlash = new MuzzleFlash();
    private final HitMarker hitMarker = new HitMarker();
    private final InputManager input = new InputManager();
    private final InputState inputState = new InputState();
    private final MatchHud hud = new MatchHud();
    private final HudData hudData = new HudData();
    private final SpriteBatch batch = new SpriteBatch();
    private final ScreenViewport viewport = new ScreenViewport();
    private Arena arena;
    private Player player;
    private FirstPersonCamera view;
    private MovementController movement;
    private ImpactDecals decals;
    private WeaponController weapons;
    private HudDataBinder binder;
    private final boolean loadFailed;

    public GameScreen(BrFpsGame game) {
        this.game = game;
        camera = new PerspectiveCamera(Constants.FOV_DEGREES,
                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = Constants.NEAR_PLANE;
        camera.far = Constants.FAR_PLANE;
        inventory.defaultLoadout();

        MapRegistry registry = new MapRegistry();
        registry.load();
        String mapId = registry.getDefaultMapId();
        MapLayout layout = registry.loadMap(mapId);
        if (layout == null) {
            Gdx.app.error("GameScreen", "no map could be loaded (id=" + mapId + ")");
            loadFailed = true;
            hudData.loadFailed = true;
            return;
        }
        loadFailed = false;

        arena = new Arena(layout);
        player = new Player();
        view = new FirstPersonCamera(camera);
        BuildingCollider collider = new BuildingCollider(layout.buildings, layout.size);
        movement = new MovementController(player, collider, layout.size);
        decals = new ImpactDecals();
        weapons = new WeaponController(inventory, collider, decals);
        binder = new HudDataBinder(player, inventory, weapons, movement, view,
                arena, decals, zone);
        binder.spawnWithClearance(arena.getSpawnPoints(), collider);

        hudData.helpText = input.helpText();
        hudData.colliderBoxes = collider.getWallCount() + collider.getPlinthCount();
    }

    @Override
    public void show() {
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void render(float delta) {
        if (HudDataBinder.backPressed()) {
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
        if (!loadFailed) {
            binder.maybeCaptureScreenshot(delta, loadFailed);
        }
    }

    /** One frame of gameplay, in the effect order documented on the class. */
    private void updateGameplay(float delta) {
        input.setActiveSlot(hudData.activeSlot);
        input.update(inputState);
        binder.consumeInput(inputState);
        view.look(inputState.lookDX, inputState.lookDY, input.lookSensitivity());
        movement.update(delta, inputState, view.getYaw()); // base yaw: before any offset
        feel.update(delta, inputState.fire); // recoil recovers first, then shake decays
        zone.update(delta); // the ring shrinks on schedule; damage is master M11
        view.updateFov(delta, movement.isSprinting());
        muzzleFlash.update(delta);
        hitMarker.update(delta);
        view.apply(player, feel.getShake().offsetX(),
                feel.getRecoil().pitchOffset() + feel.getShake().offsetY(),
                feel.getShake().roll());
        weapons.update(delta, inputState.fire, inputState.reload,
                camera.position, camera.direction);
        if (weapons.firedThisFrame()) {
            binder.onShotFired(feel, muzzleFlash, hitMarker, game.getShotBeep(),
                    weapons.getLastShotRecoil(), weapons.getLastHit().hit);
        }
    }

    /** Touch widgets first, then the match HUD on top of them. */
    private void renderHud() {
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        hudData.framesPerSecond = Gdx.graphics.getFramesPerSecond();
        if (!loadFailed) {
            binder.bind(hudData, loadFailed, feel.getBloom(),
                    feel.getRecoil().pitchOffset());
        }
        input.setSprinting(!loadFailed && movement.isSprinting());
        batch.begin();
        input.render(batch, game.getAssets().circle(), game.getAssets().font(),
                worldWidth, worldHeight, hudData.medCount);
        hud.render(batch, game.getAssets(), hudData, muzzleFlash, hitMarker,
                worldWidth, worldHeight);
        batch.end();
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

package com.brfps.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.brfps.audio.ShotBeep;
import com.brfps.input.InputState;
import com.brfps.items.ItemType;
import com.brfps.player.FirstPersonCamera;
import com.brfps.player.MovementController;
import com.brfps.player.Player;
import com.brfps.player.PlayerInventory;
import com.brfps.player.PlayerStats;
import com.brfps.util.Constants;
import com.brfps.util.ScreenshotUtil;
import com.brfps.weapons.Weapon;
import com.brfps.weapons.WeaponController;
import com.brfps.weapons.WeaponFeel;
import com.brfps.world.Arena;
import com.brfps.world.BuildingCollider;
import com.brfps.world.ImpactDecals;
import com.brfps.world.SafeZone;
import com.brfps.world.SpawnPoint;

/**
 * Owns everything between gameplay and the HUD (HUD phase A2): copies one frame of
 * values into the reusable {@link HudData} snapshot (no allocation, R6), consumes
 * the weapon/medkit taps from the {@link InputState}, picks a spawn point with
 * clearance and takes the one-shot debug screenshot. Extracted from
 * {@code screens.GameScreen} so that screen stays small (R13).
 */
public class HudDataBinder {

    private static final String FIST_NAME = "FIST";

    private final Player player;
    private final PlayerInventory inventory;
    private final WeaponController weapons;
    private final MovementController movement;
    private final FirstPersonCamera view;
    private final Arena arena;
    private final ImpactDecals decals;
    private final SafeZone zone;
    private final Vector3 scratch = new Vector3();
    private float elapsed;
    private boolean screenshotTaken;

    public HudDataBinder(Player player, PlayerInventory inventory, WeaponController weapons,
                         MovementController movement, FirstPersonCamera view,
                         Arena arena, ImpactDecals decals, SafeZone zone) {
        this.player = player;
        this.inventory = inventory;
        this.weapons = weapons;
        this.movement = movement;
        this.view = view;
        this.arena = arena;
        this.decals = decals;
        this.zone = zone;
    }

    /**
     * Drops the player on the first spawn point that is really open ground: each
     * candidate is pushed out of walls with the player circle, and the first one that
     * barely moves wins. When every spawn is inside geometry, the first one is used
     * pushed out anyway. The player faces the map centre, never a wall.
     */
    public void spawnWithClearance(Array<SpawnPoint> spawns, BuildingCollider collider) {
        float x = 0f;
        float z = 0f;
        boolean found = false;
        for (int i = 0; i < spawns.size && !found; i++) {
            SpawnPoint spawn = spawns.get(i);
            scratch.set(spawn.x, 0f, spawn.z);
            collider.resolve(scratch, Constants.PLAYER_RADIUS);
            float dx = scratch.x - spawn.x;
            float dz = scratch.z - spawn.z;
            if (dx * dx + dz * dz
                    < Constants.SPAWN_CLEARANCE * Constants.SPAWN_CLEARANCE) {
                x = scratch.x;
                z = scratch.z;
                found = true;
            }
        }
        if (!found && spawns.size > 0) {
            SpawnPoint spawn = spawns.first();
            scratch.set(spawn.x, 0f, spawn.z);
            collider.resolve(scratch, Constants.PLAYER_RADIUS);
            x = scratch.x;
            z = scratch.z;
        }
        player.spawn(x, z);
        // Yaw convention (Math3D): 0 looks along +X, 90 along +Z.
        view.setYaw((float) Math.atan2(-z, -x) * MathUtils.radiansToDegrees);
    }

    /** Applies this frame's weapon and medkit taps (call before the weapons tick). */
    public void consumeInput(InputState input) {
        if (input.weaponSlot >= 0) {
            inventory.switchTo(input.weaponSlot);
        }
        if (input.cycleWeapon) {
            inventory.switchWeapon();
        }
        if (input.mediPressed && inventory.consumeItem(ItemType.MEDKIT)) {
            player.getStats().heal(ItemType.MEDKIT.amount());
        }
    }

    /**
     * Copies this frame's values into the reusable HUD snapshot (no allocation).
     * Bloom and recoil pitch come from the screen's WeaponFeel (master M5).
     */
    public void bind(HudData data, boolean loadFailed, float crosshairBloom,
                     float recoilPitch) {
        data.framesPerSecond = Gdx.graphics.getFramesPerSecond();
        if (loadFailed) {
            data.loadFailed = true;
            data.drawCalls = 0;
            data.triangles = 0;
            return;
        }
        data.loadFailed = false;
        PlayerStats stats = player.getStats();
        Vector3 feet = player.getPosition();
        Weapon weapon = weapons.getWeapon();
        data.health = Math.round(stats.getHealth());
        data.armor = Math.round(stats.getArmor());
        data.crouching = player.isCrouching();
        data.sitting = player.isSitting();
        data.prone = player.isProne();
        data.stamina = player.getStamina().getStamina();
        data.sprinting = movement.isSprinting();
        data.onGround = player.isOnGround();
        data.positionX = feet.x;
        data.positionY = feet.y;
        data.positionZ = feet.z;
        data.yawDegrees = view.getYaw();
        data.aliveCount = Constants.TOTAL_PLAYERS;
        data.weaponName = weapon == null ? FIST_NAME : weapon.getType().displayName();
        data.ammoInMagazine = weapon == null ? 0 : weapon.getAmmoInMagazine();
        data.reserveAmmo = weapon == null ? 0 : weapon.getReserveAmmo();
        data.reloading = weapon != null && weapon.isReloading();
        data.reloadProgress = weapon == null ? 1f : weapon.getReloadProgress();
        data.fistActive = weapon == null;
        bindSlots(data);
        data.medCount = inventory.medCount();
        data.zoneRadius = zone.getRadius();
        data.zoneCenterX = zone.getCenter().x;
        data.zoneCenterZ = zone.getCenter().y;
        data.drawCalls = arena.getDrawCalls() + decals.getDrawCalls();
        data.triangles = arena.getTrianglesRendered() + decals.getTrianglesRendered();
        data.decals = decals.getUsedCount();
        data.shotsFired = weapons.getShotsFired();
        data.shotsOnTarget = weapons.getShotsOnTarget();
        data.lastHitDistance = weapons.getLastHitDistance();
        data.crosshairBloom = crosshairBloom;
        data.recoilPitch = recoilPitch;
    }

    /** Slot display in place: name, ammo, fire mode, carried (fists: name only). */
    private void bindSlots(HudData data) {
        for (int slot = 0; slot < Constants.WEAPON_SLOT_COUNT; slot++) {
            Weapon carried = inventory.getSlot(slot);
            if (carried != null) {
                data.slotHas[slot] = true;
                data.slotName[slot] = carried.getType().displayName();
                data.slotMag[slot] = carried.getAmmoInMagazine();
                data.slotReserve[slot] = carried.getReserveAmmo();
                data.slotMode[slot] = carried.getType().fireMode();
            } else if (slot == PlayerInventory.meleeSlot()) {
                data.slotHas[slot] = true;
                data.slotName[slot] = FIST_NAME;
                data.slotMag[slot] = -1;
                data.slotReserve[slot] = 0;
                data.slotMode[slot] = "";
            } else {
                data.slotHas[slot] = false;
                data.slotName[slot] = null;
                data.slotMag[slot] = 0;
                data.slotReserve[slot] = 0;
                data.slotMode[slot] = "";
            }
        }
        data.activeSlot = inventory.getActiveSlot();
    }

    /** BACK (Android) or ESC (desktop): leave the match for the main menu. */
    public static boolean backPressed() {
        return Gdx.input.isKeyJustPressed(Input.Keys.BACK)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);
    }

    /**
     * One shot event: camera kick, flash, hit marker and the placeholder crack.
     * Takes the pieces as parameters so the screen keeps owning the instances.
     */
    public void onShotFired(WeaponFeel feel, MuzzleFlash flash, HitMarker marker,
                            ShotBeep beep, float recoil, boolean hit) {
        feel.onShot(recoil);
        flash.fire();
        if (hit) {
            marker.show();
        }
        beep.play();
    }

    /** One debug screenshot a few seconds into the match (matches EditorScreen). */
    public void maybeCaptureScreenshot(float delta, boolean loadFailed) {
        elapsed += delta;
        if (screenshotTaken || loadFailed || elapsed < Constants.SCREENSHOT_DELAY) {
            return;
        }
        screenshotTaken = true;
        ScreenshotUtil.capture("debug/firstperson.png");
    }
}

package com.brfps.weapons;

import com.badlogic.gdx.math.Vector3;
import com.brfps.world.BuildingCollider;
import com.brfps.world.ImpactDecals;
import com.brfps.world.RayHit;

/**
 * Fires the player's weapon: ticks the gun, casts one hitscan ray per shot from the
 * camera, leaves a bullet hole on whatever it hit, and counts shots for the HUD.
 * The ray stops at the weapon's range; damage to bots lands with master M10, when
 * there is finally something to damage (bots will be tested before the world).
 * Since master M5 the class also reports the shot it just took
 * ({@link #firedThisFrame()}), which is the single event the screen turns into recoil,
 * shake, muzzle flash, hit marker and the placeholder beep.
 */
public class WeaponController {

    private final Weapon weapon;
    private final BuildingCollider collider;
    /** Null only where world effects are not wanted; shots still register. */
    private final ImpactDecals decals;
    private final RayHit hit = new RayHit();
    private int shotsFired;
    private int shotsOnTarget;
    private float lastHitDistance;
    private boolean firedThisFrame;
    private float lastShotRecoil;

    public WeaponController(Weapon weapon, BuildingCollider collider, ImpactDecals decals) {
        this.weapon = weapon;
        this.collider = collider;
        this.decals = decals;
    }

    /**
     * Runs the weapon for this frame.
     *
     * @param triggerHeld   fire button or left mouse button down
     * @param reloadPressed reload requested this frame (R key)
     * @param origin        ray start: the camera position, i.e. the player's eyes
     * @param direction     unit look direction from FirstPersonCamera
     */
    public void update(float delta, boolean triggerHeld, boolean reloadPressed,
                       Vector3 origin, Vector3 direction) {
        firedThisFrame = false;
        weapon.update(delta);
        if (reloadPressed) {
            weapon.startReload();
        }
        if (!triggerHeld || weapon.isReloading()) {
            return;
        }
        if (weapon.getAmmoInMagazine() == 0) {
            weapon.startReload(); // an empty magazine reloads itself; no dry fire
            return;
        }
        if (!weapon.fire()) {
            return; // still on the fire-rate cooldown
        }
        shotsFired++;
        firedThisFrame = true;
        lastShotRecoil = weapon.getType().recoil();
        castShot(origin, direction);
    }

    /** One hitscan ray against the world, and a decal where it lands. */
    private void castShot(Vector3 origin, Vector3 direction) {
        float range = weapon.getType().range();
        boolean hitSomething = collider.raycast(origin.x, origin.y, origin.z,
                direction.x, direction.y, direction.z, range, hit);
        if (!hitSomething) {
            return;
        }
        shotsOnTarget++;
        lastHitDistance = hit.distance;
        if (decals != null) {
            decals.add(hit.x, hit.y, hit.z, hit.normalX, hit.normalY, hit.normalZ);
        }
    }

    public Weapon getWeapon() {
        return weapon;
    }

    /** True only on the frame a shot left the barrel: drives every M5 effect. */
    public boolean firedThisFrame() {
        return firedThisFrame;
    }

    /** Vertical kick in degrees of the shot just fired ({@code WeaponType.recoil()}). */
    public float getLastShotRecoil() {
        return lastShotRecoil;
    }

    /** Impact data of the most recent shot, valid until the next shot. */
    public RayHit getLastHit() {
        return hit;
    }

    public int getShotsFired() {
        return shotsFired;
    }

    public int getShotsOnTarget() {
        return shotsOnTarget;
    }

    /** Distance to the last impact in meters (debug readout). */
    public float getLastHitDistance() {
        return lastHitDistance;
    }
}

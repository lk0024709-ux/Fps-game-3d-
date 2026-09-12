package com.brfps.weapons;

/**
 * Runtime state of one carried weapon: ammo, cooldown and reload timer.
 * Purely logical - rendering and raycasting live in WeaponManager (M4).
 */
public class Weapon {

    private final WeaponType type;
    private int ammoInMagazine;
    private int reserveAmmo;
    private float cooldown;
    private float reloadRemaining;

    public Weapon(WeaponType type) {
        this.type = type;
        this.ammoInMagazine = type.magazineSize();
        this.reserveAmmo = type.magazineSize() * 3;
    }

    public WeaponType getType() {
        return type;
    }

    public int getAmmoInMagazine() {
        return ammoInMagazine;
    }

    public int getReserveAmmo() {
        return reserveAmmo;
    }

    public boolean isReloading() {
        return reloadRemaining > 0f;
    }

    public boolean canFire() {
        return cooldown <= 0f && reloadRemaining <= 0f && ammoInMagazine > 0;
    }

    /** Consumes one round. Returns false when the shot could not be taken. */
    public boolean fire() {
        if (!canFire()) {
            return false;
        }
        ammoInMagazine--;
        cooldown = type.fireInterval();
        return true;
    }

    /** Starts a reload when it makes sense. Returns true when one began. */
    public boolean startReload() {
        if (reloadRemaining > 0f || ammoInMagazine >= type.magazineSize() || reserveAmmo <= 0) {
            return false;
        }
        reloadRemaining = type.reloadTime();
        return true;
    }

    /** Fills the magazine and the reserve (ammo box pickup). */
    public void refill() {
        ammoInMagazine = type.magazineSize();
        reserveAmmo = type.magazineSize() * 3;
    }

    public void update(float delta) {
        if (cooldown > 0f) {
            cooldown -= delta;
        }
        if (reloadRemaining > 0f) {
            reloadRemaining -= delta;
            if (reloadRemaining <= 0f) {
                reloadRemaining = 0f;
                int needed = type.magazineSize() - ammoInMagazine;
                int moved = Math.min(needed, reserveAmmo);
                ammoInMagazine += moved;
                reserveAmmo -= moved;
            }
        }
    }
}

package com.brfps.weapons;

/** Weapon catalogue. Stats are the single source of truth (see SPEC.md). */
public enum WeaponType {

    PISTOL("Pistol", 15f, 0.300f, 12, 40f, false, 1.4f, 1.2f),
    SMG("SMG", 12f, 0.080f, 30, 30f, false, 1.8f, 1.0f),
    RIFLE("Rifle", 25f, 0.150f, 30, 80f, false, 2.2f, 1.6f),
    SHOTGUN("Shotgun", 60f, 0.800f, 6, 15f, false, 2.6f, 4.5f),
    SNIPER("Sniper", 90f, 1.500f, 5, 200f, true, 3.0f, 6.0f);

    private final String displayName;
    private final float damage;
    private final float fireInterval;
    private final int magazineSize;
    private final float range;
    private final boolean projectile;
    private final float reloadTime;
    private final float recoil;

    WeaponType(String displayName, float damage, float fireInterval, int magazineSize,
               float range, boolean projectile, float reloadTime, float recoil) {
        this.displayName = displayName;
        this.damage = damage;
        this.fireInterval = fireInterval;
        this.magazineSize = magazineSize;
        this.range = range;
        this.projectile = projectile;
        this.reloadTime = reloadTime;
        this.recoil = recoil;
    }

    public String displayName() {
        return displayName;
    }

    public float damage() {
        return damage;
    }

    /** Seconds between shots. */
    public float fireInterval() {
        return fireInterval;
    }

    public int magazineSize() {
        return magazineSize;
    }

    public float range() {
        return range;
    }

    /** Sniper rounds travel as projectiles with drop; everything else is hitscan. */
    public boolean isProjectile() {
        return projectile;
    }

    public float reloadTime() {
        return reloadTime;
    }

    /** Vertical kick in degrees per shot. */
    public float recoil() {
        return recoil;
    }

    /** First-person arms model for this weapon. */
    public String armsModelPath() {
        return "models/arms_" + name().toLowerCase() + ".g3dj";
    }

    public String fireSoundPath() {
        return "sounds/" + name().toLowerCase() + "_fire.ogg";
    }
}

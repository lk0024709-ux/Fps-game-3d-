package com.brfps.ui;

import com.brfps.util.Constants;

/**
 * One frame of HUD values: filled by the screen, read by {@link MatchHud}. A plain
 * mutable snapshot so the HUD never allocates and never has to reach into gameplay
 * classes itself (R6) — the same pattern as {@code input.InputState}. The weapon
 * slot arrays are fixed at {@link Constants#WEAPON_SLOT_COUNT} and filled in place.
 */
public class HudData {

    // --- player ---
    public int health;
    public int armor;
    public boolean crouching;
    public boolean sitting;
    public boolean prone;
    public float stamina;
    public boolean sprinting;
    public boolean onGround;
    public float positionX;
    public float positionY;
    public float positionZ;
    /** Aim yaw in degrees (compass + minimap arrow, HUD phase A3). */
    public float yawDegrees;
    /** Players still in the match (alive counter, HUD phase A3). */
    public int aliveCount;

    // --- weapon ---
    public String weaponName;
    public int ammoInMagazine;
    public int reserveAmmo;
    public boolean reloading;
    public float reloadProgress;
    /** True when bare fists are active: the HUD shows "FIST", never ammo numbers. */
    public boolean fistActive;
    /** Slot display, filled in place (HUD phase A2): name, ammo, fire mode, carried. */
    public final String[] slotName = new String[Constants.WEAPON_SLOT_COUNT];
    public final int[] slotMag = new int[Constants.WEAPON_SLOT_COUNT];
    public final int[] slotReserve = new int[Constants.WEAPON_SLOT_COUNT];
    public final String[] slotMode = new String[Constants.WEAPON_SLOT_COUNT];
    public final boolean[] slotHas = new boolean[Constants.WEAPON_SLOT_COUNT];
    public int activeSlot;

    // --- items ---
    /** Carried medkit units (MEDI button count, HUD phase A2). */
    public int medCount;

    // --- safe zone (minimap ring, HUD phase A3) ---
    public float zoneRadius;
    public float zoneCenterX;
    public float zoneCenterZ;

    // --- world / performance ---
    public int framesPerSecond;
    public int drawCalls;
    public int triangles;
    public int colliderBoxes;
    public int decals;

    // --- shooting results (debug readout) ---
    public int shotsFired;
    public int shotsOnTarget;
    public float lastHitDistance;

    // --- game feel (master M5) ---
    /** 0 settled .. 1 full recoil kick: blooms the crosshair. */
    public float crosshairBloom;
    /** 1 while the hit marker should be visible. */
    public boolean hitMarker;
    /** 1 while the muzzle flash should be visible. */
    public boolean muzzleFlash;
    /** Current recoil pitch offset in degrees (debug readout). */
    public float recoilPitch;

    // --- static text owned by the screen ---
    public String helpText;
    public boolean loadFailed;
}
